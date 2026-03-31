-- Create team table
CREATE TABLE mst_team (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    name NVARCHAR(255) NOT NULL,
    description NVARCHAR(1000),
    pic_uuid UNIQUEIDENTIFIER NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_team_pic FOREIGN KEY (pic_uuid) REFERENCES mst_user(uuid)
);

-- Create team member join table
CREATE TABLE mst_team_member (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    team_id UNIQUEIDENTIFIER NOT NULL,
    user_uuid UNIQUEIDENTIFIER NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_team_member_team FOREIGN KEY (team_id) REFERENCES mst_team(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_member_user FOREIGN KEY (user_uuid) REFERENCES mst_user(uuid),
    CONSTRAINT uq_team_member UNIQUE (team_id, user_uuid)
);

-- Create indexes for better query performance
CREATE INDEX idx_team_pic ON mst_team(pic_uuid);
CREATE INDEX idx_team_member_team ON mst_team_member(team_id);
CREATE INDEX idx_team_member_user ON mst_team_member(user_uuid);
