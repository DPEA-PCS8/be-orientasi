-- Drop Pengajuan tahapan columns from F.S.2 document
-- This migration removes columns that are no longer displayed in the UI
-- and whose semantics have been folded into the Asesmen stage.

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('mst_fs2_document') AND name = 'tahapan_status_pengajuan')
BEGIN
    ALTER TABLE mst_fs2_document DROP COLUMN tahapan_status_pengajuan;
END

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('mst_fs2_document') AND name = 'tanggal_pengajuan_selesai')
BEGIN
    ALTER TABLE mst_fs2_document DROP COLUMN tanggal_pengajuan_selesai;
END
