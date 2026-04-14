-- Add per-tahapan completion date fields to trn_pksi_document
ALTER TABLE trn_pksi_document ADD tanggal_pengadaan DATE NULL;
ALTER TABLE trn_pksi_document ADD tanggal_desain DATE NULL;
ALTER TABLE trn_pksi_document ADD tanggal_coding DATE NULL;
ALTER TABLE trn_pksi_document ADD tanggal_unit_test DATE NULL;
