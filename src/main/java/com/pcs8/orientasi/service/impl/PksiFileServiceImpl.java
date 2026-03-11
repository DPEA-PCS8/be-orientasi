package com.pcs8.orientasi.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.pcs8.orientasi.domain.dto.response.PksiFileResponse;
import com.pcs8.orientasi.domain.entity.PksiDocument;
import com.pcs8.orientasi.domain.entity.PksiFile;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.PksiDocumentRepository;
import com.pcs8.orientasi.repository.PksiFileRepository;
import com.pcs8.orientasi.service.PksiFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PksiFileServiceImpl implements PksiFileService {

    private static final Logger log = LoggerFactory.getLogger(PksiFileServiceImpl.class);
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10MB
    private static final String FILE_NOT_FOUND_MSG = "File not found with id: ";
    private static final String TEMP_PREFIX = "temp/";
    private static final String PKSI_PREFIX = "pksi/";
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/jpeg",
            "image/png",
            "image/gif"
    );

    private final BlobContainerClient blobContainerClient;
    private final PksiFileRepository pksiFileRepository;
    private final PksiDocumentRepository pksiDocumentRepository;

    @Autowired
    public PksiFileServiceImpl(
            @Autowired(required = false) BlobContainerClient blobContainerClient,
            PksiFileRepository pksiFileRepository,
            PksiDocumentRepository pksiDocumentRepository) {
        this.blobContainerClient = blobContainerClient;
        this.pksiFileRepository = pksiFileRepository;
        this.pksiDocumentRepository = pksiDocumentRepository;
        
        if (blobContainerClient == null) {
            log.warn("Azure Blob Storage is not configured. File upload feature will be disabled.");
        }
    }

    @Override
    @Transactional
    public List<PksiFileResponse> uploadFiles(UUID pksiId, MultipartFile[] files) {
        if (blobContainerClient == null) {
            throw new IllegalStateException("Azure Blob Storage is not configured");
        }

        PksiDocument pksiDocument = pksiDocumentRepository.findById(pksiId)
                .orElseThrow(() -> new ResourceNotFoundException("PKSI Document not found with id: " + pksiId));

        List<PksiFileResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            validateFile(file);
            
            try {
                PksiFileResponse response = uploadSingleFile(pksiDocument, file);
                responses.add(response);
            } catch (IOException e) {
                log.error("Failed to upload file for PKSI: {}. Error: {}", pksiId, e.getMessage(), e);
                throw new IllegalStateException("Failed to upload file for PKSI: " + pksiId, e);
            }
        }

        return responses;
    }

    @Override
    @Transactional
    public List<PksiFileResponse> uploadTempFiles(String sessionId, MultipartFile[] files) {
        if (blobContainerClient == null) {
            throw new IllegalStateException("Azure Blob Storage is not configured");
        }

        List<PksiFileResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            validateFile(file);
            
            try {
                PksiFileResponse response = uploadSingleTempFile(sessionId, file);
                responses.add(response);
            } catch (IOException e) {
                log.error("Failed to upload temp file for session: {}. Error: {}", sessionId, e.getMessage(), e);
                throw new IllegalStateException("Failed to upload temp file for session: " + sessionId, e);
            }
        }

        return responses;
    }

    private PksiFileResponse uploadSingleTempFile(String sessionId, MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }
        
        String extension = getFileExtension(originalName);
        String blobName = String.format("%s%s/%s_%s%s", 
                TEMP_PREFIX,
                sessionId, 
                UUID.randomUUID(), 
                System.currentTimeMillis(),
                extension);

        // Upload to Azure Blob Storage with try-with-resources
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
        try (var inputStream = file.getInputStream()) {
            blobClient.upload(inputStream, file.getSize(), true);
        }

        String blobUrl = blobClient.getBlobUrl();

        // Save metadata to database (without PKSI association)
        PksiFile pksiFile = PksiFile.builder()
                .pksiDocument(null) // No PKSI association yet
                .fileName(blobName)
                .originalName(originalName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .blobUrl(blobUrl)
                .blobName(blobName)
                .sessionId(sessionId) // Store session ID for later association
                .build();

        pksiFile = pksiFileRepository.save(pksiFile);

        log.info("Uploaded temp file with id {} for session {}", pksiFile.getId(), sessionId);

        return mapToResponse(pksiFile);
    }

    @Override
    @Transactional
    public List<PksiFileResponse> moveTempFilesToPermanent(UUID pksiId, String sessionId) {
        if (blobContainerClient == null) {
            throw new IllegalStateException("Azure Blob Storage is not configured");
        }

        PksiDocument pksiDocument = pksiDocumentRepository.findById(pksiId)
                .orElseThrow(() -> new ResourceNotFoundException("PKSI Document not found with id: " + pksiId));

        // Find all temp files for this session
        List<PksiFile> tempFiles = pksiFileRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
        List<PksiFileResponse> responses = new ArrayList<>();

        for (PksiFile tempFile : tempFiles) {
            try {
                // Move blob from temp to permanent location
                String oldBlobName = tempFile.getBlobName();
                String extension = getFileExtension(tempFile.getOriginalName());
                String newBlobName = String.format("%s%s/%s_%s%s",
                        PKSI_PREFIX,
                        pksiId,
                        UUID.randomUUID(),
                        System.currentTimeMillis(),
                        extension);

                BlobClient sourceBlob = blobContainerClient.getBlobClient(oldBlobName);
                BlobClient destBlob = blobContainerClient.getBlobClient(newBlobName);

                // Copy blob to new location
                destBlob.copyFromUrl(sourceBlob.getBlobUrl());

                // Delete old blob
                if (Boolean.TRUE.equals(sourceBlob.exists())) {
                    sourceBlob.delete();
                }

                // Update database record
                tempFile.setPksiDocument(pksiDocument);
                tempFile.setBlobName(newBlobName);
                tempFile.setFileName(newBlobName);
                tempFile.setBlobUrl(destBlob.getBlobUrl());
                tempFile.setSessionId(null); // Clear session ID
                tempFile = pksiFileRepository.save(tempFile);

                responses.add(mapToResponse(tempFile));
                log.info("Moved temp file {} to permanent location for PKSI {}", tempFile.getId(), pksiId);

            } catch (Exception e) {
                log.error("Failed to move temp file {} to permanent. Error: {}", tempFile.getId(), e.getMessage(), e);
                throw new IllegalStateException("Failed to move temp file to permanent storage", e);
            }
        }

        return responses;
    }

    @Override
    @Transactional
    public void deleteTempFiles(String sessionId) {
        List<PksiFile> tempFiles = pksiFileRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);

        for (PksiFile file : tempFiles) {
            if (blobContainerClient != null && file.getBlobName() != null) {
                BlobClient blobClient = blobContainerClient.getBlobClient(file.getBlobName());
                if (Boolean.TRUE.equals(blobClient.exists())) {
                    blobClient.delete();
                }
            }
            pksiFileRepository.delete(file);
        }

        log.info("Deleted {} temp files for session: {}", tempFiles.size(), sessionId);
    }

    private PksiFileResponse uploadSingleFile(PksiDocument pksiDocument, MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }
        
        String extension = getFileExtension(originalName);
        String blobName = String.format("%s%s/%s_%s%s", 
                PKSI_PREFIX,
                pksiDocument.getId(), 
                UUID.randomUUID(), 
                System.currentTimeMillis(),
                extension);

        // Upload to Azure Blob Storage with try-with-resources
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
        try (var inputStream = file.getInputStream()) {
            blobClient.upload(inputStream, file.getSize(), true);
        }

        String blobUrl = blobClient.getBlobUrl();

        // Save metadata to database
        PksiFile pksiFile = PksiFile.builder()
                .pksiDocument(pksiDocument)
                .fileName(blobName)
                .originalName(originalName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .blobUrl(blobUrl)
                .blobName(blobName)
                .build();

        pksiFile = pksiFileRepository.save(pksiFile);

        log.info("Uploaded file with id {} for PKSI {}", pksiFile.getId(), pksiDocument.getId());

        return mapToResponse(pksiFile);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("File type not allowed");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    @Override
    public List<PksiFileResponse> getFilesByPksiId(UUID pksiId) {
        List<PksiFile> files = pksiFileRepository.findByPksiDocumentIdOrderByCreatedAtDesc(pksiId);
        return files.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public void deleteFile(UUID fileId) {
        PksiFile pksiFile = pksiFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND_MSG + fileId));

        // Delete from Azure Blob Storage
        if (blobContainerClient != null && pksiFile.getBlobName() != null) {
            BlobClient blobClient = blobContainerClient.getBlobClient(pksiFile.getBlobName());
            if (Boolean.TRUE.equals(blobClient.exists())) {
                blobClient.delete();
            }
        }

        // Delete from database
        pksiFileRepository.delete(pksiFile);
        log.info("Deleted file with id: {}", fileId);
    }

    @Override
    @Transactional
    public void deleteFilesByPksiId(UUID pksiId) {
        List<PksiFile> files = pksiFileRepository.findByPksiDocumentId(pksiId);
        
        for (PksiFile file : files) {
            if (blobContainerClient != null && file.getBlobName() != null) {
                BlobClient blobClient = blobContainerClient.getBlobClient(file.getBlobName());
                if (Boolean.TRUE.equals(blobClient.exists())) {
                    blobClient.delete();
                }
            }
        }

        pksiFileRepository.deleteByPksiDocumentId(pksiId);
        log.info("Deleted all files for PKSI: {}", pksiId);
    }

    @Override
    public String getDownloadUrl(UUID fileId) {
        PksiFile pksiFile = pksiFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND_MSG + fileId));
        return pksiFile.getBlobUrl();
    }

    @Override
    public byte[] downloadFile(UUID fileId) {
        if (blobContainerClient == null) {
            throw new IllegalStateException("Azure Blob Storage is not configured");
        }

        PksiFile pksiFile = pksiFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND_MSG + fileId));

        BlobClient blobClient = blobContainerClient.getBlobClient(pksiFile.getBlobName());
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            blobClient.downloadStream(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Failed to download file with id: {}. Error: {}", fileId, e.getMessage(), e);
            throw new IllegalStateException("Failed to download file with id: " + fileId, e);
        }
    }

    @Override
    public PksiFileResponse getFileById(UUID fileId) {
        PksiFile pksiFile = pksiFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND_MSG + fileId));
        return mapToResponse(pksiFile);
    }

    private PksiFileResponse mapToResponse(PksiFile file) {
        return PksiFileResponse.builder()
                .id(file.getId())
                .pksiId(file.getPksiDocument() != null ? file.getPksiDocument().getId() : null)
                .fileName(file.getFileName())
                .originalName(file.getOriginalName())
                .contentType(file.getContentType())
                .fileSize(file.getFileSize())
                .blobUrl(file.getBlobUrl())
                .createdAt(file.getCreatedAt() != null ? file.getCreatedAt() : LocalDateTime.now())
                .build();
    }
}
