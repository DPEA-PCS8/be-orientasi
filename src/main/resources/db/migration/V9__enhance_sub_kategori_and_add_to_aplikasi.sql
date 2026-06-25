-- V9__enhance_sub_kategori_and_add_to_aplikasi.sql
-- Description: Enhance mst_sub_kategori with category_name and create snapshot table for historical data
-- Also adds sub_kategori_id relation to mst_aplikasi

-- 1. Add category_name column to mst_sub_kategori for full category name
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('mst_sub_kategori') AND name = 'category_name')
BEGIN
    ALTER TABLE mst_sub_kategori ADD category_name NVARCHAR(100) NULL;
END
GO

-- 2. Update existing category_code values with their full names
UPDATE mst_sub_kategori SET category_name = 'Core System' WHERE category_code = 'CS' AND category_name IS NULL;
UPDATE mst_sub_kategori SET category_name = 'Supporting System' WHERE category_code = 'SP' AND category_name IS NULL;
UPDATE mst_sub_kategori SET category_name = 'Data Analytics' WHERE category_code = 'DA' AND category_name IS NULL;
UPDATE mst_sub_kategori SET category_name = 'Data Management' WHERE category_code = 'DM' AND category_name IS NULL;
GO

-- 3. Make category_name NOT NULL after update
ALTER TABLE mst_sub_kategori ALTER COLUMN category_name NVARCHAR(100) NOT NULL;
GO

-- 4. Create snapshot table for historical data per year
IF NOT EXISTS (SELECT 1 FROM sys.objects WHERE object_id = OBJECT_ID('mst_sub_kategori_snapshot') AND type = 'U')
BEGIN
    CREATE TABLE mst_sub_kategori_snapshot (
        id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        snapshot_year INT NOT NULL,
        sub_kategori_id UNIQUEIDENTIFIER NOT NULL,
        kode NVARCHAR(20) NOT NULL,
        nama NVARCHAR(255) NOT NULL,
        category_code NVARCHAR(10) NOT NULL,
        category_name NVARCHAR(100) NOT NULL,
        snapshot_date DATETIME2 NOT NULL DEFAULT GETDATE(),
        change_type NVARCHAR(20) NOT NULL, -- CREATED, UPDATED, DELETED
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        created_by NVARCHAR(255) NULL,
        CONSTRAINT pk_mst_sub_kategori_snapshot PRIMARY KEY (id),
        CONSTRAINT fk_snapshot_sub_kategori FOREIGN KEY (sub_kategori_id) REFERENCES mst_sub_kategori(id)
    );
    
    CREATE INDEX idx_snapshot_year ON mst_sub_kategori_snapshot(snapshot_year);
    CREATE INDEX idx_snapshot_sub_kategori ON mst_sub_kategori_snapshot(sub_kategori_id);
END
GO

-- 5. Add sub_kategori_id to mst_aplikasi
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('mst_aplikasi') AND name = 'sub_kategori_id')
BEGIN
    ALTER TABLE mst_aplikasi ADD sub_kategori_id UNIQUEIDENTIFIER NULL;
    
    ALTER TABLE mst_aplikasi ADD CONSTRAINT fk_aplikasi_sub_kategori 
        FOREIGN KEY (sub_kategori_id) REFERENCES mst_sub_kategori(id);
    
    CREATE INDEX idx_aplikasi_sub_kategori ON mst_aplikasi(sub_kategori_id);
END
GO

-- 6. Add sub_kategori_id to aplikasi_snapshot for historical tracking
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('aplikasi_snapshot') AND name = 'sub_kategori_id')
BEGIN
    ALTER TABLE aplikasi_snapshot ADD sub_kategori_id UNIQUEIDENTIFIER NULL;
    ALTER TABLE aplikasi_snapshot ADD sub_kategori_kode NVARCHAR(20) NULL;
    ALTER TABLE aplikasi_snapshot ADD sub_kategori_nama NVARCHAR(255) NULL;
END
GO
