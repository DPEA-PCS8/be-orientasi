package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.client.objectstorage.FileStorageService;
import com.pcs8.orientasi.domain.dto.response.Fs2FileResponse;
import com.pcs8.orientasi.domain.entity.Fs2Document;
import com.pcs8.orientasi.domain.entity.Fs2File;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.Fs2DocumentRepository;
import com.pcs8.orientasi.repository.Fs2FileRepository;
import com.pcs8.orientasi.service.FileVersioningService;
import com.pcs8.orientasi.service.Fs2FileService;
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
public class Fs2FileServiceImpl implements Fs2FileService {

    private static final Logger log = LoggerFactory.getLogger(Fs2FileServiceImpl.class);
    private static final long MAX_FILE_SIZE = 8L * 1024 * 1024; // 8MB
    private static final String FILE_NOT_FOUND_MSG = "File not found with id: ";
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/pdf",
            "application/x-pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".pdf", ".doc", ".docx");

    private final FileStorageService fileStorageService;
    private final Fs2FileRepository fs2FileRepository;
    private final Fs2DocumentRepository fs2DocumentRepository;
    private final FileVersioningService fileVersioningService;

    public Fs2FileServiceImpl(
            FileStorageService fileStorageService,
            Fs2FileRepository fs2FileRepository,
            Fs2DocumentRepository fs2DocumentRepository,
            FileVersioningService fileVersioningService) {
        this.fileStorageService = fileStorageService;
        this.fs2FileRepository = fs2FileRepository;
        this.fs2DocumentRepository = fs2DocumentRepository;
        this.fileVersioningService = fileVersioningService;
    }

