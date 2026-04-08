-- Create changelog table for FS2 Document changes tracking
CREATE TABLE his_fs2_changelog (
    id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    fs2_document_id UNIQUEIDENTIFIER NOT NULL,
    field_name NVARCHAR(100) NOT NULL,
    field_label NVARCHAR(255) NOT NULL,
    old_value NVARCHAR(MAX),
    new_value NVARCHAR(MAX),
    updated_by UNIQUEIDENTIFIER NOT NULL,
    updated_by_name NVARCHAR(255) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT pk_his_fs2_changelog PRIMARY KEY (id),
    CONSTRAINT fk_his_fs2_changelog_fs2_document FOREIGN KEY (fs2_document_id) 
        REFERENCES mst_fs2_document(id) ON DELETE CASCADE,
    CONSTRAINT fk_his_fs2_changelog_updated_by FOREIGN KEY (updated_by) 
        REFERENCES mst_user(uuid)
);

-- Create index for faster lookups
CREATE INDEX idx_his_fs2_changelog_fs2_document_id ON his_fs2_changelog(fs2_document_id);
CREATE INDEX idx_his_fs2_changelog_created_at ON his_fs2_changelog(created_at DESC);
