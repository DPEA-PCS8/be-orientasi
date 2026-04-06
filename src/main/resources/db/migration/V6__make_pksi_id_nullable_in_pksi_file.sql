-- Make pksi_id nullable in trn_pksi_file table to support temporary file uploads
ALTER TABLE trn_pksi_file ALTER COLUMN pksi_id NVARCHAR(36) NULL;
