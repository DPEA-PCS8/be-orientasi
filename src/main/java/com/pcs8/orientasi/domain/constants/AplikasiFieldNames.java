package com.pcs8.orientasi.domain.constants;

/**
 * Constants for field names used across Aplikasi entities and DTOs
 * to avoid string literal duplication
 */
public final class AplikasiFieldNames {

    private AplikasiFieldNames() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Common field names
    public static final String ID = "id";
    public static final String APLIKASI_ID = "aplikasi_id";
    public static final String SNAPSHOT_ID = "snapshot_id";
    public static final String TAHUN = "tahun";

    // Aplikasi basic info
    public static final String KODE_APLIKASI = "kode_aplikasi";
    public static final String NAMA_APLIKASI = "nama_aplikasi";
    public static final String DESKRIPSI = "deskripsi";
    public static final String STATUS_APLIKASI = "status_aplikasi";
    public static final String TANGGAL_STATUS = "tanggal_status";

    // Bidang fields
    public static final String BIDANG_ID = "bidang_id";
    public static final String BIDANG_KODE = "bidang_kode";
    public static final String BIDANG_NAMA = "bidang_nama";

    // SKPA fields
    public static final String SKPA_ID = "skpa_id";
    public static final String SKPA_KODE = "skpa_kode";
    public static final String SKPA_NAMA = "skpa_nama";

    // Implementation fields
    public static final String TANGGAL_IMPLEMENTASI = "tanggal_implementasi";
    public static final String AKSES = "akses";
    public static final String PROSES_DATA_PRIBADI = "proses_data_pribadi";
    public static final String DATA_PRIBADI_DIPROSES = "data_pribadi_diproses";

    // Idle fields
    public static final String KATEGORI_IDLE = "kategori_idle";
    public static final String ALASAN_IDLE = "alasan_idle";
    public static final String RENCANA_PENGAKHIRAN = "rencana_pengakhiran";
    public static final String ALASAN_BELUM_DIAKHIRI = "alasan_belum_diakhiri";

    // Snapshot metadata
    public static final String SNAPSHOT_DATE = "snapshot_date";
    public static final String SNAPSHOT_TYPE = "snapshot_type";
    public static final String KETERANGAN_HISTORIS = "keterangan_historis";

    // Komunikasi Sistem fields
    public static final String NAMA_SISTEM = "nama_sistem";
    public static final String TIPE_SISTEM = "tipe_sistem";
    public static final String DESKRIPSI_KOMUNIKASI = "deskripsi_komunikasi";
    public static final String KETERANGAN = "keterangan";
    public static final String IS_PLANNED = "is_planned";

    // Nested objects
    public static final String BIDANG = "bidang";
    public static final String SKPA = "skpa";
    public static final String IDLE_INFO = "idle_info";
    public static final String URLS = "urls";
    public static final String SATKER_INTERNALS = "satker_internals";
    public static final String PENGGUNA_EKSTERNALS = "pengguna_eksternals";
    public static final String KOMUNIKASI_SISTEMS = "komunikasi_sistems";
    public static final String PENGHARGAANS = "penghargaans";
    public static final String CHANGELOGS = "changelogs";

    // Timestamps
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
}
