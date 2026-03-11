package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.response.PksiFileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface PksiFileService {
    
    /**
     * Upload multiple files for a PKSI document
     */
    List<PksiFileResponse> uploadFiles(UUID pksiId, MultipartFile[] files);
    
    /**
     * Get all files for a PKSI document
     */
    List<PksiFileResponse> getFilesByPksiId(UUID pksiId);
    
    /**
     * Delete a specific file
     */
    void deleteFile(UUID fileId);
    
    /**
     * Delete all files for a PKSI document
     */
    void deleteFilesByPksiId(UUID pksiId);
    
    /**
     * Get download URL for a file
     */
    String getDownloadUrl(UUID fileId);
    
    /**
     * Download file content
     */
    byte[] downloadFile(UUID fileId);
}
