-- RBSI Module Tables

-- Table: mst_rbsi (Master RBSI - Periode)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'mst_rbsi')
BEGIN
    CREATE TABLE mst_rbsi (
        id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        periode NVARCHAR(50) NOT NULL,
        is_active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2,
        created_by NVARCHAR(100), 
        updated_by NVARCHAR(100),
        CONSTRAINT UK_mst_rbsi_periode UNIQUE (periode)
    );
    
    CREATE INDEX IX_mst_rbsi_periode ON mst_rbsi(periode);
    CREATE INDEX IX_mst_rbsi_is_active ON mst_rbsi(is_active);
END;
GO

-- Table: mst_program (Master Program)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'mst_program')
BEGIN
    CREATE TABLE mst_program (
        id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        rbsi_id UNIQUEIDENTIFIER NOT NULL,
        program_number NVARCHAR(20) NOT NULL,
        sequence_order INT NOT NULL,
        name NVARCHAR(500) NOT NULL,
        description NVARCHAR(1000),
        year_version INT NOT NULL,
        start_date DATETIME2,
        status NVARCHAR(50) DEFAULT 'active',
        is_active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2,
        created_by NVARCHAR(100),
        updated_by NVARCHAR(100),
        CONSTRAINT FK_mst_program_rbsi FOREIGN KEY (rbsi_id) REFERENCES mst_rbsi(id)
    );
    
    CREATE INDEX IX_mst_program_rbsi_id ON mst_program(rbsi_id);
    CREATE INDEX IX_mst_program_year_version ON mst_program(year_version);
    CREATE INDEX IX_mst_program_sequence ON mst_program(rbsi_id, year_version, sequence_order);
    CREATE INDEX IX_mst_program_is_active ON mst_program(is_active);
END;
GO

-- Table: mst_initiative (Master Initiative)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'mst_initiative')
BEGIN
    CREATE TABLE mst_initiative (
        id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        program_id UNIQUEIDENTIFIER NOT NULL,
        initiative_number NVARCHAR(30) NOT NULL,
        sequence_order INT NOT NULL,
        name NVARCHAR(500) NOT NULL,
        description NVARCHAR(1000),
        year_version INT NOT NULL,
        submit_date DATETIME2,
        document_link NVARCHAR(500),
        status NVARCHAR(50) DEFAULT 'pending',
        pksi_relation_id UNIQUEIDENTIFIER,
        is_active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2,
        created_by NVARCHAR(100),
        updated_by NVARCHAR(100),
        CONSTRAINT FK_mst_initiative_program FOREIGN KEY (program_id) REFERENCES mst_program(id)
    );
    
    CREATE INDEX IX_mst_initiative_program_id ON mst_initiative(program_id);
    CREATE INDEX IX_mst_initiative_year_version ON mst_initiative(year_version);
    CREATE INDEX IX_mst_initiative_sequence ON mst_initiative(program_id, year_version, sequence_order);
    CREATE INDEX IX_mst_initiative_status ON mst_initiative(status);
    CREATE INDEX IX_mst_initiative_is_active ON mst_initiative(is_active);
END;
GO
