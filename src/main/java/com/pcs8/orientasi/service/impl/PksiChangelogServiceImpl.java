package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.response.PksiChangelogResponse;
import com.pcs8.orientasi.domain.entity.MstSkpa;
import com.pcs8.orientasi.domain.entity.MstUser;
import com.pcs8.orientasi.domain.entity.PksiChangelog;
import com.pcs8.orientasi.domain.entity.PksiDocument;
import com.pcs8.orientasi.repository.MstSkpaRepository;
import com.pcs8.orientasi.repository.PksiChangelogRepository;
import com.pcs8.orientasi.service.PksiChangelogService;
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
public class PksiChangelogServiceImpl implements PksiChangelogService {

    private static final Logger log = LoggerFactory.getLogger(PksiChangelogServiceImpl.class);

        private final PksiChangelogRepository pksiChangelogRepository;
        private final MstSkpaRepository skpaRepository;

    // Field label mapping for human-readable names
    private static final Map<String, String> FIELD_LABELS = createFieldLabels();

    private static Map<String, String> createFieldLabels() {
        Map<String, String> labels = new HashMap<>();
        // Header
        labels.put("namaPksi", "Nama PKSI");
        labels.put("tanggalPengajuan", "Tanggal Pengajuan");
        
        // Section 1: Pendahuluan
        labels.put("deskripsiPksi", "Deskripsi PKSI");
        labels.put("mengapaPksiDiperlukan", "Mengapa PKSI Diperlukan");
        labels.put("kapanDiselesaikan", "Kapan Diselesaikan");
        labels.put("picSatker", "PIC Satker");
        
        // Section 2: Tujuan dan Kegunaan
        labels.put("kegunaanPksi", "Kegunaan PKSI");
        labels.put("tujuanPksi", "Tujuan PKSI");
        labels.put("targetPksi", "Target PKSI");
        
        // Section 3: Cakupan
        labels.put("ruangLingkup", "Ruang Lingkup");
        labels.put("batasanPksi", "Batasan PKSI");
        labels.put("hubunganSistemLain", "Hubungan Sistem Lain");
        labels.put("asumsi", "Asumsi");
        
        // Section 4: Risiko dan Batasan
        labels.put("batasanDesain", "Batasan Desain");
        labels.put("risikoBisnis", "Risiko Bisnis");
        labels.put("risikoSuksesPksi", "Risiko Sukses PKSI");
        labels.put("pengendalianRisiko", "Pengendalian Risiko");
        
        // Section 5: Gambaran Umum Aplikasi
        labels.put("pengelolaAplikasi", "Pengelola Aplikasi");
        labels.put("penggunaAplikasi", "Pengguna Aplikasi");
        labels.put("programInisiatifRbsi", "Program Inisiatif RBSI");
        labels.put("fungsiAplikasi", "Fungsi Aplikasi");
        labels.put("informasiYangDikelola", "Informasi Yang Dikelola");
        labels.put("dasarPeraturan", "Dasar Peraturan");
        
        // Section 6: Jadwal Pelaksanaan
        labels.put("tahap1Awal", "Tahap 1 - Tanggal Awal");
        labels.put("tahap1Akhir", "Tahap 1 - Tanggal Akhir");
        labels.put("tahap5Awal", "Tahap 5 - Tanggal Awal");
        labels.put("tahap5Akhir", "Tahap 5 - Tanggal Akhir");
        labels.put("tahap7Awal", "Tahap 7 - Tanggal Awal");
        labels.put("tahap7Akhir", "Tahap 7 - Tanggal Akhir");
        
        // Section 7: Rencana Pengelolaan
        labels.put("rencanaPengelolaan", "Rencana Pengelolaan");
        
        // Status
        labels.put("status", "Status");
        
        // Approval fields - use friendly names
        labels.put("iku", "IKU");
        labels.put("inhouseOutsource", "Inhouse/Outsource");
        labels.put("picApproval", "PIC Approval");
        labels.put("picApprovalName", "PIC Approval");
        labels.put("anggotaTim", "Anggota Tim");
        labels.put("anggotaTimNames", "Anggota Tim");
        labels.put("progress", "Progress");
        
        // Monitoring fields - New
        labels.put("teamName", "Tim");
        labels.put("anggaranTotal", "Anggaran Total");
        labels.put("anggaranTahunIni", "Anggaran Tahun Ini");
        labels.put("anggaranTahunDepan", "Anggaran Tahun Depan");
        labels.put("targetUsreq", "Target Usreq");
        labels.put("targetSit", "Target SIT");
        labels.put("targetUat", "Target UAT");
        labels.put("targetGoLive", "Target Go Live");
        labels.put("statusT01T02", "Status Rencana PKSI (T01/T02)");
        labels.put("berkasT01T02", "Berkas Rencana PKSI (T01/T02)");
        labels.put("statusT11", "Status Spesifikasi Kebutuhan (T11)");
        labels.put("berkasT11", "Berkas Spesifikasi Kebutuhan (T11)");
        labels.put("statusCd", "Status CD Prinsip");
        labels.put("nomorCd", "Nomor CD Prinsip");
        labels.put("kontrakTanggalMulai", "Kontrak - Tanggal Mulai");
        labels.put("kontrakTanggalSelesai", "Kontrak - Tanggal Selesai");
        labels.put("kontrakNilai", "Kontrak - Nilai");
        labels.put("kontrakJumlahTermin", "Kontrak - Jumlah Termin");
        labels.put("kontrakDetailPembayaran", "Kontrak - Detail Pembayaran");
        labels.put("baDeploy", "BA Deploy");
        
        return Collections.unmodifiableMap(labels);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PksiChangelogResponse> getChangelogsByPksiId(UUID pksiDocumentId) {
        log.info("Fetching changelogs for PKSI document: {}", pksiDocumentId);
        
        List<PksiChangelog> changelogs = pksiChangelogRepository.findByPksiDocumentIdOrderByCreatedAtDesc(pksiDocumentId);
        
        return changelogs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void trackChanges(PksiDocument pksiDocument, PksiDocument oldDocument, MstUser updatedBy) {
        log.info("Tracking changes for PKSI document: {}", pksiDocument.getId());
        
        List<PksiChangelog> changes = new ArrayList<>();
        String updatedByName = getUpdatedByName(updatedBy);
        
        // Compare and track each field
        trackStringChange(changes, pksiDocument, oldDocument, "namaPksi", 
                oldDocument.getNamaPksi(), pksiDocument.getNamaPksi(), updatedBy, updatedByName);
        
        trackDateChange(changes, pksiDocument, oldDocument, "tanggalPengajuan",
                oldDocument.getTanggalPengajuan(), pksiDocument.getTanggalPengajuan(), updatedBy, updatedByName);
        
        // Section 1
        trackStringChange(changes, pksiDocument, oldDocument, "deskripsiPksi",
                oldDocument.getDeskripsiPksi(), pksiDocument.getDeskripsiPksi(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "mengapaPksiDiperlukan",
                oldDocument.getMengapaPksiDiperlukan(), pksiDocument.getMengapaPksiDiperlukan(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "kapanDiselesaikan",
                oldDocument.getKapanDiselesaikan(), pksiDocument.getKapanDiselesaikan(), updatedBy, updatedByName);
        // Resolve picSatker UUIDs to names
        trackStringChange(changes, pksiDocument, oldDocument, "picSatker",
                resolveSkpaUuidsToNames(oldDocument.getPicSatker()), 
                resolveSkpaUuidsToNames(pksiDocument.getPicSatker()), updatedBy, updatedByName);
        
        // Section 2
        trackStringChange(changes, pksiDocument, oldDocument, "kegunaanPksi",
                oldDocument.getKegunaanPksi(), pksiDocument.getKegunaanPksi(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "tujuanPksi",
                oldDocument.getTujuanPksi(), pksiDocument.getTujuanPksi(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "targetPksi",
                oldDocument.getTargetPksi(), pksiDocument.getTargetPksi(), updatedBy, updatedByName);
        
        // Section 3
        trackStringChange(changes, pksiDocument, oldDocument, "ruangLingkup",
                oldDocument.getRuangLingkup(), pksiDocument.getRuangLingkup(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "batasanPksi",
                oldDocument.getBatasanPksi(), pksiDocument.getBatasanPksi(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "hubunganSistemLain",
                oldDocument.getHubunganSistemLain(), pksiDocument.getHubunganSistemLain(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "asumsi",
                oldDocument.getAsumsi(), pksiDocument.getAsumsi(), updatedBy, updatedByName);
        
        // Section 4
        trackStringChange(changes, pksiDocument, oldDocument, "batasanDesain",
                oldDocument.getBatasanDesain(), pksiDocument.getBatasanDesain(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "risikoBisnis",
                oldDocument.getRisikoBisnis(), pksiDocument.getRisikoBisnis(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "risikoSuksesPksi",
                oldDocument.getRisikoSuksesPksi(), pksiDocument.getRisikoSuksesPksi(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "pengendalianRisiko",
                oldDocument.getPengendalianRisiko(), pksiDocument.getPengendalianRisiko(), updatedBy, updatedByName);
        
        // Section 5
        trackStringChange(changes, pksiDocument, oldDocument, "pengelolaAplikasi",
                oldDocument.getPengelolaAplikasi(), pksiDocument.getPengelolaAplikasi(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "penggunaAplikasi",
                oldDocument.getPenggunaAplikasi(), pksiDocument.getPenggunaAplikasi(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "programInisiatifRbsi",
                oldDocument.getProgramInisiatifRbsi(), pksiDocument.getProgramInisiatifRbsi(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "fungsiAplikasi",
                oldDocument.getFungsiAplikasi(), pksiDocument.getFungsiAplikasi(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "informasiYangDikelola",
                oldDocument.getInformasiYangDikelola(), pksiDocument.getInformasiYangDikelola(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "dasarPeraturan",
                oldDocument.getDasarPeraturan(), pksiDocument.getDasarPeraturan(), updatedBy, updatedByName);
        
        // Section 6 - Timeline
        trackDateChange(changes, pksiDocument, oldDocument, "tahap1Awal",
                oldDocument.getTahap1Awal(), pksiDocument.getTahap1Awal(), updatedBy, updatedByName);
        trackDateChange(changes, pksiDocument, oldDocument, "tahap1Akhir",
                oldDocument.getTahap1Akhir(), pksiDocument.getTahap1Akhir(), updatedBy, updatedByName);
        trackDateChange(changes, pksiDocument, oldDocument, "tahap5Awal",
                oldDocument.getTahap5Awal(), pksiDocument.getTahap5Awal(), updatedBy, updatedByName);
        trackDateChange(changes, pksiDocument, oldDocument, "tahap5Akhir",
                oldDocument.getTahap5Akhir(), pksiDocument.getTahap5Akhir(), updatedBy, updatedByName);
        trackDateChange(changes, pksiDocument, oldDocument, "tahap7Awal",
                oldDocument.getTahap7Awal(), pksiDocument.getTahap7Awal(), updatedBy, updatedByName);
        trackDateChange(changes, pksiDocument, oldDocument, "tahap7Akhir",
                oldDocument.getTahap7Akhir(), pksiDocument.getTahap7Akhir(), updatedBy, updatedByName);
        
        // Section 7
        trackStringChange(changes, pksiDocument, oldDocument, "rencanaPengelolaan",
                oldDocument.getRencanaPengelolaan(), pksiDocument.getRencanaPengelolaan(), updatedBy, updatedByName);
        
        // Status
        trackEnumChange(changes, pksiDocument, oldDocument, "status",
                oldDocument.getStatus(), pksiDocument.getStatus(), updatedBy, updatedByName);
        
        // Approval fields - Track Names instead of UUIDs
        trackStringChange(changes, pksiDocument, oldDocument, "iku",
                oldDocument.getIku(), pksiDocument.getIku(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "inhouseOutsource",
                oldDocument.getInhouseOutsource(), pksiDocument.getInhouseOutsource(), updatedBy, updatedByName);
        // Track picApprovalName instead of picApproval (UUID)
        trackStringChange(changes, pksiDocument, oldDocument, "picApprovalName",
                oldDocument.getPicApprovalName(), pksiDocument.getPicApprovalName(), updatedBy, updatedByName);
        // Track anggotaTimNames instead of anggotaTim (UUIDs)
        trackStringChange(changes, pksiDocument, oldDocument, "anggotaTimNames",
                oldDocument.getAnggotaTimNames(), pksiDocument.getAnggotaTimNames(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "progress",
                oldDocument.getProgress(), pksiDocument.getProgress(), updatedBy, updatedByName);
        
        // Monitoring fields - Anggaran
        trackStringChange(changes, pksiDocument, oldDocument, "anggaranTotal",
                oldDocument.getAnggaranTotal(), pksiDocument.getAnggaranTotal(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "anggaranTahunIni",
                oldDocument.getAnggaranTahunIni(), pksiDocument.getAnggaranTahunIni(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "anggaranTahunDepan",
                oldDocument.getAnggaranTahunDepan(), pksiDocument.getAnggaranTahunDepan(), updatedBy, updatedByName);
        
        // Monitoring fields - Target Timeline
        trackDateChange(changes, pksiDocument, oldDocument, "targetUsreq",
                oldDocument.getTargetUsreq(), pksiDocument.getTargetUsreq(), updatedBy, updatedByName);
        trackDateChange(changes, pksiDocument, oldDocument, "targetSit",
                oldDocument.getTargetSit(), pksiDocument.getTargetSit(), updatedBy, updatedByName);
        trackDateChange(changes, pksiDocument, oldDocument, "targetUat",
                oldDocument.getTargetUat(), pksiDocument.getTargetUat(), updatedBy, updatedByName);
        trackDateChange(changes, pksiDocument, oldDocument, "targetGoLive",
                oldDocument.getTargetGoLive(), pksiDocument.getTargetGoLive(), updatedBy, updatedByName);
        
        // Monitoring fields - T01/T02 Status
        trackStringChange(changes, pksiDocument, oldDocument, "statusT01T02",
                oldDocument.getStatusT01T02(), pksiDocument.getStatusT01T02(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "berkasT01T02",
                oldDocument.getBerkasT01T02(), pksiDocument.getBerkasT01T02(), updatedBy, updatedByName);
        
        // Monitoring fields - T11 Status
        trackStringChange(changes, pksiDocument, oldDocument, "statusT11",
                oldDocument.getStatusT11(), pksiDocument.getStatusT11(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "berkasT11",
                oldDocument.getBerkasT11(), pksiDocument.getBerkasT11(), updatedBy, updatedByName);
        
        // Monitoring fields - CD Prinsip
        trackStringChange(changes, pksiDocument, oldDocument, "statusCd",
                oldDocument.getStatusCd(), pksiDocument.getStatusCd(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "nomorCd",
                oldDocument.getNomorCd(), pksiDocument.getNomorCd(), updatedBy, updatedByName);
        
        // Monitoring fields - Kontrak
        trackStringChange(changes, pksiDocument, oldDocument, "kontrakTanggalMulai",
                oldDocument.getKontrakTanggalMulai(), pksiDocument.getKontrakTanggalMulai(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "kontrakTanggalSelesai",
                oldDocument.getKontrakTanggalSelesai(), pksiDocument.getKontrakTanggalSelesai(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "kontrakNilai",
                oldDocument.getKontrakNilai(), pksiDocument.getKontrakNilai(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "kontrakJumlahTermin",
                oldDocument.getKontrakJumlahTermin(), pksiDocument.getKontrakJumlahTermin(), updatedBy, updatedByName);
        trackStringChange(changes, pksiDocument, oldDocument, "kontrakDetailPembayaran",
                oldDocument.getKontrakDetailPembayaran(), pksiDocument.getKontrakDetailPembayaran(), updatedBy, updatedByName);
        
        // Monitoring fields - BA Deploy
        trackStringChange(changes, pksiDocument, oldDocument, "baDeploy",
                oldDocument.getBaDeploy(), pksiDocument.getBaDeploy(), updatedBy, updatedByName);
        
        // Save all changes
        if (!changes.isEmpty()) {
            pksiChangelogRepository.saveAll(changes);
            log.info("Saved {} changes for PKSI document: {}", changes.size(), pksiDocument.getId());
        } else {
            log.info("No changes detected for PKSI document: {}", pksiDocument.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countChangelogsByPksiId(UUID pksiDocumentId) {
        return pksiChangelogRepository.countByPksiDocumentId(pksiDocumentId);
    }

    private void trackStringChange(List<PksiChangelog> changes, PksiDocument pksiDocument, 
            PksiDocument oldDocument, String fieldName, String oldValue, String newValue, 
            MstUser updatedBy, String updatedByName) {
        
        if (!Objects.equals(oldValue, newValue)) {
            changes.add(createChangelog(pksiDocument, fieldName, oldValue, newValue, updatedBy, updatedByName));
        }
    }

    private void trackDateChange(List<PksiChangelog> changes, PksiDocument pksiDocument,
            PksiDocument oldDocument, String fieldName, LocalDate oldValue, LocalDate newValue,
            MstUser updatedBy, String updatedByName) {
        
        if (!Objects.equals(oldValue, newValue)) {
            String oldStr = oldValue != null ? oldValue.toString() : null;
            String newStr = newValue != null ? newValue.toString() : null;
            changes.add(createChangelog(pksiDocument, fieldName, oldStr, newStr, updatedBy, updatedByName));
        }
    }

    private void trackEnumChange(List<PksiChangelog> changes, PksiDocument pksiDocument,
            PksiDocument oldDocument, String fieldName, Enum<?> oldValue, Enum<?> newValue,
            MstUser updatedBy, String updatedByName) {
        
        if (!Objects.equals(oldValue, newValue)) {
            String oldStr = oldValue != null ? oldValue.name() : null;
            String newStr = newValue != null ? newValue.name() : null;
            changes.add(createChangelog(pksiDocument, fieldName, oldStr, newStr, updatedBy, updatedByName));
        }
    }

    private PksiChangelog createChangelog(PksiDocument pksiDocument, String fieldName,
            String oldValue, String newValue, MstUser updatedBy, String updatedByName) {
        
        String fieldLabel = FIELD_LABELS.getOrDefault(fieldName, fieldName);
        
        return PksiChangelog.builder()
                .pksiDocument(pksiDocument)
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

    /**
     * Resolve SKPA UUIDs to human-readable kode_skpa names.
     * Input: comma-separated UUIDs like "uuid1, uuid2"
     * Output: comma-separated kode_skpa values like "SKPA1, SKPA2"
     */
    private String resolveSkpaUuidsToNames(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            return null;
        }

        String[] guids = uuidString.split(",");
        String resolvedNames = Arrays.stream(guids)
                .map(String::trim)
                .filter(guid -> !guid.isEmpty())
                .map(guid -> {
                    try {
                        UUID uuid = UUID.fromString(guid);
                        Optional<MstSkpa> skpa = skpaRepository.findById(uuid);
                        return skpa.map(MstSkpa::getKodeSkpa).orElse(null);
                    } catch (IllegalArgumentException e) {
                        // Not a valid UUID, return original value
                        return guid;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));

        return resolvedNames.isEmpty() ? null : resolvedNames;
    }
    private PksiChangelogResponse mapToResponse(PksiChangelog changelog) {
        return PksiChangelogResponse.builder()
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
}
