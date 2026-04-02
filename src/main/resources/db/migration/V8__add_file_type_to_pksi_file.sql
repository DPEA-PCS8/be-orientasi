-- Add file_type column to trn_pksi_file table
-- T01 = Rencana PKSI (T.0.1/T.0.2)
-- T11 = Spesifikasi Kebutuhan (T.1.1)

ALTER TABLE trn_pksi_file
ADD COLUMN file_type VARCHAR(20) DEFAULT 'T01';

-- Update existing records to have T01 as default
UPDATE trn_pksi_file SET file_type = 'T01' WHERE file_type IS NULL;
