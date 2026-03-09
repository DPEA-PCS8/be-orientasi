package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.PksiDocumentRequest;
import com.pcs8.orientasi.domain.dto.response.PksiDocumentResponse;
import com.pcs8.orientasi.domain.entity.PksiDocument;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Mapper untuk konversi antara PksiDocument entity, request, dan response.
 * Mengurangi duplikasi kode di service implementation.
 */
@Component
public class PksiDocumentMapper {

    /**
     * Map fields from request to existing document entity (for update)
     */
    public void mapRequestToDocument(PksiDocumentRequest request, PksiDocument document) {
        // Header
        document.setNamaPksi(request.getNamaPksi());
        document.setTanggalPengajuan(parseDate(request.getTanggalPengajuan()));
        // Section 1
        document.setDeskripsiPksi(request.getDeskripsiPksi());
        document.setMengapaPksiDiperlukan(request.getMengapaPksiDiperlukan());
        document.setKapanDiselesaikan(resolveKapanDiselesaikan(request));
        document.setPicSatker(resolvePicSatker(request));
        // Section 2
        document.setKegunaanPksi(request.getKegunaanPksi());
        document.setTujuanPksi(request.getTujuanPksi());
        document.setTargetPksi(request.getTargetPksi());
        // Section 3
        document.setRuangLingkup(request.getRuangLingkup());
        document.setBatasanPksi(request.getBatasanPksi());
        document.setHubunganSistemLain(request.getHubunganSistemLain());
        document.setAsumsi(request.getAsumsi());
        // Section 4
        document.setBatasanDesain(request.getBatasanDesain());
        document.setRisikoBisnis(request.getRisikoBisnis());
        document.setRisikoSuksesPksi(request.getRisikoSuksesPksi());
        document.setPengendalianRisiko(request.getPengendalianRisiko());
        // Section 5
        document.setPengelolaAplikasi(request.getPengelolaAplikasi());
        document.setPenggunaAplikasi(request.getPenggunaAplikasi());
        document.setProgramInisiatifRbsi(request.getProgramInisiatifRbsi());
        document.setFungsiAplikasi(request.getFungsiAplikasi());
        document.setInformasiYangDikelola(request.getInformasiYangDikelola());
        document.setDasarPeraturan(request.getDasarPeraturan());
        // Section 6
        document.setTahap1Awal(parseDate(request.getTahap1Awal()));
        document.setTahap1Akhir(parseDate(request.getTahap1Akhir()));
        document.setTahap5Awal(parseDate(request.getTahap5Awal()));
        document.setTahap5Akhir(parseDate(request.getTahap5Akhir()));
        document.setTahap7Awal(parseDate(request.getTahap7Awal()));
        document.setTahap7Akhir(parseDate(request.getTahap7Akhir()));
        // Section 7
        document.setRencanaPengelolaan(request.getRencanaPengelolaan());
        // Legacy
        document.setTujuanPengajuan(request.getTujuanPengajuan());
    }

    /**
     * Map document entity to response DTO
     */
    public PksiDocumentResponse mapToResponse(PksiDocument document) {
        String userId = extractUserId(document);
        String userName = extractUserName(document);
        
        return PksiDocumentResponse.builder()
                .id(document.getId().toString())
                .userId(userId)
                .userName(userName)
                // Header
                .namaPksi(document.getNamaPksi())
                .tanggalPengajuan(formatDate(document.getTanggalPengajuan()))
                // Section 1
                .deskripsiPksi(document.getDeskripsiPksi())
                .mengapaPksiDiperlukan(document.getMengapaPksiDiperlukan())
                .kapanHarusDiselesaikan(document.getKapanDiselesaikan())
                .picSatkerBA(document.getPicSatker())
                // Section 2
                .kegunaanPksi(document.getKegunaanPksi())
                .tujuanPksi(document.getTujuanPksi())
                .targetPksi(document.getTargetPksi())
                // Section 3
                .ruangLingkup(document.getRuangLingkup())
                .batasanPksi(document.getBatasanPksi())
                .hubunganSistemLain(document.getHubunganSistemLain())
                .asumsi(document.getAsumsi())
                // Section 4
                .batasanDesain(document.getBatasanDesain())
                .risikoBisnis(document.getRisikoBisnis())
                .risikoSuksesPksi(document.getRisikoSuksesPksi())
                .pengendalianRisiko(document.getPengendalianRisiko())
                // Section 5
                .pengelolaAplikasi(document.getPengelolaAplikasi())
                .penggunaAplikasi(document.getPenggunaAplikasi())
                .programInisiatifRbsi(document.getProgramInisiatifRbsi())
                .fungsiAplikasi(document.getFungsiAplikasi())
                .informasiYangDikelola(document.getInformasiYangDikelola())
                .dasarPeraturan(document.getDasarPeraturan())
                // Section 6
                .tahap1Awal(formatDate(document.getTahap1Awal()))
                .tahap1Akhir(formatDate(document.getTahap1Akhir()))
                .tahap5Awal(formatDate(document.getTahap5Awal()))
                .tahap5Akhir(formatDate(document.getTahap5Akhir()))
                .tahap7Awal(formatDate(document.getTahap7Awal()))
                .tahap7Akhir(formatDate(document.getTahap7Akhir()))
                // Section 7
                .rencanaPengelolaan(document.getRencanaPengelolaan())
                // Legacy
                .tujuanPengajuan(document.getTujuanPengajuan())
                .kapanDiselesaikan(document.getKapanDiselesaikan())
                .picSatker(document.getPicSatker())
                // Status & Metadata
                .status(document.getStatus() != null ? document.getStatus().name() : null)
                .createdAt(document.getCreatedAt() != null ? document.getCreatedAt().toString() : null)
                .updatedAt(document.getUpdatedAt() != null ? document.getUpdatedAt().toString() : null)
                .build();
    }

    // ==================== HELPER METHODS ====================

    public LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public String formatDate(LocalDate date) {
        return date != null ? date.toString() : null;
    }

    public String resolveKapanDiselesaikan(PksiDocumentRequest request) {
        if (request.getKapanHarusDiselesaikan() != null && !request.getKapanHarusDiselesaikan().isEmpty()) {
            return request.getKapanHarusDiselesaikan();
        }
        return request.getKapanDiselesaikan();
    }

    public String resolvePicSatker(PksiDocumentRequest request) {
        if (request.getPicSatkerBA() != null && !request.getPicSatkerBA().isEmpty()) {
            return request.getPicSatkerBA();
        }
        return request.getPicSatker();
    }

    private String extractUserId(PksiDocument document) {
        if (document.getUser() != null && document.getUser().getUuid() != null) {
            return document.getUser().getUuid().toString();
        }
        return null;
    }

    private String extractUserName(PksiDocument document) {
        if (document.getUser() != null) {
            return document.getUser().getFullName();
        }
        return null;
    }
}
