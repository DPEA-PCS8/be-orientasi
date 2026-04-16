package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.response.PksiFileResponse;
import com.pcs8.orientasi.domain.entity.PksiDocument;
import com.pcs8.orientasi.domain.entity.PksiFile;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.PksiDocumentRepository;
import com.pcs8.orientasi.repository.PksiFileRepository;
import com.pcs8.orientasi.service.FileVersioningService;
import com.pcs8.orientasi.service.MinioService;
import com.pcs8.orientasi.service.PksiFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PksiFileServiceImpl implements PksiFileService {

    private static final Logger log = LoggerFactory.getLogger(PksiFileServiceImpl.class);
    private static final long MAX_FILE_SIZE = 8L * 1024 * 1024; // 8MB
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

    private final MinioService minioService;
    private final PksiFileRepository pksiFileRepository;
    private final PksiDocumentRepository pksiDocumentRepository;
    private final FileVersioningService fileVersioningService;

    public PksiFileServiceImpl(
            MinioService minioService,
            PksiFileRepository pksiFileRepository,
            PksiDocumentRepository pksiDocumentRepository,
            FileVersioningService fileVersioningService) {
        this.minioService = minioService;
        this.pksiFileRepository = pksiFileRepository;
        this.pksiDocumentRepository = pksiDocumentRepository;
        this.fileVersioningService = fileVersioningService;
    }

    @Override
    @Transactional
    public List<PksiFileResponse> uploadFiles(UUID pksiId, MultipartFile[] files, String fileType, LocalDate tanggalDokumen) {
        PksiDocument pksiDocument = pksiDocumentRepository.findById(pksiId)
                .orElseThrow(() -> new ResourceNotFoundException("PKSI Document not found with id: " + pksiId));

        List<PksiFileResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            validateFile(file);
            
            try {
                PksiFileResponse response = uploadSingleFile(pksiDocument, file, fileType, tanggalDokumen);
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
    public List<PksiFileResponse> uploadTempFiles(String sessionId, MultipartFile[] files, String fileType, LocalDate tanggalDokumen) {
        List<PksiFileResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            validateFile(file);
            
            try {
                PksiFileResponse response = uploadSingleTempFile(sessionId, file, fileType, tanggalDokumen);
                responses.add(response);
            } catch (IOException e) {
                log.error("Failed to upload temp file. Error: {}", e.getMessage(), e);
                throw new IllegalStateException("Failed to upload temp file", e);
            }
        }

        return responses;
    }

    private PksiFileResponse uploadSingleTempFile(String sessionId, MultipartFile file, String fileType, LocalDate tanggalDokumen) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }
        
        String extension = getFileExtension(originalName);
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

        // Save metadata to database (without PKSI association)
        PksiFile pksiFile = PksiFile.builder()
                .pksiDocument(null) // No PKSI association yet
                .fileName(blobName)
                .originalName(originalName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .blobUrl(fileUrl)
                .blobName(blobName)
                .sessionId(sessionId) // Store session ID for later association
                .fileType(fileType) // T01 or T11
                .tanggalDokumen(tanggalDokumen)
                .build();

        pksiFile = pksiFileRepository.save(pksiFile);

        log.info("Uploaded temp file successfully");

        return mapToResponse(pksiFile);
    }

    @Override
    @Transactional
    public List<PksiFileResponse> moveTempFilesToPermanent(UUID pksiId, String sessionId) {
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
                tempFile.setPksiDocument(pksiDocument);
                tempFile.setBlobName(newBlobName);
                tempFile.setFileName(newBlobName);
                tempFile.setBlobUrl(newFileUrl);
                tempFile.setSessionId(null); // Clear session ID
                tempFile = pksiFileRepository.save(tempFile);

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
        List<PksiFile> tempFiles = pksiFileRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);

        for (PksiFile file : tempFiles) {
            if (file.getBlobName() != null && minioService.fileExists(file.getBlobName())) {
                minioService.deleteFile(file.getBlobName());
            }
            pksiFileRepository.delete(file);
        }

        log.info("Deleted temp files successfully");
    }

    private PksiFileResponse uploadSingleFile(PksiDocument pksiDocument, MultipartFile file, String fileType, LocalDate tanggalDokumen) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }
        
        String extension = getFileExtension(originalName);
        // Generate unique filename to avoid collisions
        String uniqueFileName = String.format("%s_%s%s", 
                UUID.randomUUID(), 
                System.currentTimeMillis(),
                extension);
        // The actual path that will be stored in Minio
        String blobName = String.format("%s%s/%s", 
                PKSI_PREFIX,
                pksiDocument.getId(), 
                uniqueFileName);

        // Upload to Minio using InputStream with exact blobName
        String fileUrl = minioService.uploadFile(
                file.getInputStream(),
                blobName,
                file.getContentType(),
                file.getSize()
        );

        // Calculate version and file group
        Integer currentMaxVersion = pksiFileRepository.findMaxVersionByPksiIdAndFileType(pksiDocument.getId(), fileType);
        int newVersion = currentMaxVersion + 1;
        
        // Get or create file group ID
        UUID fileGroupId;
        Optional<PksiFile> existingFile = pksiFileRepository.findFirstByPksiDocumentIdAndFileTypeOrderByVersionDesc(pksiDocument.getId(), fileType);
        if (existingFile.isPresent() && existingFile.get().getFileGroupId() != null) {
            fileGroupId = existingFile.get().getFileGroupId();
        } else {
            fileGroupId = UUID.randomUUID();
        }

        // Generate standardized display name
        String displayName = fileVersioningService.generateDisplayName(fileType, pksiDocument.getNamaPksi(), newVersion, extension);

        // Save metadata to database
        PksiFile pksiFile = PksiFile.builder()
                .pksiDocument(pksiDocument)
                .fileName(blobName)
                .originalName(originalName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .blobUrl(fileUrl)
                .blobName(blobName)
                .fileType(fileType)
                .version(newVersion)
                .fileGroupId(fileGroupId)
                .displayName(displayName)
                .tanggalDokumen(tanggalDokumen)
                .build();

        pksiFile = pksiFileRepository.save(pksiFile);

        log.info("Uploaded file successfully - id: {}, displayName: {}, version: {}", 
                pksiFile.getId(), pksiFile.getDisplayName(), pksiFile.getVersion());

        return mapToResponse(pksiFile, true);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 20MB");
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

        // Delete from Minio
        if (pksiFile.getBlobName() != null && minioService.fileExists(pksiFile.getBlobName())) {
            minioService.deleteFile(pksiFile.getBlobName());
        }

        // Delete from database
        pksiFileRepository.delete(pksiFile);
        log.info("Deleted file successfully");
    }

    @Override
    @Transactional
    public void deleteFilesByPksiId(UUID pksiId) {
        List<PksiFile> files = pksiFileRepository.findByPksiDocumentId(pksiId);
        
        for (PksiFile file : files) {
            if (file.getBlobName() != null && minioService.fileExists(file.getBlobName())) {
                minioService.deleteFile(file.getBlobName());
            }
        }

        pksiFileRepository.deleteByPksiDocumentId(pksiId);
        log.info("Deleted all files for PKSI document successfully");
    }

    @Override
    public String getDownloadUrl(UUID fileId) {
        PksiFile pksiFile = pksiFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND_MSG + fileId));
        return pksiFile.getBlobUrl();
    }

    @Override
    public byte[] downloadFile(UUID fileId) {
        PksiFile pksiFile = pksiFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND_MSG + fileId));

        try (InputStream inputStream = minioService.downloadFile(pksiFile.getBlobName());
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
    public PksiFileResponse getFileById(UUID fileId) {
        PksiFile pksiFile = pksiFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND_MSG + fileId));
        return mapToResponse(pksiFile);
    }

    private PksiFileResponse mapToResponse(PksiFile file) {
        return mapToResponse(file, false);
    }

    private PksiFileResponse mapToResponse(PksiFile file, boolean isLatest) {
        return PksiFileResponse.builder()
                .id(file.getId())
                .pksiId(file.getPksiDocument() != null ? file.getPksiDocument().getId() : null)
                .fileName(file.getFileName())
                .originalName(file.getOriginalName())
                .contentType(file.getContentType())
                .fileSize(file.getFileSize())
                .blobUrl(file.getBlobUrl())
                .fileType(file.getFileType())
                .createdAt(file.getCreatedAt() != null ? file.getCreatedAt() : LocalDateTime.now())
                .version(file.getVersion())
                .fileGroupId(file.getFileGroupId())
                .displayName(file.getDisplayName())
                .isLatestVersion(isLatest)
                .tanggalDokumen(file.getTanggalDokumen())
                .build();
    }

    // ==================== VERSIONING METHODS ====================

    @Override
    @Transactional
    public PksiFileResponse uploadNewVersion(UUID pksiId, MultipartFile file, String fileType, LocalDate tanggalDokumen) {
        PksiDocument pksiDocument = pksiDocumentRepository.findById(pksiId)
                .orElseThrow(() -> new ResourceNotFoundException("PKSI Document not found with id: " + pksiId));
        
        validateFile(file);
        
        try {
            return uploadSingleFile(pksiDocument, file, fileType, tanggalDokumen);
        } catch (IOException e) {
            log.error("Failed to upload new version. Error: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to upload new version", e);
        }
    }

    @Override
    public List<PksiFileResponse> getLatestVersionFiles(UUID pksiId) {
        List<PksiFile> latestFiles = pksiFileRepository.findLatestVersionFilesByPksiId(pksiId);
        return latestFiles.stream()
                .map(file -> mapToResponse(file, true))
                .toList();
    }

    @Override
    public List<PksiFileResponse> getFileHistory(UUID pksiId, String fileType) {
        List<PksiFile> history = pksiFileRepository.findFileHistoryByPksiIdAndFileType(pksiId, fileType);
        if (history.isEmpty()) {
            return List.of();
        }
        // First item is latest version
        return history.stream()
                .map(file -> mapToResponse(file, file.equals(history.get(0))))
                .toList();
    }

    @Override
    public List<PksiFileResponse> getFilesByGroupId(UUID fileGroupId) {
        List<PksiFile> files = pksiFileRepository.findByFileGroupIdOrderByVersionDesc(fileGroupId);
        if (files.isEmpty()) {
            return List.of();
        }
        // First item is latest version
        return files.stream()
                .map(file -> mapToResponse(file, file.equals(files.get(0))))
                .toList();
    }

    @Override
    public byte[] downloadFileVersion(UUID pksiId, String fileType, Integer version) {
        List<PksiFile> files = pksiFileRepository.findByPksiDocumentIdAndFileTypeOrderByVersionDesc(pksiId, fileType);
        PksiFile targetFile = files.stream()
                .filter(f -> f.getVersion().equals(version))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("File not found for PKSI %s, type %s, version %d", pksiId, fileType, version)));
        
        return downloadFileContent(targetFile);
    }

    private byte[] downloadFileContent(PksiFile pksiFile) {
        try (InputStream inputStream = minioService.downloadFile(pksiFile.getBlobName());
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
}
