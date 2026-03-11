-- Add session_id column to trn_pksi_file for temporary file storage
-- This allows files to be uploaded before PKSI is created

-- Make pksi_id nullable (for temp files)
ALTER TABLE trn_pksi_file ALTER COLUMN pksi_id DROP NOT NULL;

-- Add session_id column
ALTER TABLE trn_pksi_file ADD COLUMN IF NOT EXISTS session_id VARCHAR(100);

-- Add index for session_id lookups
CREATE INDEX IF NOT EXISTS idx_pksi_file_session_id ON trn_pksi_file(session_id);
