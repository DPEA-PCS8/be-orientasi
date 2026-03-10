-- Migration to add aplikasi_id foreign key to trn_pksi_document table
-- This allows PKSI documents to reference specific applications

ALTER TABLE trn_pksi_document
ADD aplikasi_id UNIQUEIDENTIFIER NULL;

-- Add foreign key constraint
ALTER TABLE trn_pksi_document
ADD CONSTRAINT FK_pksi_document_aplikasi
FOREIGN KEY (aplikasi_id) REFERENCES mst_aplikasi(id);

-- Create index for better query performance
CREATE INDEX IX_pksi_document_aplikasi_id ON trn_pksi_document(aplikasi_id);
