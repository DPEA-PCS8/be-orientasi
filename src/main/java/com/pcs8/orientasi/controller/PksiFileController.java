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
            @RequestParam("files") MultipartFile[] files) {
        
        log.info("Uploading {} files for PKSI: {}", files.length, pksiId);
        
        List<PksiFileResponse> responses = pksiFileService.uploadFiles(pksiId, files);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Files uploaded successfully", responses));
    }

    /**
     * Get all files for a PKSI document
     */
    @GetMapping("/pksi/{pksiId}")
    public ResponseEntity<BaseResponse> getFilesByPksiId(@PathVariable UUID pksiId) {
        log.info("Getting files for PKSI: {}", pksiId);
        
        List<PksiFileResponse> files = pksiFileService.getFilesByPksiId(pksiId);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, files));
    }

    /**
     * Download a file
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID fileId) {
        log.info("Downloading file: {}", fileId);
        
        byte[] content = pksiFileService.downloadFile(fileId);
        
        // Get file info for headers - use safe defaults
        String fileName = "download";
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        
        // Try to get actual file info from service
        String downloadUrl = pksiFileService.getDownloadUrl(fileId);
        if (downloadUrl != null) {
            // Extract filename from URL if available
            int lastSlash = downloadUrl.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < downloadUrl.length() - 1) {
                fileName = downloadUrl.substring(lastSlash + 1);
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    /**
     * Delete a specific file
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<BaseResponse> deleteFile(@PathVariable UUID fileId) {
        log.info("Deleting file: {}", fileId);
        
        pksiFileService.deleteFile(fileId);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "File deleted successfully", null));
    }

    /**
     * Delete all files for a PKSI document
     */
    @DeleteMapping("/pksi/{pksiId}")
    public ResponseEntity<BaseResponse> deleteFilesByPksiId(@PathVariable UUID pksiId) {
        log.info("Deleting all files for PKSI: {}", pksiId);
        
        pksiFileService.deleteFilesByPksiId(pksiId);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "All files deleted successfully", null));
    }
}
