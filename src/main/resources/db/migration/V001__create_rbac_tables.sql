-- RBAC Schema Migration Script
-- Create tables for Role-Based Access Control

-- 1. Create mst_role table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='mst_role' AND xtype='U')
CREATE TABLE mst_role (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    role_name NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(255),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- 2. Create trn_user_role table (Many-to-Many join table)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='trn_user_role' AND xtype='U')
CREATE TABLE trn_user_role (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    user_id UNIQUEIDENTIFIER NOT NULL,
    role_id UNIQUEIDENTIFIER NOT NULL,
    assigned_by UNIQUEIDENTIFIER,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_trn_user_role_user FOREIGN KEY (user_id) REFERENCES mst_user(uuid) ON DELETE CASCADE,
    CONSTRAINT FK_trn_user_role_role FOREIGN KEY (role_id) REFERENCES mst_role(id) ON DELETE CASCADE,
    CONSTRAINT UQ_user_role UNIQUE (user_id, role_id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_trn_user_role_user_id ON trn_user_role(user_id);
CREATE INDEX IF NOT EXISTS idx_trn_user_role_role_id ON trn_user_role(role_id);

PRINT 'RBAC tables created successfully'