    @Override
    @Transactional
    public List<Fs2FileResponse> uploadFiles(UUID fs2Id, MultipartFile[] files, String fileType, LocalDate tanggalDokumen) {
        Fs2Document fs2Document = fs2DocumentRepository.findById(fs2Id)
                .orElseThrow(() -> new ResourceNotFoundException("F.S.2 Document not found with id: " + fs2Id));

        List<Fs2FileResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            checkFs2FileValidity(file);
            Fs2FileResponse response = uploadSingleFile(fs2Document, file, fileType, tanggalDokumen);
            responses.add(response);
        }
        return responses;
    }

    private Fs2FileResponse uploadSingleFile(Fs2Document fs2Document, MultipartFile file, String fileType, LocalDate tanggalDokumen) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }

        String extension = extractExtension(originalName);

        // Upload to object storage — returns storageId (UUID string)
        String storageId = fileStorageService.uploadFile(file, "fs2", fs2Document.getId().toString());
        String fileUrl = fileStorageService.getFileUrl(storageId);

        // Calculate version and file group
        Integer currentMaxVersion = fs2FileRepository.findMaxVersionByFs2IdAndFileType(fs2Document.getId(), fileType);
        int newVersion = currentMaxVersion + 1;

        UUID fileGroupId;
        Optional<Fs2File> existingFile = fs2FileRepository.findFirstByFs2DocumentIdAndFileTypeOrderByVersionDesc(fs2Document.getId(), fileType);
        if (existingFile.isPresent() && existingFile.get().getFileGroupId() != null) {
            fileGroupId = existingFile.get().getFileGroupId();
        } else {
            fileGroupId = UUID.randomUUID();
        }

        String docName = fs2Document.getAplikasi() != null
                ? fs2Document.getAplikasi().getNamaAplikasi()
                : "FS2_" + fs2Document.getId().toString().substring(0, 8);
        String displayName = fileVersioningService.generateDisplayName(fileType, docName, newVersion, extension);

        Fs2File fs2File = Fs2File.builder()
                .fs2Document(fs2Document)
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

        fs2File = fs2FileRepository.save(fs2File);
        updateBerkasField(fs2Document, fileType);

        log.info("Uploaded file successfully - id: {}, displayName: {}, version: {}",
                fs2File.getId(), fs2File.getDisplayName(), fs2File.getVersion());

        return mapToResponse(fs2File, true);
    }

    private void checkFs2FileValidity(MultipartFile uploadedFile) {
        long fileSize = uploadedFile.getSize();
        String originalName = uploadedFile.getOriginalFilename();

        boolean isEmptyFile = uploadedFile.isEmpty();
        boolean exceedsMaxSize = fileSize > MAX_FILE_SIZE;

        String contentType = uploadedFile.getContentType();
        String extension = extractExtension(originalName).toLowerCase();

        boolean isValidContentType = contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType);
        boolean isValidExtension = ALLOWED_EXTENSIONS.contains(extension);
        boolean isInvalidFileType = !isValidContentType && !isValidExtension;

        if (isEmptyFile) {
            throw new IllegalArgumentException("Uploaded file cannot be empty");
        }
        if (exceedsMaxSize) {
            if (log.isErrorEnabled()) {
                double fileSizeMB = fileSize / (1024.0 * 1024.0);
                log.error("File size validation failed - Size: {} bytes ({} MB) exceeds limit of {} bytes (8 MB)",
                        fileSize, String.format("%.2f", fileSizeMB), MAX_FILE_SIZE);
            }
            throw new IllegalArgumentException("Uploaded file exceeds the 8MB size limit");
        }
        if (isInvalidFileType) {
            log.warn("Invalid file type uploaded. Content-Type: {}, Extension: {}", contentType, extension);
            throw new IllegalArgumentException("Uploaded file type is not supported. Only PDF and Word files are allowed.");
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

        String fileType = fs2File.getFileType();
        Fs2Document fs2Document = fs2File.getFs2Document();

        if (fs2File.getBlobName() != null && fileStorageService.fileExists(fs2File.getBlobName())) {
            fileStorageService.deleteFile(fs2File.getBlobName());
        }
        fs2FileRepository.delete(fs2File);
        log.info("Deleted file successfully");

        if (fs2Document != null && fileType != null) {
            clearBerkasFieldIfNoFilesExist(fs2Document, fileType);
        }
    }

    @Override
    @Transactional
    public void deleteFilesByFs2Id(UUID fs2Id) {
        List<Fs2File> files = fs2FileRepository.findByFs2DocumentId(fs2Id);
        for (Fs2File file : files) {
            if (file.getBlobName() != null && fileStorageService.fileExists(file.getBlobName())) {
                fileStorageService.deleteFile(file.getBlobName());
            }
        }
        fs2FileRepository.deleteByFs2DocumentId(fs2Id);
        log.info("Deleted all files for F.S.2 document successfully");
    }

    @Override
    public String getDownloadUrl(UUID fileId) {
        Fs2File fs2File = fs2FileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND_MSG + fileId));
        return fileStorageService.getFileUrl(fs2File.getBlobName());
    }

    @Override
    public byte[] downloadFile(UUID fileId) {
        Fs2File fs2File = fs2FileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND_MSG + fileId));
        return fileStorageService.downloadFile(fs2File.getBlobName());
    }

    @Override
    public Fs2FileResponse getFileById(UUID fileId) {
        Fs2File fs2File = fs2FileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND_MSG + fileId));
        return mapToResponse(fs2File);
    }

    private Fs2FileResponse mapToResponse(Fs2File file) {
        return mapToResponse(file, false);
    }

    private Fs2FileResponse mapToResponse(Fs2File file, boolean isLatest) {
        return Fs2FileResponse.builder()
                .id(file.getId())
                .fs2Id(file.getFs2Document() != null ? file.getFs2Document().getId() : null)
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
    public Fs2FileResponse uploadNewVersion(UUID fs2Id, MultipartFile file, String fileType, LocalDate tanggalDokumen) {
        Fs2Document fs2Document = fs2DocumentRepository.findById(fs2Id)
                .orElseThrow(() -> new ResourceNotFoundException("F.S.2 Document not found with id: " + fs2Id));
        checkFs2FileValidity(file);
        return uploadSingleFile(fs2Document, file, fileType, tanggalDokumen);
    }

    @Override
    public List<Fs2FileResponse> getLatestVersionFiles(UUID fs2Id) {
        List<Fs2File> latestFiles = fs2FileRepository.findLatestVersionFilesByFs2Id(fs2Id);
        return latestFiles.stream()
                .map(file -> mapToResponse(file, true))
                .toList();
    }

    @Override
    public List<Fs2FileResponse> getFileHistory(UUID fs2Id, String fileType) {
        List<Fs2File> history = fs2FileRepository.findFileHistoryByFs2IdAndFileType(fs2Id, fileType);
        if (history.isEmpty()) {
            return List.of();
        }
        return history.stream()
                .map(file -> mapToResponse(file, file.equals(history.get(0))))
                .toList();
    }

    @Override
    public List<Fs2FileResponse> getFilesByGroupId(UUID fileGroupId) {
        List<Fs2File> files = fs2FileRepository.findByFileGroupIdOrderByVersionDesc(fileGroupId);
        if (files.isEmpty()) {
            return List.of();
        }
        return files.stream()
                .map(file -> mapToResponse(file, file.equals(files.get(0))))
                .toList();
    }

    @Override
    public byte[] downloadFileVersion(UUID fs2Id, String fileType, Integer version) {
        List<Fs2File> files = fs2FileRepository.findByFs2DocumentIdAndFileTypeOrderByVersionDesc(fs2Id, fileType);
        Fs2File targetFile = files.stream()
                .filter(f -> f.getVersion().equals(version))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("File not found for FS2 %s, type %s, version %d", fs2Id, fileType, version)));
        return fileStorageService.downloadFile(targetFile.getBlobName());
    }

    private void updateBerkasField(Fs2Document fs2Document, String fileType) {
        switch (fileType) {
            case "ND" -> fs2Document.setBerkasNd("Y");
            case "FS2" -> fs2Document.setBerkasFs2("Y");
            case "CD" -> fs2Document.setBerkasCd("Y");
            case "FS2A" -> fs2Document.setBerkasFs2a("Y");
            case "FS2B" -> fs2Document.setBerkasFs2b("Y");
            case "F45" -> fs2Document.setBerkasF45("Y");
            case "F46" -> fs2Document.setBerkasF46("Y");
            case "NDBA" -> fs2Document.setBerkasNdBaDeployment("Y");
            default -> { return; }
        }
        fs2DocumentRepository.save(fs2Document);
    }

    private void clearBerkasFieldIfNoFilesExist(Fs2Document fs2Document, String fileType) {
        List<Fs2File> remainingFiles = fs2FileRepository.findByFs2DocumentIdOrderByCreatedAtDesc(fs2Document.getId())
                .stream()
                .filter(f -> fileType.equals(f.getFileType()))
                .toList();

        if (remainingFiles.isEmpty()) {
            switch (fileType) {
                case "ND" -> fs2Document.setBerkasNd(null);
                case "FS2" -> fs2Document.setBerkasFs2(null);
                case "CD" -> fs2Document.setBerkasCd(null);
                case "FS2A" -> fs2Document.setBerkasFs2a(null);
                case "FS2B" -> fs2Document.setBerkasFs2b(null);
                case "F45" -> fs2Document.setBerkasF45(null);
                case "F46" -> fs2Document.setBerkasF46(null);
                case "NDBA" -> fs2Document.setBerkasNdBaDeployment(null);
                default -> {
                    log.warn("Unknown file type for berkas clear: {}", fileType);
                    return;
                }
            }
            fs2DocumentRepository.save(fs2Document);
            log.info("Cleared berkas field for file type: {} (no files remaining)", fileType);
        }
    }
}
