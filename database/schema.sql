-- RBSI Module Tables
-- Run this script after existing tables

-- Table: mst_rbsi (Master RBSI - Periode)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='mst_rbsi' AND xtype='U')
CREATE TABLE mst_rbsi (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    periode VARCHAR(20) NOT NULL UNIQUE,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Table: mst_program (Master Program)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='mst_program' AND xtype='U')
CREATE TABLE mst_program (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    rbsi_id UNIQUEIDENTIFIER NOT NULL,
    program_number VARCHAR(20) NOT NULL,
    name VARCHAR(500) NOT NULL,
    description TEXT,
    year_version INT NOT NULL,
    sort_order INT NOT NULL,
    status VARCHAR(20) DEFAULT 'active',
    start_date DATETIME2,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_program_rbsi FOREIGN KEY (rbsi_id) REFERENCES mst_rbsi(id)
);

-- Indexes for mst_program
CREATE INDEX idx_program_rbsi_year ON mst_program(rbsi_id, year_version);
CREATE INDEX idx_program_sort_order ON mst_program(rbsi_id, year_version, sort_order);

-- Table: mst_initiative (Master Initiative/Inisiatif)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='mst_initiative' AND xtype='U')
CREATE TABLE mst_initiative (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    program_id UNIQUEIDENTIFIER NOT NULL,
    initiative_number VARCHAR(30) NOT NULL,
    name VARCHAR(500) NOT NULL,
    description TEXT,
    year_version INT NOT NULL,
    sort_order INT NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    link_dokumen VARCHAR(500),
    tanggal_submit DATETIME2,
    pksi_relation_id UNIQUEIDENTIFIER,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_initiative_program FOREIGN KEY (program_id) REFERENCES mst_program(id) ON DELETE CASCADE
);

-- Indexes for mst_initiative
CREATE INDEX idx_initiative_program_year ON mst_initiative(program_id, year_version);
CREATE INDEX idx_initiative_sort_order ON mst_initiative(program_id, year_version, sort_order);

-- Sample Data for Testing
-- Insert sample RBSI
INSERT INTO mst_rbsi (id, periode, description, is_active)
VALUES 
    (NEWID(), '2025-2029', 'Roadmap SI OJK Periode 2025-2029', 1),
    (NEWID(), '2024-2028', 'Roadmap SI OJK Periode 2024-2028', 1);
