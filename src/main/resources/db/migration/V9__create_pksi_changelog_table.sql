-- Create table for tracking PKSI document changes
CREATE TABLE his_pksi_changelog (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    pksi_document_id UNIQUEIDENTIFIER NOT NULL,
    field_name NVARCHAR(100) NOT NULL,
    field_label NVARCHAR(255) NOT NULL,
    old_value NVARCHAR(MAX) NULL,
    new_value NVARCHAR(MAX) NULL,
    updated_by UNIQUEIDENTIFIER NOT NULL,
    updated_by_name NVARCHAR(255) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    
    CONSTRAINT fk_pksi_changelog_document FOREIGN KEY (pksi_document_id) 
        REFERENCES trn_pksi_document(id) ON DELETE CASCADE,
    CONSTRAINT fk_pksi_changelog_user FOREIGN KEY (updated_by) 
        REFERENCES mst_user(id) ON DELETE NO ACTION
);

-- Create index for faster lookups by pksi_document_id
CREATE INDEX idx_pksi_changelog_document_id ON his_pksi_changelog(pksi_document_id);

-- Create index for faster lookups by date
CREATE INDEX idx_pksi_changelog_created_at ON his_pksi_changelog(created_at DESC);
