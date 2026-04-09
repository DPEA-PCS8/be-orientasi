package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.service.FileVersioningService;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Implementation of FileVersioningService.
 * Provides common versioning logic for both PKSI and FS2 files (DRY principle).
 */
@Service
public class FileVersioningServiceImpl implements FileVersioningService {

    private static final Pattern INVALID_FILENAME_CHARS = Pattern.compile("[\\\\/:*?\"<>|\\s]+");
    private static final String REPLACEMENT_CHAR = "_";
    private static final String VERSION_PREFIX = "V";
    private static final String ND_SUFFIX = "_ND";

    @Override
    public String generateDisplayName(String fileType, String documentName, Integer version, String originalExtension) {
        String sanitizedName = sanitizeDocumentName(documentName);
        return String.format("%s_%s_%s%d%s",
                fileType,
                sanitizedName,
                VERSION_PREFIX,
                version,
                originalExtension != null ? originalExtension : "");
    }

    @Override
    public String generateNdDisplayName(String fileType, String documentName, Integer version, String originalExtension) {
        String sanitizedName = sanitizeDocumentName(documentName);
        return String.format("%s_%s_%s%d%s%s",
                fileType,
                sanitizedName,
                VERSION_PREFIX,
                version,
                ND_SUFFIX,
                originalExtension != null ? originalExtension : "");
    }

    @Override
    public String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    @Override
    public String sanitizeDocumentName(String documentName) {
        if (documentName == null || documentName.isEmpty()) {
            return "Document";
        }
        // Replace invalid characters with underscore
        String sanitized = INVALID_FILENAME_CHARS.matcher(documentName).replaceAll(REPLACEMENT_CHAR);
        // Remove consecutive underscores
        sanitized = sanitized.replaceAll("_+", "_");
        // Remove leading underscores
        sanitized = sanitized.replaceAll("^_+", "");
        // Remove trailing underscores
        sanitized = sanitized.replaceAll("_+$", "");
        // Limit length to 100 characters
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
        }
        return sanitized.isEmpty() ? "Document" : sanitized;
    }
}
