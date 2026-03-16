package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.domain.dto.MinioUploadResponse;
import com.pcs8.orientasi.exception.MinioOperationException;
import com.pcs8.orientasi.service.MinioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/minio")
public class MinioController {

    private static final Logger log = LoggerFactory.getLogger(MinioController.class);
    
    private final MinioService minioService;

    public MinioController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/upload")
    public ResponseEntity<MinioUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "path", required = false, defaultValue = "") String path) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        String fileUrl = minioService.uploadFile(file, path);
        String fullPath = path.isEmpty() ? file.getOriginalFilename() 
                : (path.endsWith("/") ? path : path + "/") + file.getOriginalFilename();
        
        MinioUploadResponse response = MinioUploadResponse.builder()
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .filePath(fullPath)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/file")
    public ResponseEntity<Map<String, String>> getFileUrl(@RequestParam("path") String path) {
        if (!minioService.fileExists(path)) {
            return ResponseEntity.notFound().build();
        }
        
        String fileUrl = minioService.getFileUrl(path);
        return ResponseEntity.ok(Map.of("file_url", fileUrl));
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam("path") String path) {
        if (!minioService.fileExists(path)) {
            return ResponseEntity.notFound().build();
        }
        
        InputStream inputStream = minioService.downloadFile(path);
        String filename = path.substring(path.lastIndexOf("/") + 1);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    @DeleteMapping("/file")
    public ResponseEntity<Map<String, String>> deleteFile(@RequestParam("path") String path) {
        if (!minioService.fileExists(path)) {
            return ResponseEntity.notFound().build();
        }
        
        minioService.deleteFile(path);
        return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
    }

    @ExceptionHandler(MinioOperationException.class)
    public ResponseEntity<Map<String, String>> handleMinioException(MinioOperationException e) {
        log.error("Minio operation error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
    }
}
