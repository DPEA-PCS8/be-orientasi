package com.pcs8.orientasi.constant;

/**
 * Global constants untuk aplikasi Orientasi.
 * Menyimpan semua konstanta yang digunakan di seluruh aplikasi untuk menghindari hardcoding.
 */
public final class ConstantVariable {

    private ConstantVariable() {
        // Private constructor untuk mencegah instantiation
    }

    // ==================== API RESPONSE MESSAGES ====================
    /**
     * Pesan sukses standar untuk API response
     */
    public static final String SUCCESS_MESSAGE = "Success";

    // ==================== DATABASE FIELD NAMES ====================
    /**
     * Nama field untuk sorting berdasarkan tanggal pembuatan (createdAt)
     */
    public static final String CREATED_AT_FIELD = "createdAt";

    // ==================== ERROR/LOG MESSAGES ====================
    /**
     * Error message ketika gagal membuat audit log
     */
    public static final String AUDIT_LOG_CREATION_FAILED = "Failed to create audit log for {} {}: {}";

    public static final String SNAPSHOT_NOT_FOUND = "Snapshot tidak ditemukan";
}
