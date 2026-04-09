package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.response.Fs2FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface Fs2FileService {
    
    /**
     * Upload multiple files for a F.S.2 document
     */
    List<Fs2FileResponse> uploadFiles(UUID fs2Id, MultipartFile[] files, String fileType);
    
    /**
     * Upload files to temporary storage (before F.S.2 is created)
     */
    List<Fs2FileResponse> uploadTempFiles(String sessionId, MultipartFile[] files, String fileType);
    
    /**
     * Move temporary files to permanent storage after F.S.2 is created
     */
    List<Fs2FileResponse> moveTempFilesToPermanent(UUID fs2Id, String sessionId);
    
    /**
     * Delete temporary files by session ID
     */
    void deleteTempFiles(String sessionId);
    
    /**
     * Get all files for a F.S.2 document
     */
    List<Fs2FileResponse> getFilesByFs2Id(UUID fs2Id);
    
    /**
     * Delete a specific file
     */
    void deleteFile(UUID fileId);
    
    /**
     * Delete all files for a F.S.2 document
     */
    void deleteFilesByFs2Id(UUID fs2Id);
    
    /**
     * Get download URL for a file
     */
    String getDownloadUrl(UUID fileId);
    
    /**
     * Download file content
     */
    byte[] downloadFile(UUID fileId);
    
    /**
     * Get file metadata by ID
     */
    Fs2FileResponse getFileById(UUID fileId);

    // ==================== VERSIONING METHODS ====================

    /**
     * Upload a new version of an existing file type.
     * Automatically increments version number and generates standardized display name.
     */
    Fs2FileResponse uploadNewVersion(UUID fs2Id, MultipartFile file, String fileType);

    /**
     * Get the latest version files for a F.S.2 document (one per file type)
     */
    List<Fs2FileResponse> getLatestVersionFiles(UUID fs2Id);

    /**
     * Get file version history for a specific file type
     */
    List<Fs2FileResponse> getFileHistory(UUID fs2Id, String fileType);

    /**
     * Get file by file group ID (all versions of same logical file)
     */
    List<Fs2FileResponse> getFilesByGroupId(UUID fileGroupId);

    /**
     * Download a specific version of a file
     */
    byte[] downloadFileVersion(UUID fs2Id, String fileType, Integer version);
}
