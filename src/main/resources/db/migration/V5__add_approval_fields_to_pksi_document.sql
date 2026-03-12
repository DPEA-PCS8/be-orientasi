-- Add approval fields to trn_pksi_document table
-- These fields are populated when PKSI status is changed to DISETUJUI

ALTER TABLE trn_pksi_document
ADD iku NVARCHAR(10) NULL;

ALTER TABLE trn_pksi_document
ADD inhouse_outsource NVARCHAR(20) NULL;

ALTER TABLE trn_pksi_document
ADD pic_approval NVARCHAR(255) NULL;

ALTER TABLE trn_pksi_document
ADD anggota_tim NVARCHAR(MAX) NULL;
