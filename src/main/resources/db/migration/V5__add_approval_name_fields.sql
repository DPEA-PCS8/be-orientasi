-- Add name fields for PIC and Anggota Tim to display human-readable names
ALTER TABLE pksi_document ADD pic_approval_name NVARCHAR(255) NULL;
ALTER TABLE pksi_document ADD anggota_tim_names NVARCHAR(MAX) NULL;
