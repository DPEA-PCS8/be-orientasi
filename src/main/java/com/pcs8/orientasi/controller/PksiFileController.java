package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.PksiFileResponse;
import com.pcs8.orientasi.service.PksiFileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pksi/files")
@RequiredArgsConstructor
@RequiresRole({"Admin", "Pengembang", "Satker"})
public class PksiFileController {

    private static final Logger log = LoggerFactory.getLogger(PksiFileController.class);
    private static final String SUCCESS_MESSAGE = "Success";

    private final PksiFileService pksiFileService;

    /**
     * Upload files for a PKSI document
     */
    @PostMapping("/upload/{pksiId}")
    public ResponseEntity<BaseResponse> uploadFiles(
            @PathVariable UUID pksiId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "fileType", required = false, defaultValue = "T01") String fileType) {
        
        log.info("Uploading files for PKSI document");
        
        List<PksiFileResponse> responses = pksiFileService.uploadFiles(pksiId, files, fileType);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Files uploaded successfully", responses));
    }

    /**
     * Upload files to temporary storage (before PKSI is created)
     */
    @PostMapping("/temp/upload/{sessionId}")
    public ResponseEntity<BaseResponse> uploadTempFiles(
            @PathVariable String sessionId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "fileType", required = false, defaultValue = "T01") String fileType) {
        
        log.info("Uploading temp files for session");
        
        List<PksiFileResponse> responses = pksiFileService.uploadTempFiles(sessionId, files, fileType);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Temp files uploaded successfully", responses));
    }

    /**
     * Move temporary files to permanent storage after PKSI is created
     */
    @PostMapping("/temp/move/{pksiId}/{sessionId}")
    public ResponseEntity<BaseResponse> moveTempFiles(
            @PathVariable UUID pksiId,
            @PathVariable String sessionId) {
        
        log.info("Moving temp files to permanent storage");
        
        List<PksiFileResponse> responses = pksiFileService.moveTempFilesToPermanent(pksiId, sessionId);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Files moved successfully", responses));
    }

    /**
     * Delete temporary files by session ID
     */
    @DeleteMapping("/temp/{sessionId}")
    public ResponseEntity<BaseResponse> deleteTempFiles(@PathVariable String sessionId) {
        log.info("Deleting temp files");
        
        pksiFileService.deleteTempFiles(sessionId);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Temp files deleted successfully", null));
    }

    /**
     * Get all files for a PKSI document
     */
    @GetMapping("/pksi/{pksiId}")
    public ResponseEntity<BaseResponse> getFilesByPksiId(@PathVariable UUID pksiId) {
        log.info("Getting files for PKSI document");
        
        List<PksiFileResponse> files = pksiFileService.getFilesByPksiId(pksiId);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, files));
    }

    /**
     * Get file metadata by ID
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<BaseResponse> getFileById(@PathVariable UUID fileId) {
        log.info("Getting file metadata");
        
        PksiFileResponse file = pksiFileService.getFileById(fileId);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, file));
    }

    /**
     * Download a file
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID fileId) {
        log.info("Downloading file");
        
        byte[] content = pksiFileService.downloadFile(fileId);
        PksiFileResponse fileInfo = pksiFileService.getFileById(fileId);
        
        String fileName = fileInfo.getOriginalName() != null ? fileInfo.getOriginalName() : "download";
        String contentType = fileInfo.getContentType() != null ? fileInfo.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    /**
     * Preview a file inline (for PDF, images, etc.)
     */
    @GetMapping("/preview/{fileId}")
    public ResponseEntity<byte[]> previewFile(@PathVariable UUID fileId) {
        log.info("Previewing file");
        
        byte[] content = pksiFileService.downloadFile(fileId);
        PksiFileResponse fileInfo = pksiFileService.getFileById(fileId);
        
        String contentType = fileInfo.getContentType() != null ? fileInfo.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline");
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    /**
     * Delete a specific file
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<BaseResponse> deleteFile(@PathVariable UUID fileId) {
        log.info("Deleting file");
        
        pksiFileService.deleteFile(fileId);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "File deleted successfully", null));
    }

    /**
     * Delete all files for a PKSI document
     */
    @DeleteMapping("/pksi/{pksiId}")
    public ResponseEntity<BaseResponse> deleteFilesByPksiId(@PathVariable UUID pksiId) {
        log.info("Deleting all files for PKSI document");
        
        pksiFileService.deleteFilesByPksiId(pksiId);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "All files deleted successfully", null));
    }
}
