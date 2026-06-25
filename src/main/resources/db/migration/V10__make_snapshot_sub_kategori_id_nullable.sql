-- Make sub_kategori_id nullable in snapshot table to support DELETED records
-- When a sub kategori is deleted, we still want to keep the snapshot but the entity no longer exists

ALTER TABLE mst_sub_kategori_snapshot
ALTER COLUMN sub_kategori_id UNIQUEIDENTIFIER NULL;
