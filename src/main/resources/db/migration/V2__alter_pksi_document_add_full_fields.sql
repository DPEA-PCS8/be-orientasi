-- Migration script to add full PKSI document fields
-- Version: 2
-- Description: Add all fields to support frontend form

-- Add tanggal_pengajuan column
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'tanggal_pengajuan')
BEGIN
    ALTER TABLE trn_pksi_document ADD tanggal_pengajuan DATE NULL;
END
GO

-- Add mengapa_pksi_diperlukan column
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'mengapa_pksi_diperlukan')
BEGIN
    ALTER TABLE trn_pksi_document ADD mengapa_pksi_diperlukan NVARCHAR(MAX) NULL;
END
GO

-- Section 2: Tujuan dan Kegunaan
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'kegunaan_pksi')
BEGIN
    ALTER TABLE trn_pksi_document ADD kegunaan_pksi NVARCHAR(MAX) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'target_pksi')
BEGIN
    ALTER TABLE trn_pksi_document ADD target_pksi NVARCHAR(MAX) NULL;
END
GO

-- Section 3: Cakupan
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'batasan_pksi')
BEGIN
    ALTER TABLE trn_pksi_document ADD batasan_pksi NVARCHAR(MAX) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'hubungan_sistem_lain')
BEGIN
    ALTER TABLE trn_pksi_document ADD hubungan_sistem_lain NVARCHAR(MAX) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'asumsi')
BEGIN
    ALTER TABLE trn_pksi_document ADD asumsi NVARCHAR(MAX) NULL;
END
GO

-- Section 4: Risiko dan Batasan
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'batasan_desain')
BEGIN
    ALTER TABLE trn_pksi_document ADD batasan_desain NVARCHAR(MAX) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'risiko_bisnis')
BEGIN
    ALTER TABLE trn_pksi_document ADD risiko_bisnis NVARCHAR(MAX) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'risiko_sukses_pksi')
BEGIN
    ALTER TABLE trn_pksi_document ADD risiko_sukses_pksi NVARCHAR(MAX) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'pengendalian_risiko')
BEGIN
    ALTER TABLE trn_pksi_document ADD pengendalian_risiko NVARCHAR(MAX) NULL;
END
GO

-- Section 5: Gambaran Umum Aplikasi (additional fields)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'informasi_yang_dikelola')
BEGIN
    ALTER TABLE trn_pksi_document ADD informasi_yang_dikelola NVARCHAR(MAX) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'dasar_peraturan')
BEGIN
    ALTER TABLE trn_pksi_document ADD dasar_peraturan NVARCHAR(MAX) NULL;
END
GO

-- Section 6: Usulan Jadwal Pelaksanaan
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'tahap1_awal')
BEGIN
    ALTER TABLE trn_pksi_document ADD tahap1_awal DATE NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'tahap1_akhir')
BEGIN
    ALTER TABLE trn_pksi_document ADD tahap1_akhir DATE NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'tahap5_awal')
BEGIN
    ALTER TABLE trn_pksi_document ADD tahap5_awal DATE NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'tahap5_akhir')
BEGIN
    ALTER TABLE trn_pksi_document ADD tahap5_akhir DATE NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'tahap7_awal')
BEGIN
    ALTER TABLE trn_pksi_document ADD tahap7_awal DATE NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'tahap7_akhir')
BEGIN
    ALTER TABLE trn_pksi_document ADD tahap7_akhir DATE NULL;
END
GO

-- Section 7: Rencana Pengelolaan
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'trn_pksi_document') AND name = 'rencana_pengelolaan')
BEGIN
    ALTER TABLE trn_pksi_document ADD rencana_pengelolaan NVARCHAR(MAX) NULL;
END
GO

-- Make existing required columns nullable for backward compatibility
ALTER TABLE trn_pksi_document ALTER COLUMN deskripsi_pksi NVARCHAR(MAX) NULL;
ALTER TABLE trn_pksi_document ALTER COLUMN tujuan_pengajuan NVARCHAR(MAX) NULL;
ALTER TABLE trn_pksi_document ALTER COLUMN kapan_diselesaikan NVARCHAR(500) NULL;
ALTER TABLE trn_pksi_document ALTER COLUMN pic_satker NVARCHAR(255) NULL;
ALTER TABLE trn_pksi_document ALTER COLUMN tujuan_pksi NVARCHAR(MAX) NULL;
ALTER TABLE trn_pksi_document ALTER COLUMN ruang_lingkup NVARCHAR(MAX) NULL;
ALTER TABLE trn_pksi_document ALTER COLUMN pengelola_aplikasi NVARCHAR(255) NULL;
ALTER TABLE trn_pksi_document ALTER COLUMN pengguna_aplikasi NVARCHAR(MAX) NULL;
ALTER TABLE trn_pksi_document ALTER COLUMN program_inisiatif_rbsi NVARCHAR(255) NULL;
ALTER TABLE trn_pksi_document ALTER COLUMN fungsi_aplikasi NVARCHAR(MAX) NULL;
GO

PRINT 'Migration V2: PKSI document full fields added successfully';
