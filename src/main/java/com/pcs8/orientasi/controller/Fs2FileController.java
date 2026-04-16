package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.Fs2FileResponse;
import com.pcs8.orientasi.service.Fs2FileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/fs2/files")
@RequiredArgsConstructor
@RequiresRole({"admin", "pengembang", "skpa"})
public class Fs2FileController {

    private static final Logger log = LoggerFactory.getLogger(Fs2FileController.class);
    private static final String SUCCESS_MESSAGE = "Success";

    private final Fs2FileService fs2FileService;

    /**
     * Upload files for a F.S.2 document
     */
    @PostMapping("/upload/{fs2Id}")
    public ResponseEntity<BaseResponse> uploadFiles(
            @PathVariable UUID fs2Id,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "fileType", required = false, defaultValue = "FS2") String fileType,
            @RequestParam(value = "tanggal_dokumen", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tanggalDokumen) {
        
        
        List<Fs2FileResponse> responses = fs2FileService.uploadFiles(fs2Id, files, fileType, tanggalDokumen);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Files uploaded successfully", responses));
    }

    /**
     * Upload files to temporary storage (before F.S.2 is created)
     */
    @PostMapping("/temp/upload/{sessionId}")
    public ResponseEntity<BaseResponse> uploadTempFiles(
            @PathVariable String sessionId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "fileType", required = false, defaultValue = "FS2") String fileType,
            @RequestParam(value = "tanggal_dokumen", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tanggalDokumen) {
        
        
        List<Fs2FileResponse> responses = fs2FileService.uploadTempFiles(sessionId, files, fileType, tanggalDokumen);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Temp files uploaded successfully", responses));
    }

    /**
     * Move temporary files to permanent storage after F.S.2 is created
     */
    @PostMapping("/temp/move/{fs2Id}/{sessionId}")
    public ResponseEntity<BaseResponse> moveTempFiles(
            @PathVariable UUID fs2Id,
            @PathVariable String sessionId) {
        
        log.info("Moving temp files to permanent storage for F.S.2");
        
        List<Fs2FileResponse> responses = fs2FileService.moveTempFilesToPermanent(fs2Id, sessionId);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Files moved successfully", responses));
    }

    /**
     * Delete temporary files by session ID
     */
    @DeleteMapping("/temp/{sessionId}")
    public ResponseEntity<BaseResponse> deleteTempFiles(@PathVariable String sessionId) {
        log.info("Deleting temp files for F.S.2");
        
        fs2FileService.deleteTempFiles(sessionId);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Temp files deleted successfully", null));
    }

    /**
     * Get all files for a F.S.2 document
     */
    @GetMapping("/fs2/{fs2Id}")
    public ResponseEntity<BaseResponse> getFilesByFs2Id(@PathVariable UUID fs2Id) {
        log.info("Getting files for F.S.2 document");
        
        List<Fs2FileResponse> files = fs2FileService.getFilesByFs2Id(fs2Id);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, files));
    }

    /**
     * Get file by ID
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<BaseResponse> getFileById(@PathVariable UUID fileId) {
        log.info("Getting file by ID");
        
        Fs2FileResponse file = fs2FileService.getFileById(fileId);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, file));
    }

    /**
     * Download file
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID fileId) {
        log.info("Downloading file");
        
        Fs2FileResponse fileInfo = fs2FileService.getFileById(fileId);
        byte[] fileContent = fs2FileService.downloadFile(fileId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(fileInfo.getContentType()));
        headers.setContentDispositionFormData("attachment", fileInfo.getOriginalName());
        headers.setContentLength(fileContent.length);
        
        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }

    /**
     * Delete a specific file
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<BaseResponse> deleteFile(@PathVariable UUID fileId) {
        log.info("Deleting file");
        
        fs2FileService.deleteFile(fileId);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "File deleted successfully", null));
    }

    /**
     * Delete all files for a F.S.2 document
     */
    @DeleteMapping("/fs2/{fs2Id}")
    public ResponseEntity<BaseResponse> deleteFilesByFs2Id(@PathVariable UUID fs2Id) {
        log.info("Deleting all files for F.S.2 document");
        
        fs2FileService.deleteFilesByFs2Id(fs2Id);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Files deleted successfully", null));
    }

    // ==================== VERSIONING ENDPOINTS ====================

    /**
     * Upload a new version of an existing file type
     */
    @PostMapping("/version/{fs2Id}")
    public ResponseEntity<BaseResponse> uploadNewVersion(
            @PathVariable UUID fs2Id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileType") String fileType) {
        
        log.info("Uploading new version of file F.S.2 document");
        
        Fs2FileResponse response = fs2FileService.uploadNewVersion(fs2Id, file, fileType);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "New version uploaded successfully", response));
    }

    /**
     * Get the latest version files for a F.S.2 document (one per file type)
     */
    @GetMapping("/latest/{fs2Id}")
    public ResponseEntity<BaseResponse> getLatestVersionFiles(@PathVariable UUID fs2Id) {
        log.info("Getting latest version files for F.S.2 document");
        
        List<Fs2FileResponse> files = fs2FileService.getLatestVersionFiles(fs2Id);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, files));
    }

    /**
     * Get file version history for a specific file type
     */
    @GetMapping("/history/{fs2Id}/{fileType}")
    public ResponseEntity<BaseResponse> getFileHistory(
            @PathVariable UUID fs2Id,
            @PathVariable String fileType) {
        
        log.info("Getting file history for F.S.2 document");
        
        List<Fs2FileResponse> history = fs2FileService.getFileHistory(fs2Id, fileType);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, history));
    }

    /**
     * Get all versions of a file by file group ID
     */
    @GetMapping("/group/{fileGroupId}")
    public ResponseEntity<BaseResponse> getFilesByGroupId(@PathVariable UUID fileGroupId) {
        log.info("Getting files by group ID");
        
        List<Fs2FileResponse> files = fs2FileService.getFilesByGroupId(fileGroupId);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, files));
    }

    /**
     * Download a specific version of a file
     */
    @GetMapping("/download/{fs2Id}/{fileType}/{version}")
    public ResponseEntity<byte[]> downloadFileVersion(
            @PathVariable UUID fs2Id,
            @PathVariable String fileType,
            @PathVariable Integer version) {
        
        log.info("Downloading file version for F.S.2 document");
        
        byte[] content = fs2FileService.downloadFileVersion(fs2Id, fileType, version);
        List<Fs2FileResponse> history = fs2FileService.getFileHistory(fs2Id, fileType);
        Fs2FileResponse fileInfo = history.stream()
                .filter(f -> f.getVersion().equals(version))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("File not found"));

        String fileName = fileInfo.getDisplayName() != null ? fileInfo.getDisplayName() : fileInfo.getOriginalName();
        String contentType = fileInfo.getContentType() != null ? fileInfo.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}
