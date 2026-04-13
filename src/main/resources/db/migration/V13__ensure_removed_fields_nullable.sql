-- V13: Ensure all removed/optional PKSI form fields allow NULL values
-- Fixes columns that may still have NOT NULL constraint due to V2 migration batch separator issues

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'deskripsi_pksi', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN deskripsi_pksi NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'mengapa_pksi_diperlukan', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN mengapa_pksi_diperlukan NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'kapan_diselesaikan', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN kapan_diselesaikan NVARCHAR(500) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'pic_satker', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN pic_satker NVARCHAR(255) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'kegunaan_pksi', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN kegunaan_pksi NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'tujuan_pksi', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN tujuan_pksi NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'target_pksi', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN target_pksi NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'ruang_lingkup', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN ruang_lingkup NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'batasan_pksi', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN batasan_pksi NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'hubungan_sistem_lain', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN hubungan_sistem_lain NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'asumsi', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN asumsi NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'batasan_desain', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN batasan_desain NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'risiko_bisnis', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN risiko_bisnis NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'risiko_sukses_pksi', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN risiko_sukses_pksi NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'pengendalian_risiko', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN pengendalian_risiko NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'pengelola_aplikasi', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN pengelola_aplikasi NVARCHAR(255) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'pengguna_aplikasi', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN pengguna_aplikasi NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'program_inisiatif_rbsi', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN program_inisiatif_rbsi NVARCHAR(255) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'fungsi_aplikasi', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN fungsi_aplikasi NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'informasi_yang_dikelola', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN informasi_yang_dikelola NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'dasar_peraturan', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN dasar_peraturan NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'rencana_pengelolaan', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN rencana_pengelolaan NVARCHAR(MAX) NULL;

IF COLUMNPROPERTY(OBJECT_ID('trn_pksi_document'), 'tujuan_pengajuan', 'AllowsNull') = 0
    ALTER TABLE trn_pksi_document ALTER COLUMN tujuan_pengajuan NVARCHAR(MAX) NULL;

PRINT 'Migration V13: All removed form fields are now nullable.';
