package com.pcs8.orientasi.service;

/**
 * Service interface for file versioning operations.
 * Provides common versioning logic for both PKSI and FS2 files (DRY principle).
 */
public interface FileVersioningService {

    /**
     * Generate a standardized display name for a file.
     * Format: [FileType]_[DocumentName]_V[Version].ext
     * Example: T01_PKSIA_V1.xlsx, FS2_MyDocument_V2.pdf
     *
     * @param fileType the file type (T01, T11, FS2, ND, etc.)
     * @param documentName the name of the parent document (PKSI name or FS2 name)
     * @param version the version number
     * @param originalExtension the original file extension (including dot)
     * @return the standardized display name
     */
    String generateDisplayName(String fileType, String documentName, Integer version, String originalExtension);

    /**
     * Generate a standardized display name for ND (Nota Dinas) files.
     * Format: [FileType]_[DocumentName]_V[Version]_ND.ext
     * Example: T01_PKSIA_V1_ND.docx, FS2_MyDocument_V2_ND.pdf
     *
     * @param fileType the parent file type (T01, T11, FS2, etc.)
     * @param documentName the name of the parent document
     * @param version the version number
     * @param originalExtension the original file extension (including dot)
     * @return the standardized display name for ND file
     */
    String generateNdDisplayName(String fileType, String documentName, Integer version, String originalExtension);

    /**
     * Extract file extension from a filename.
     *
     * @param fileName the original filename
     * @return the extension including the dot, or empty string if no extension
     */
    String extractExtension(String fileName);

    /**
     * Sanitize a document name for use in a filename.
     * Removes or replaces invalid characters.
     *
     * @param documentName the original document name
     * @return the sanitized name safe for use in filenames
     */
    String sanitizeDocumentName(String documentName);
}
