-- Add team_id column to trn_pksi_document for linking to mst_team
ALTER TABLE trn_pksi_document ADD team_id UNIQUEIDENTIFIER NULL;

-- Add foreign key constraint
ALTER TABLE trn_pksi_document ADD CONSTRAINT FK_pksi_document_team 
    FOREIGN KEY (team_id) REFERENCES mst_team(id);

-- Create index for better query performance
CREATE INDEX IX_pksi_document_team_id ON trn_pksi_document(team_id);
