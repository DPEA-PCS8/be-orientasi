-- Normalize tahapan_status_* values to canonical uppercase underscore format
BEGIN TRANSACTION;

UPDATE mst_fs2_document
SET
    tahapan_status_pengajuan = CASE
        WHEN tahapan_status_pengajuan IS NULL THEN NULL
        WHEN LOWER(tahapan_status_pengajuan) LIKE '%dalam%proses%' THEN 'DALAM_PROSES'
        WHEN LOWER(tahapan_status_pengajuan) LIKE '%belum%mulai%' OR LOWER(tahapan_status_pengajuan) LIKE '%belum%dimulai%' THEN 'BELUM_DIMULAI'
        WHEN LOWER(tahapan_status_pengajuan) LIKE '%selesai%' THEN 'SELESAI'
        ELSE UPPER(REPLACE(tahapan_status_pengajuan,' ', '_'))
    END,

    tahapan_status_asesmen = CASE
        WHEN tahapan_status_asesmen IS NULL THEN NULL
        WHEN LOWER(tahapan_status_asesmen) LIKE '%dalam%proses%' THEN 'DALAM_PROSES'
        WHEN LOWER(tahapan_status_asesmen) LIKE '%belum%mulai%' OR LOWER(tahapan_status_asesmen) LIKE '%belum%dimulai%' THEN 'BELUM_DIMULAI'
        WHEN LOWER(tahapan_status_asesmen) LIKE '%selesai%' THEN 'SELESAI'
        ELSE UPPER(REPLACE(tahapan_status_asesmen,' ', '_'))
    END,

    tahapan_status_pemrograman = CASE
        WHEN tahapan_status_pemrograman IS NULL THEN NULL
        WHEN LOWER(tahapan_status_pemrograman) LIKE '%dalam%proses%' THEN 'DALAM_PROSES'
        WHEN LOWER(tahapan_status_pemrograman) LIKE '%belum%mulai%' OR LOWER(tahapan_status_pemrograman) LIKE '%belum%dimulai%' THEN 'BELUM_DIMULAI'
        WHEN LOWER(tahapan_status_pemrograman) LIKE '%selesai%' THEN 'SELESAI'
        ELSE UPPER(REPLACE(tahapan_status_pemrograman,' ', '_'))
    END,

    tahapan_status_pengujian = CASE
        WHEN tahapan_status_pengujian IS NULL THEN NULL
        WHEN LOWER(tahapan_status_pengujian) LIKE '%dalam%proses%' THEN 'DALAM_PROSES'
        WHEN LOWER(tahapan_status_pengujian) LIKE '%belum%mulai%' OR LOWER(tahapan_status_pengujian) LIKE '%belum%dimulai%' THEN 'BELUM_DIMULAI'
        WHEN LOWER(tahapan_status_pengujian) LIKE '%selesai%' THEN 'SELESAI'
        ELSE UPPER(REPLACE(tahapan_status_pengujian,' ', '_'))
    END,

    tahapan_status_deployment = CASE
        WHEN tahapan_status_deployment IS NULL THEN NULL
        WHEN LOWER(tahapan_status_deployment) LIKE '%dalam%proses%' THEN 'DALAM_PROSES'
        WHEN LOWER(tahapan_status_deployment) LIKE '%belum%mulai%' OR LOWER(tahapan_status_deployment) LIKE '%belum%dimulai%' THEN 'BELUM_DIMULAI'
        WHEN LOWER(tahapan_status_deployment) LIKE '%selesai%' THEN 'SELESAI'
        ELSE UPPER(REPLACE(tahapan_status_deployment,' ', '_'))
    END,

    tahapan_status_go_live = CASE
        WHEN tahapan_status_go_live IS NULL THEN NULL
        WHEN LOWER(tahapan_status_go_live) LIKE '%dalam%proses%' THEN 'DALAM_PROSES'
        WHEN LOWER(tahapan_status_go_live) LIKE '%belum%mulai%' OR LOWER(tahapan_status_go_live) LIKE '%belum%dimulai%' THEN 'BELUM_DIMULAI'
        WHEN LOWER(tahapan_status_go_live) LIKE '%selesai%' THEN 'SELESAI'
        ELSE UPPER(REPLACE(tahapan_status_go_live,' ', '_'))
    END
;

COMMIT;
