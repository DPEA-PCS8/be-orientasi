package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.response.Fs2FileResponse;
import com.pcs8.orientasi.domain.entity.Fs2Document;
import com.pcs8.orientasi.domain.entity.Fs2File;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.Fs2DocumentRepository;
import com.pcs8.orientasi.repository.Fs2FileRepository;
import com.pcs8.orientasi.service.MinioService;
import com.pcs8.orientasi.service.Fs2FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class Fs2FileServiceImpl implements Fs2FileService {

    private static final Logger log = LoggerFactory.getLogger(Fs2FileServiceImpl.class);
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024; // 20MB
    private static final String FILE_NOT_FOUND_MSG = "File not found with id: ";
    private static final String TEMP_PREFIX = "temp/";
    private static final String FS2_PREFIX = "FS2/";
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

    private final MinioService minioService;
    private final Fs2FileRepository fs2FileRepository;
    private final Fs2DocumentRepository fs2DocumentRepository;

    public Fs2FileServiceImpl(
            MinioService minioService,
            Fs2FileRepository fs2FileRepository,
            Fs2DocumentRepository fs2DocumentRepository) {
        this.minioService = minioService;
        this.fs2FileRepository = fs2FileRepository;
        this.fs2DocumentRepository = fs2DocumentRepository;
    }

    @Override
    @Transactional
    public List<Fs2FileResponse> uploadFiles(UUID fs2Id, MultipartFile[] files) {
        Fs2Document fs2Document = fs2DocumentRepository.findById(fs2Id)
                .orElseThrow(() -> new ResourceNotFoundException("F.S.2 Document not found with id: " + fs2Id));

        List<Fs2FileResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            checkFs2FileValidity(file);
            
            try {
                Fs2FileResponse response = uploadSingleFile(fs2Document, file);
                responses.add(response);
            } catch (IOException e) {
                log.error("Failed to upload file. Error: {}", e.getMessage(), e);
                throw new IllegalStateException("Failed to upload file", e);
            }
        }

        return responses;
    }

    @Override
    @Transactional
    public List<Fs2FileResponse> uploadTempFiles(String sessionId, MultipartFile[] files) {
        List<Fs2FileResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            checkFs2FileValidity(file);
            
            try {
                Fs2FileResponse response = uploadSingleTempFile(sessionId, file);
                responses.add(response);
            } catch (IOException e) {
                log.error("Failed to upload temp file. Error: {}", e.getMessage(), e);
                throw new IllegalStateException("Failed to upload temp file", e);
            }
        }

        return responses;
    }

    private Fs2FileResponse uploadSingleTempFile(String sessionId, MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }
        
        String extension = extractExtension(originalName);
        // Generate unique filename to avoid collisions
        String uniqueFileName = String.format("%s_%s%s", 
                UUID.randomUUID(), 
                System.currentTimeMillis(),
                extension);
        // The actual path that will be stored in Minio
        String blobName = String.format("%s%s/%s", 
                TEMP_PREFIX,
                sessionId, 
                uniqueFileName);

        // Upload to Minio using InputStream with exact blobName
        String fileUrl = minioService.uploadFile(
                file.getInputStream(),
                blobName,
                file.getContentType(),
                file.getSize()
        );

        // Save metadata to database (without F.S.2 association)
        Fs2File fs2File = Fs2File.builder()
                .fs2Document(null) // No F.S.2 association yet
                .fileName(blobName)
                .originalName(originalName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .blobUrl(fileUrl)
                .blobName(blobName)
                .sessionId(sessionId) // Store session ID for later association
                .build();

        fs2File = fs2FileRepository.save(fs2File);

        log.info("Uploaded temp file successfully");

        return mapToResponse(fs2File);
    }

    @Override
    @Transactional
    public List<Fs2FileResponse> moveTempFilesToPermanent(UUID fs2Id, String sessionId) {
        Fs2Document fs2Document = fs2DocumentRepository.findById(fs2Id)
                .orElseThrow(() -> new ResourceNotFoundException("F.S.2 Document not found with id: " + fs2Id));

        // Find all temp files for this session
        List<Fs2File> tempFiles = fs2FileRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
        List<Fs2FileResponse> responses = new ArrayList<>();

        for (Fs2File tempFile : tempFiles) {
            try {
                // Move blob from temp to permanent location
                String oldBlobName = tempFile.getBlobName();
                String extension = extractExtension(tempFile.getOriginalName());
                // Use FS2 ID for file naming
                String newBlobName = String.format("%s%s%s",
                        FS2_PREFIX,
                        fs2Id,
                        extension);

                // Download from old location
                InputStream inputStream = minioService.downloadFile(oldBlobName);
                
                // Upload to new location
                String newFileUrl = minioService.uploadFile(
                    inputStream,
                    newBlobName,
                    tempFile.getContentType(),
                    tempFile.getFileSize()
                );

                // Delete old blob
                if (minioService.fileExists(oldBlobName)) {
                    minioService.deleteFile(oldBlobName);
                }

                // Update database record
                tempFile.setFs2Document(fs2Document);
                tempFile.setBlobName(newBlobName);
                tempFile.setFileName(newBlobName);
                tempFile.setBlobUrl(newFileUrl);
                tempFile.setSessionId(null); // Clear session ID
                tempFile = fs2FileRepository.save(tempFile);

                responses.add(mapToResponse(tempFile));
                log.info("Moved temp file to permanent location successfully");

            } catch (Exception e) {
                log.error("Failed to move temp file to permanent. Error: {}", e.getMessage(), e);
                throw new IllegalStateException("Failed to move temp file to permanent storage", e);
            }
        }

        return responses;
    }

    @Override
    @Transactional
    public void deleteTempFiles(String sessionId) {
        List<Fs2File> tempFiles = fs2FileRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);

        for (Fs2File file : tempFiles) {
            if (file.getBlobName() != null && minioService.fileExists(file.getBlobName())) {
                minioService.deleteFile(file.getBlobName());
            }
            fs2FileRepository.delete(file);
        }

        log.info("Deleted temp files successfully");
    }

    private Fs2FileResponse uploadSingleFile(Fs2Document fs2Document, MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }
        
        String extension = extractExtension(originalName);
        // Use FS2 ID for file naming
        String blobName = String.format("%s%s%s", 
                FS2_PREFIX,
                fs2Document.getId(),
                extension);

        // Upload to Minio using InputStream with exact blobName
        String fileUrl = minioService.uploadFile(
                file.getInputStream(),
                blobName,
                file.getContentType(),
                file.getSize()
        );

        // Save metadata to database
        Fs2File fs2File = Fs2File.builder()
                .fs2Document(fs2Document)
                .fileName(blobName)
                .originalName(originalName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .blobUrl(fileUrl)
                .blobName(blobName)
                .build();

        fs2File = fs2FileRepository.save(fs2File);

        log.info("Uploaded file successfully");

        return mapToResponse(fs2File);
    }

    private void checkFs2FileValidity(MultipartFile uploadedFile) {
        boolean isEmptyFile = uploadedFile.isEmpty();
        boolean exceedsMaxSize = uploadedFile.getSize() > MAX_FILE_SIZE;
        boolean isInvalidContentType = !ALLOWED_CONTENT_TYPES.contains(uploadedFile.getContentType());
        
        if (isEmptyFile) {
            throw new IllegalArgumentException("Uploaded file cannot be empty");
        }
        if (exceedsMaxSize) {
            throw new IllegalArgumentException("Uploaded file exceeds the 20MB size limit");
        }
        if (isInvalidContentType) {
            throw new IllegalArgumentException("Uploaded file type is not supported");
        }
    }

    private String extractExtension(String name) {
        int lastDotIndex = name != null ? name.lastIndexOf(".") : -1;
        return lastDotIndex > 0 ? name.substring(lastDotIndex) : "";
    }

    @Override
    public List<Fs2FileResponse> getFilesByFs2Id(UUID fs2Id) {
        List<Fs2File> files = fs2FileRepository.findByFs2DocumentIdOrderByCreatedAtDesc(fs2Id);
        return files.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public void deleteFile(UUID fileId) {
        Fs2File fs2File = fs2FileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND_MSG + fileId));

        // Delete from Minio
        if (fs2File.getBlobName() != null && minioService.fileExists(fs2File.getBlobName())) {
            minioService.deleteFile(fs2File.getBlobName());
        }

        // Delete from database
        fs2FileRepository.delete(fs2File);
        log.info("Deleted file successfully");
    }

    @Override
    @Transactional
    public void deleteFilesByFs2Id(UUID fs2Id) {
        List<Fs2File> files = fs2FileRepository.findByFs2DocumentId(fs2Id);
        
        for (Fs2File file : files) {
            if (file.getBlobName() != null && minioService.fileExists(file.getBlobName())) {
                minioService.deleteFile(file.getBlobName());
            }
        }

        fs2FileRepository.deleteByFs2DocumentId(fs2Id);
        log.info("Deleted all files for F.S.2 document successfully");
    }

    @Override
    public String getDownloadUrl(UUID fileId) {
        Fs2File fs2File = fs2FileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND_MSG + fileId));
        return fs2File.getBlobUrl();
    }

    @Override
    public byte[] downloadFile(UUID fileId) {
        Fs2File fs2File = fs2FileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND_MSG + fileId));

        try (InputStream inputStream = minioService.downloadFile(fs2File.getBlobName());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Failed to download file. Error: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to download file", e);
        }
    }

    @Override
    public Fs2FileResponse getFileById(UUID fileId) {
        Fs2File fs2File = fs2FileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND_MSG + fileId));
        return mapToResponse(fs2File);
    }

    private Fs2FileResponse mapToResponse(Fs2File file) {
        return Fs2FileResponse.builder()
                .id(file.getId())
                .fs2Id(file.getFs2Document() != null ? file.getFs2Document().getId() : null)
                .fileName(file.getFileName())
                .originalName(file.getOriginalName())
                .contentType(file.getContentType())
                .fileSize(file.getFileSize())
                .blobUrl(file.getBlobUrl())
                .createdAt(file.getCreatedAt() != null ? file.getCreatedAt() : LocalDateTime.now())
                .build();
    }
}
