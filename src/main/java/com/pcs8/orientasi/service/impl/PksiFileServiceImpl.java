package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.client.objectstorage.FileStorageService;
import com.pcs8.orientasi.domain.dto.response.PksiFileResponse;
import com.pcs8.orientasi.domain.entity.PksiDocument;
import com.pcs8.orientasi.domain.entity.PksiFile;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.PksiDocumentRepository;
import com.pcs8.orientasi.repository.PksiFileRepository;
import com.pcs8.orientasi.service.FileVersioningService;
import com.pcs8.orientasi.service.PksiFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    private final FileStorageService fileStorageService;
    private final PksiFileRepository pksiFileRepository;
    private final PksiDocumentRepository pksiDocumentRepository;
    private final FileVersioningService fileVersioningService;

    public PksiFileServiceImpl(
            FileStorageService fileStorageService,
            PksiFileRepository pksiFileRepository,
            PksiDocumentRepository pksiDocumentRepository,
            FileVersioningService fileVersioningService) {
        this.fileStorageService = fileStorageService;
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
            PksiFileResponse response = uploadSingleFile(pksiDocument, file, fileType, tanggalDokumen);
            responses.add(response);
        }
        return responses;
    }

    private PksiFileResponse uploadSingleFile(PksiDocument pksiDocument, MultipartFile file, String fileType, LocalDate tanggalDokumen) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }

        String extension = getFileExtension(originalName);

        // Upload to object storage — returns storageId (UUID string)
        String storageId = fileStorageService.uploadFile(file, "pksi", pksiDocument.getId().toString());
        String fileUrl = fileStorageService.getFileUrl(storageId);

        // Calculate version and file group
        Integer currentMaxVersion = pksiFileRepository.findMaxVersionByPksiIdAndFileType(pksiDocument.getId(), fileType);
        int newVersion = currentMaxVersion + 1;

        UUID fileGroupId;
        Optional<PksiFile> existingFile = pksiFileRepository.findFirstByPksiDocumentIdAndFileTypeOrderByVersionDesc(pksiDocument.getId(), fileType);
        if (existingFile.isPresent() && existingFile.get().getFileGroupId() != null) {
            fileGroupId = existingFile.get().getFileGroupId();
        } else {
            fileGroupId = UUID.randomUUID();
        }

        String displayName = fileVersioningService.generateDisplayName(fileType, pksiDocument.getNamaPksi(), newVersion, extension);

        PksiFile pksiFile = PksiFile.builder()
                .pksiDocument(pksiDocument)
                .fileName(storageId)
                .originalName(originalName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .blobUrl(fileUrl)
                .blobName(storageId)
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
            throw new IllegalArgumentException("File size exceeds maximum limit of 8MB");
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

        if (pksiFile.getBlobName() != null && fileStorageService.fileExists(pksiFile.getBlobName())) {
            fileStorageService.deleteFile(pksiFile.getBlobName());
        }
        pksiFileRepository.delete(pksiFile);
        log.info("Deleted file successfully");
    }

    @Override
    @Transactional
    public void deleteFilesByPksiId(UUID pksiId) {
        List<PksiFile> files = pksiFileRepository.findByPksiDocumentId(pksiId);
        for (PksiFile file : files) {
            if (file.getBlobName() != null && fileStorageService.fileExists(file.getBlobName())) {
                fileStorageService.deleteFile(file.getBlobName());
            }
        }
        pksiFileRepository.deleteByPksiDocumentId(pksiId);
        log.info("Deleted all files for PKSI document successfully");
    }

    @Override
    public String getDownloadUrl(UUID fileId) {
        PksiFile pksiFile = pksiFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND_MSG + fileId));
        return fileStorageService.getFileUrl(pksiFile.getBlobName());
    }

    @Override
    public byte[] downloadFile(UUID fileId) {
        PksiFile pksiFile = pksiFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND_MSG + fileId));
        return fileStorageService.downloadFile(pksiFile.getBlobName());
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
        return uploadSingleFile(pksiDocument, file, fileType, tanggalDokumen);
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
        return fileStorageService.downloadFile(targetFile.getBlobName());
    }
}
