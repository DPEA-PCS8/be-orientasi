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
        log.warn("Data integrity violation: {}", ex.getMessage());
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
     * Extract constraint violation info dari SQL Server error message
     * SQL Server format: The DELETE, INSERT, UPDATE statement conflicted with a FOREIGN KEY constraint
     * "FK_trn_pksi_document_mst_aplikasi". The conflict occurred in database "dbname", table "dbo.mst_aplikasi", column 'id'.
     */
    private String extractConstraintViolationInfo(DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            return "Tidak dapat menghapus data karena masih memiliki relasi dengan data lain.";
        }

        // Try to extract table name from SQL Server error message
        // Pattern: table "dbo.table_name"
        Pattern tablePattern = Pattern.compile("table\\s+\"dbo\\.([^\"]+)\"", Pattern.CASE_INSENSITIVE);
        Matcher tableMatcher = tablePattern.matcher(message);

        String relatedTable = null;
        if (tableMatcher.find()) {
            relatedTable = tableMatcher.group(1).toLowerCase().trim();
        }

        // Map table names to user-friendly names
        String friendlyTableName = mapTableToFriendlyName(relatedTable);

        return String.format(
            "Tidak dapat menghapus data ini karena masih digunakan oleh %s. " +
            "Silakan hapus atau ubah data terkait terlebih dahulu.",
            friendlyTableName
        );
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

