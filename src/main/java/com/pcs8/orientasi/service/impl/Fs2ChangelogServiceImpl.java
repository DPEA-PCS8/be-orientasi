package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.response.Fs2ChangelogResponse;
import com.pcs8.orientasi.domain.entity.Fs2Changelog;
import com.pcs8.orientasi.domain.entity.Fs2Document;
import com.pcs8.orientasi.domain.entity.MstUser;
import com.pcs8.orientasi.repository.Fs2ChangelogRepository;
import com.pcs8.orientasi.service.Fs2ChangelogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Fs2ChangelogServiceImpl implements Fs2ChangelogService {

    private static final Logger log = LoggerFactory.getLogger(Fs2ChangelogServiceImpl.class);

    private final Fs2ChangelogRepository fs2ChangelogRepository;

    // Field label mapping for human-readable names
    private static final Map<String, String> FIELD_LABELS = createFieldLabels();

    private static Map<String, String> createFieldLabels() {
        Map<String, String> labels = new HashMap<>();
        
        // Header
        labels.put("tanggalPengajuan", "Tanggal Pengajuan");
        labels.put("status", "Status");
        
        // Deskripsi dan Alasan
        labels.put("deskripsiPengubahan", "Deskripsi Pengubahan");
        labels.put("alasanPengubahan", "Alasan Pengubahan");
        labels.put("statusTahapan", "Status Tahapan");
        labels.put("urgensi", "Urgensi");
        
        // Kriteria
        labels.put("kriteria1", "Kriteria 1");
        labels.put("kriteria2", "Kriteria 2");
        labels.put("kriteria3", "Kriteria 3");
        labels.put("kriteria4", "Kriteria 4");
        
        // Aspek Perubahan
        labels.put("aspekSistemAda", "Aspek Sistem Ada");
        labels.put("aspekSistemTerkait", "Aspek Sistem Terkait");
        labels.put("aspekAlurKerja", "Aspek Alur Kerja");
        labels.put("aspekStrukturOrganisasi", "Aspek Struktur Organisasi");
        
        // Dokumentasi
        labels.put("dokT01Sebelum", "Dok T.0.1 Sebelum");
        labels.put("dokT01Sesudah", "Dok T.0.1 Sesudah");
        labels.put("dokT11Sebelum", "Dok T.1.1 Sebelum");
        labels.put("dokT11Sesudah", "Dok T.1.1 Sesudah");
        
        // Penggunaan Sistem
        labels.put("penggunaSebelum", "Jumlah Pengguna Sebelum");
        labels.put("penggunaSesudah", "Jumlah Pengguna Sesudah");
        labels.put("aksesBersamaanSebelum", "Akses Bersamaan Sebelum");
        labels.put("aksesBersamaanSesudah", "Akses Bersamaan Sesudah");
        labels.put("pertumbuhanDataSebelum", "Pertumbuhan Data Sebelum");
        labels.put("pertumbuhanDataSesudah", "Pertumbuhan Data Sesudah");
        
        // Target
        labels.put("targetPengujian", "Target Pengujian");
        labels.put("targetDeployment", "Target Deployment");
        labels.put("targetGoLive", "Target Go Live");
        
        // Pernyataan
        labels.put("pernyataan1", "Pernyataan 1");
        labels.put("pernyataan2", "Pernyataan 2");
        
        // F.S.2 Disetujui fields
        labels.put("progres", "Progres");
        labels.put("fasePengajuan", "Fase Pengajuan");
        labels.put("iku", "IKU");
        labels.put("mekanisme", "Mekanisme");
        labels.put("pelaksanaan", "Pelaksanaan");
        labels.put("tahun", "Tahun");
        labels.put("tahunMulai", "Tahun Mulai");
        labels.put("tahunSelesai", "Tahun Selesai");
        labels.put("picName", "PIC");
        labels.put("dokumenPath", "Dokumen Path");
        
        // Monitoring fields
        labels.put("nomorNd", "Nomor ND");
        labels.put("tanggalNd", "Tanggal ND");
        labels.put("berkasNd", "Berkas ND");
        labels.put("berkasFs2", "Berkas F.S.2");
        labels.put("nomorCd", "Nomor CD Prinsip Persetujuan FS2");
        labels.put("tanggalCd", "Tanggal CD");
        labels.put("berkasCd", "Berkas CD Prinsip Persetujuan FS2");
        labels.put("berkasFs2a", "Berkas F.S.2.a");
        labels.put("berkasFs2b", "Berkas F.S.2.b");
        labels.put("realisasiPengujian", "Realisasi Pengujian");
        labels.put("berkasF45", "Berkas F.4.5");
        labels.put("berkasF46", "Berkas F.4.6");
        labels.put("realisasiDeployment", "Realisasi Deployment");
        labels.put("berkasNdBaDeployment", "Berkas ND/BA Deployment");
        labels.put("keterangan", "Keterangan");
        
        return Collections.unmodifiableMap(labels);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Fs2ChangelogResponse> getChangelogsByFs2Id(UUID fs2DocumentId) {
        log.info("Fetching changelogs for FS2 document: {}", fs2DocumentId);
        
        List<Fs2Changelog> changelogs = fs2ChangelogRepository.findByFs2DocumentIdOrderByCreatedAtDesc(fs2DocumentId);
        
        return changelogs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void trackChanges(Fs2Document fs2Document, Fs2Document oldDocument, MstUser updatedBy) {
        log.info("Tracking changes for FS2 document: {}", fs2Document.getId());
        
        List<Fs2Changelog> changes = new ArrayList<>();
        String updatedByName = getUpdatedByName(updatedBy);
        
        // Basic fields
        trackDateChange(changes, fs2Document, oldDocument, "tanggalPengajuan",
                oldDocument.getTanggalPengajuan(), fs2Document.getTanggalPengajuan(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "status",
                oldDocument.getStatus(), fs2Document.getStatus(), updatedBy, updatedByName);
        
        // Deskripsi dan Alasan
        trackStringChange(changes, fs2Document, oldDocument, "deskripsiPengubahan",
                oldDocument.getDeskripsiPengubahan(), fs2Document.getDeskripsiPengubahan(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "alasanPengubahan",
                oldDocument.getAlasanPengubahan(), fs2Document.getAlasanPengubahan(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "statusTahapan",
                oldDocument.getStatusTahapan(), fs2Document.getStatusTahapan(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "urgensi",
                oldDocument.getUrgensi(), fs2Document.getUrgensi(), updatedBy, updatedByName);
        
        // Kriteria (Boolean)
        trackBooleanChange(changes, fs2Document, oldDocument, "kriteria1",
                oldDocument.getKriteria1(), fs2Document.getKriteria1(), updatedBy, updatedByName);
        trackBooleanChange(changes, fs2Document, oldDocument, "kriteria2",
                oldDocument.getKriteria2(), fs2Document.getKriteria2(), updatedBy, updatedByName);
        trackBooleanChange(changes, fs2Document, oldDocument, "kriteria3",
                oldDocument.getKriteria3(), fs2Document.getKriteria3(), updatedBy, updatedByName);
        trackBooleanChange(changes, fs2Document, oldDocument, "kriteria4",
                oldDocument.getKriteria4(), fs2Document.getKriteria4(), updatedBy, updatedByName);
        
        // Aspek Perubahan
        trackStringChange(changes, fs2Document, oldDocument, "aspekSistemAda",
                oldDocument.getAspekSistemAda(), fs2Document.getAspekSistemAda(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "aspekSistemTerkait",
                oldDocument.getAspekSistemTerkait(), fs2Document.getAspekSistemTerkait(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "aspekAlurKerja",
                oldDocument.getAspekAlurKerja(), fs2Document.getAspekAlurKerja(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "aspekStrukturOrganisasi",
                oldDocument.getAspekStrukturOrganisasi(), fs2Document.getAspekStrukturOrganisasi(), updatedBy, updatedByName);
        
        // Dokumentasi
        trackStringChange(changes, fs2Document, oldDocument, "dokT01Sebelum",
                oldDocument.getDokT01Sebelum(), fs2Document.getDokT01Sebelum(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "dokT01Sesudah",
                oldDocument.getDokT01Sesudah(), fs2Document.getDokT01Sesudah(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "dokT11Sebelum",
                oldDocument.getDokT11Sebelum(), fs2Document.getDokT11Sebelum(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "dokT11Sesudah",
                oldDocument.getDokT11Sesudah(), fs2Document.getDokT11Sesudah(), updatedBy, updatedByName);
        
        // Penggunaan Sistem
        trackStringChange(changes, fs2Document, oldDocument, "penggunaSebelum",
                oldDocument.getPenggunaSebelum(), fs2Document.getPenggunaSebelum(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "penggunaSesudah",
                oldDocument.getPenggunaSesudah(), fs2Document.getPenggunaSesudah(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "aksesBersamaanSebelum",
                oldDocument.getAksesBersamaanSebelum(), fs2Document.getAksesBersamaanSebelum(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "aksesBersamaanSesudah",
                oldDocument.getAksesBersamaanSesudah(), fs2Document.getAksesBersamaanSesudah(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "pertumbuhanDataSebelum",
                oldDocument.getPertumbuhanDataSebelum(), fs2Document.getPertumbuhanDataSebelum(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "pertumbuhanDataSesudah",
                oldDocument.getPertumbuhanDataSesudah(), fs2Document.getPertumbuhanDataSesudah(), updatedBy, updatedByName);
        
        // Target
        trackDateChange(changes, fs2Document, oldDocument, "targetPengujian",
                oldDocument.getTargetPengujian(), fs2Document.getTargetPengujian(), updatedBy, updatedByName);
        trackDateChange(changes, fs2Document, oldDocument, "targetDeployment",
                oldDocument.getTargetDeployment(), fs2Document.getTargetDeployment(), updatedBy, updatedByName);
        trackDateChange(changes, fs2Document, oldDocument, "targetGoLive",
                oldDocument.getTargetGoLive(), fs2Document.getTargetGoLive(), updatedBy, updatedByName);
        
        // Pernyataan
        trackBooleanChange(changes, fs2Document, oldDocument, "pernyataan1",
                oldDocument.getPernyataan1(), fs2Document.getPernyataan1(), updatedBy, updatedByName);
        trackBooleanChange(changes, fs2Document, oldDocument, "pernyataan2",
                oldDocument.getPernyataan2(), fs2Document.getPernyataan2(), updatedBy, updatedByName);
        
        // F.S.2 Disetujui fields
        trackStringChange(changes, fs2Document, oldDocument, "progres",
                oldDocument.getProgres(), fs2Document.getProgres(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "fasePengajuan",
                oldDocument.getFasePengajuan(), fs2Document.getFasePengajuan(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "iku",
                oldDocument.getIku(), fs2Document.getIku(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "mekanisme",
                oldDocument.getMekanisme(), fs2Document.getMekanisme(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "pelaksanaan",
                oldDocument.getPelaksanaan(), fs2Document.getPelaksanaan(), updatedBy, updatedByName);
        trackIntegerChange(changes, fs2Document, oldDocument, "tahun",
                oldDocument.getTahun(), fs2Document.getTahun(), updatedBy, updatedByName);
        trackIntegerChange(changes, fs2Document, oldDocument, "tahunMulai",
                oldDocument.getTahunMulai(), fs2Document.getTahunMulai(), updatedBy, updatedByName);
        trackIntegerChange(changes, fs2Document, oldDocument, "tahunSelesai",
                oldDocument.getTahunSelesai(), fs2Document.getTahunSelesai(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "picName",
                oldDocument.getPicName(), fs2Document.getPicName(), updatedBy, updatedByName);
        
        // Monitoring fields
        trackStringChange(changes, fs2Document, oldDocument, "nomorNd",
                oldDocument.getNomorNd(), fs2Document.getNomorNd(), updatedBy, updatedByName);
        trackDateChange(changes, fs2Document, oldDocument, "tanggalNd",
                oldDocument.getTanggalNd(), fs2Document.getTanggalNd(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "berkasNd",
                oldDocument.getBerkasNd(), fs2Document.getBerkasNd(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "berkasFs2",
                oldDocument.getBerkasFs2(), fs2Document.getBerkasFs2(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "nomorCd",
                oldDocument.getNomorCd(), fs2Document.getNomorCd(), updatedBy, updatedByName);
        trackDateChange(changes, fs2Document, oldDocument, "tanggalCd",
                oldDocument.getTanggalCd(), fs2Document.getTanggalCd(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "berkasCd",
                oldDocument.getBerkasCd(), fs2Document.getBerkasCd(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "berkasFs2a",
                oldDocument.getBerkasFs2a(), fs2Document.getBerkasFs2a(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "berkasFs2b",
                oldDocument.getBerkasFs2b(), fs2Document.getBerkasFs2b(), updatedBy, updatedByName);
        trackDateChange(changes, fs2Document, oldDocument, "realisasiPengujian",
                oldDocument.getRealisasiPengujian(), fs2Document.getRealisasiPengujian(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "berkasF45",
                oldDocument.getBerkasF45(), fs2Document.getBerkasF45(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "berkasF46",
                oldDocument.getBerkasF46(), fs2Document.getBerkasF46(), updatedBy, updatedByName);
        trackDateChange(changes, fs2Document, oldDocument, "realisasiDeployment",
                oldDocument.getRealisasiDeployment(), fs2Document.getRealisasiDeployment(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "berkasNdBaDeployment",
                oldDocument.getBerkasNdBaDeployment(), fs2Document.getBerkasNdBaDeployment(), updatedBy, updatedByName);
        trackStringChange(changes, fs2Document, oldDocument, "keterangan",
                oldDocument.getKeterangan(), fs2Document.getKeterangan(), updatedBy, updatedByName);
        
        // Save all changes
        if (!changes.isEmpty()) {
            fs2ChangelogRepository.saveAll(changes);
            log.info("Saved {} changes for FS2 document: {}", changes.size(), fs2Document.getId());
        } else {
            log.info("No changes detected for FS2 document: {}", fs2Document.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countChangelogsByFs2Id(UUID fs2DocumentId) {
        return fs2ChangelogRepository.countByFs2DocumentId(fs2DocumentId);
    }

    private void trackStringChange(List<Fs2Changelog> changes, Fs2Document fs2Document, 
            Fs2Document oldDocument, String fieldName, String oldValue, String newValue, 
            MstUser updatedBy, String updatedByName) {
        
        if (!Objects.equals(oldValue, newValue)) {
            changes.add(createChangelog(fs2Document, fieldName, oldValue, newValue, updatedBy, updatedByName));
        }
    }

    private void trackDateChange(List<Fs2Changelog> changes, Fs2Document fs2Document,
            Fs2Document oldDocument, String fieldName, LocalDate oldValue, LocalDate newValue,
            MstUser updatedBy, String updatedByName) {
        
        if (!Objects.equals(oldValue, newValue)) {
            String oldStr = oldValue != null ? oldValue.toString() : null;
            String newStr = newValue != null ? newValue.toString() : null;
            changes.add(createChangelog(fs2Document, fieldName, oldStr, newStr, updatedBy, updatedByName));
        }
    }

    private void trackBooleanChange(List<Fs2Changelog> changes, Fs2Document fs2Document,
            Fs2Document oldDocument, String fieldName, Boolean oldValue, Boolean newValue,
            MstUser updatedBy, String updatedByName) {
        
        if (!Objects.equals(oldValue, newValue)) {
            String oldStr = oldValue != null ? (oldValue ? "Ya" : "Tidak") : null;
            String newStr = newValue != null ? (newValue ? "Ya" : "Tidak") : null;
            changes.add(createChangelog(fs2Document, fieldName, oldStr, newStr, updatedBy, updatedByName));
        }
    }

    private void trackIntegerChange(List<Fs2Changelog> changes, Fs2Document fs2Document,
            Fs2Document oldDocument, String fieldName, Integer oldValue, Integer newValue,
            MstUser updatedBy, String updatedByName) {
        
        if (!Objects.equals(oldValue, newValue)) {
            String oldStr = oldValue != null ? oldValue.toString() : null;
            String newStr = newValue != null ? newValue.toString() : null;
            changes.add(createChangelog(fs2Document, fieldName, oldStr, newStr, updatedBy, updatedByName));
        }
    }

    private Fs2Changelog createChangelog(Fs2Document fs2Document, String fieldName,
            String oldValue, String newValue, MstUser updatedBy, String updatedByName) {
        
        String fieldLabel = FIELD_LABELS.getOrDefault(fieldName, fieldName);
        
        return Fs2Changelog.builder()
                .fs2Document(fs2Document)
                .fieldName(fieldName)
                .fieldLabel(fieldLabel)
                .oldValue(oldValue)
                .newValue(newValue)
                .updatedBy(updatedBy)
                .updatedByName(updatedByName)
                .build();
    }

    private String getUpdatedByName(MstUser user) {
        if (user == null) {
            return "System";
        }
        
        String fullName = user.getFullName();
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        }
        
        String username = user.getUsername();
        return username != null ? username : "Unknown";
    }

    private Fs2ChangelogResponse mapToResponse(Fs2Changelog changelog) {
        return Fs2ChangelogResponse.builder()
                .id(changelog.getId().toString())
                .fieldName(changelog.getFieldName())
                .fieldLabel(changelog.getFieldLabel())
                .oldValue(changelog.getOldValue())
                .newValue(changelog.getNewValue())
                .updatedBy(changelog.getUpdatedBy() != null ? changelog.getUpdatedBy().getUuid().toString() : null)
                .updatedByName(changelog.getUpdatedByName())
                .updatedAt(changelog.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteByFs2DocumentId(UUID fs2DocumentId) {
        log.info("Deleting all changelogs for FS2 document: {}", fs2DocumentId);
        fs2ChangelogRepository.deleteByFs2DocumentId(fs2DocumentId);
        log.info("Successfully deleted changelogs for FS2 document: {}", fs2DocumentId);
    }
}
