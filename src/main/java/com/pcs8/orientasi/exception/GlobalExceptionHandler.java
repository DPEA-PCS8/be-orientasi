package com.pcs8.orientasi.exception;

import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BaseResponse> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BaseResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<BaseResponse> handleBadRequest(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseResponse> handleIllegalState(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<BaseResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("File upload size exceeded: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new BaseResponse(HttpStatus.PAYLOAD_TOO_LARGE.value(), 
                        "Ukuran file melebihi batas maksimal 8MB. Silakan pilih file yang lebih kecil.", null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("Validation error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponse(HttpStatus.BAD_REQUEST.value(), "Validation error", errors));
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());
        // Log root cause for debugging
        Throwable rootCause = ex.getMostSpecificCause();
        log.error("Root cause: {}", rootCause.getMessage());
        String userFriendlyMessage = extractConstraintViolationInfo(ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new BaseResponse(HttpStatus.CONFLICT.value(), userFriendlyMessage, null));
    }

    @ExceptionHandler(com.pcs8.orientasi.exception.DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse> handleCustomDataIntegrityViolation(com.pcs8.orientasi.exception.DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new BaseResponse(HttpStatus.CONFLICT.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        String errorMessage = "Internal server error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMessage, null));
    }

    /**
     * Extract constraint violation info dari SQL Server error message.
     * Handles FK conflicts, NOT NULL violations, UNIQUE violations, etc.
     */
    private String extractConstraintViolationInfo(DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        Throwable rootCause = ex.getMostSpecificCause();
        String rootMessage = rootCause != null ? rootCause.getMessage() : "";

        if (message == null || message.isEmpty()) {
            return "Terjadi kesalahan integritas data. Silakan cek kembali input Anda.";
        }

        String fullMessage = message + " " + rootMessage;

        // 1) NOT NULL violation
        // SQL Server: "Cannot insert the value NULL into column 'column_name', table 'db.dbo.table'; column does not allow nulls"
        Pattern notNullPattern = Pattern.compile(
            "Cannot insert the value NULL into column '([^']+)'.*?table '([^']+)'",
            Pattern.CASE_INSENSITIVE
        );
        Matcher notNullMatcher = notNullPattern.matcher(fullMessage);
        if (notNullMatcher.find()) {
            String columnName = notNullMatcher.group(1);
            return String.format("Kolom '%s' wajib diisi dan tidak boleh kosong.", columnName);
        }

        // 2) UNIQUE constraint violation
        // SQL Server: "Violation of UNIQUE KEY constraint 'UQ_...'. Cannot insert duplicate key"
        Pattern uniquePattern = Pattern.compile(
            "(?:UNIQUE KEY|unique constraint).*?'([^']+)'",
            Pattern.CASE_INSENSITIVE
        );
        Matcher uniqueMatcher = uniquePattern.matcher(fullMessage);
        if (uniqueMatcher.find()) {
            return "Data duplikat terdeteksi. Data dengan nilai yang sama sudah ada di database.";
        }

        // 3) FK constraint violation (INSERT/UPDATE - referencing non-existent FK)
        // SQL Server: "The INSERT statement conflicted with the FOREIGN KEY constraint"
        Pattern fkInsertPattern = Pattern.compile(
            "(?:INSERT|UPDATE) statement conflicted with the FOREIGN KEY constraint.*?table\\s+\"dbo\\.([^\"]+)\"",
            Pattern.CASE_INSENSITIVE
        );
        Matcher fkInsertMatcher = fkInsertPattern.matcher(fullMessage);
        if (fkInsertMatcher.find()) {
            String relatedTable = fkInsertMatcher.group(1).toLowerCase().trim();
            String friendlyName = mapTableToFriendlyName(relatedTable);
            return String.format("Data referensi %s tidak ditemukan. Pastikan data terkait sudah ada.", friendlyName);
        }

        // 4) FK constraint violation (DELETE - referenced by other data)
        Pattern fkDeletePattern = Pattern.compile(
            "DELETE statement conflicted.*?table\\s+\"dbo\\.([^\"]+)\"",
            Pattern.CASE_INSENSITIVE
        );
        Matcher fkDeleteMatcher = fkDeletePattern.matcher(fullMessage);
        if (fkDeleteMatcher.find()) {
            String relatedTable = fkDeleteMatcher.group(1).toLowerCase().trim();
            String friendlyName = mapTableToFriendlyName(relatedTable);
            return String.format(
                "Tidak dapat menghapus data ini karena masih digunakan oleh %s. " +
                "Silakan hapus atau ubah data terkait terlebih dahulu.",
                friendlyName
            );
        }

        // 5) Generic fallback - include root cause for debugging
        return "Terjadi kesalahan integritas data: " + (rootMessage.length() > 200 ? rootMessage.substring(0, 200) + "..." : rootMessage);
    }

    /**
     * Map technical table names to user-friendly display names
     */
    private String mapTableToFriendlyName(String tableName) {
        if (tableName == null) return "data lain";

        Map<String, String> tableMap = new HashMap<>();
        tableMap.put("mst_aplikasi", "Daftar Aplikasi");
        tableMap.put("mst_bidang", "Master Bidang");
        tableMap.put("mst_skpa", "Master SKPA");
        tableMap.put("trn_pksi_document", "Dokumen PKSI");
        tableMap.put("trn_fs2_document", "Dokumen FS2");
        tableMap.put("mst_fs2_document", "Dokumen FS2");
        tableMap.put("trn_aplikasi_url", "URL Aplikasi");
        tableMap.put("trn_aplikasi_satker_internal", "Satker Internal Aplikasi");
        tableMap.put("trn_aplikasi_pengguna_eksternal", "Pengguna Eksternal Aplikasi");
        tableMap.put("trn_aplikasi_komunikasi_sistem", "Komunikasi Sistem Aplikasi");
        tableMap.put("mst_arsitektur_rbsi", "Arsitektur RBSI");
        tableMap.put("rbsi_inisiatif", "Inisiatif RBSI");
        tableMap.put("rbsi_program", "Program RBSI");

        return tableMap.getOrDefault(tableName, "'" + tableName + "'");
    }
}

