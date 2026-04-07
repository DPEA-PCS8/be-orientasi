package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.Fs2FileResponse;
import com.pcs8.orientasi.service.Fs2FileService;
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
            @RequestParam(value = "fileType", required = false, defaultValue = "FS2") String fileType) {
        
        log.info("Uploading files for F.S.2 document with fileType: {}", fileType);
        
        List<Fs2FileResponse> responses = fs2FileService.uploadFiles(fs2Id, files, fileType);
        
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
            @RequestParam(value = "fileType", required = false, defaultValue = "FS2") String fileType) {
        
        log.info("Uploading temp files for F.S.2 with fileType: {}", fileType);
        
        List<Fs2FileResponse> responses = fs2FileService.uploadTempFiles(sessionId, files, fileType);
        
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
}
