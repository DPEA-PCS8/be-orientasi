-- Add tahapan completion date fields to mst_fs2_document table
ALTER TABLE mst_fs2_document
ADD tanggal_pengajuan_selesai DATE NULL,
ADD tanggal_asesmen DATE NULL,
ADD tanggal_pemrograman DATE NULL,
ADD tanggal_pengujian_selesai DATE NULL,
ADD tanggal_deployment_selesai DATE NULL,
ADD tanggal_go_live DATE NULL;

-- Add tahapan status fields to mst_fs2_document table
ALTER TABLE mst_fs2_document
ADD tahapan_status_pengajuan VARCHAR(50) NULL,
ADD tahapan_status_asesmen VARCHAR(50) NULL,
ADD tahapan_status_pemrograman VARCHAR(50) NULL,
ADD tahapan_status_pengujian VARCHAR(50) NULL,
ADD tahapan_status_deployment VARCHAR(50) NULL,
ADD tahapan_status_go_live VARCHAR(50) NULL;
