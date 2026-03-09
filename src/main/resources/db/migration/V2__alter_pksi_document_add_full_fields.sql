-- Migration script to add full PKSI document fields
-- Version: 2
-- Description: Add all fields to support frontend form

-- Helper procedure to add column if not exists
IF OBJECT_ID('dbo.sp_add_column_if_not_exists', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_add_column_if_not_exists;
GO

CREATE PROCEDURE dbo.sp_add_column_if_not_exists
    @table_name NVARCHAR(128),
    @column_name NVARCHAR(128),
    @column_definition NVARCHAR(256)
AS
BEGIN
    IF NOT EXISTS (
        SELECT * FROM sys.columns 
        WHERE object_id = OBJECT_ID(@table_name) 
        AND name = @column_name
    )
    BEGIN
        DECLARE @sql NVARCHAR(MAX) = 'ALTER TABLE ' + @table_name + ' ADD ' + @column_name + ' ' + @column_definition;
        EXEC sp_executesql @sql;
    END
END
GO

-- Add all new PKSI document columns
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'tanggal_pengajuan', 'DATE NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'mengapa_pksi_diperlukan', 'NVARCHAR(MAX) NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'kegunaan_pksi', 'NVARCHAR(MAX) NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'target_pksi', 'NVARCHAR(MAX) NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'batasan_pksi', 'NVARCHAR(MAX) NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'hubungan_sistem_lain', 'NVARCHAR(MAX) NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'asumsi', 'NVARCHAR(MAX) NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'batasan_desain', 'NVARCHAR(MAX) NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'risiko_bisnis', 'NVARCHAR(MAX) NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'risiko_sukses_pksi', 'NVARCHAR(MAX) NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'pengendalian_risiko', 'NVARCHAR(MAX) NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'informasi_yang_dikelola', 'NVARCHAR(MAX) NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'dasar_peraturan', 'NVARCHAR(MAX) NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'tahap1_awal', 'DATE NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'tahap1_akhir', 'DATE NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'tahap5_awal', 'DATE NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'tahap5_akhir', 'DATE NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'tahap7_awal', 'DATE NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'tahap7_akhir', 'DATE NULL';
EXEC dbo.sp_add_column_if_not_exists 'trn_pksi_document', 'rencana_pengelolaan', 'NVARCHAR(MAX) NULL';
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

-- Cleanup helper procedure
DROP PROCEDURE dbo.sp_add_column_if_not_exists;
GO

PRINT 'Migration V2: PKSI document full fields added successfully';
