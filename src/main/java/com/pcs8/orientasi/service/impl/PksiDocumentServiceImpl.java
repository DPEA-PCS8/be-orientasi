package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.PksiDocumentRequest;
import com.pcs8.orientasi.domain.dto.response.PksiDocumentResponse;
import com.pcs8.orientasi.domain.entity.MstUser;
import com.pcs8.orientasi.domain.entity.PksiDocument;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstUserRepository;
import com.pcs8.orientasi.repository.PksiDocumentRepository;
import com.pcs8.orientasi.service.PksiDocumentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation untuk dokumen T.01 (PKSI) - Full Version
 */
@Service
@RequiredArgsConstructor
public class PksiDocumentServiceImpl implements PksiDocumentService {

    private static final Logger log = LoggerFactory.getLogger(PksiDocumentServiceImpl.class);
    private static final String PKSI_NOT_FOUND = "PKSI document not found";

    private final PksiDocumentRepository pksiDocumentRepository;
    private final MstUserRepository userRepository;

    @Override
    @Transactional
    public PksiDocumentResponse createDocument(PksiDocumentRequest request, UUID userId) {
        log.info("Creating PKSI document for user: {}", userId);

        MstUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PksiDocument document = PksiDocument.builder()
                .user(user)
                // Header
                .namaPksi(request.getNamaPksi())
                .tanggalPengajuan(parseDate(request.getTanggalPengajuan()))
                // Section 1
                .deskripsiPksi(request.getDeskripsiPksi())
                .mengapaPksiDiperlukan(request.getMengapaPksiDiperlukan())
                .kapanDiselesaikan(getKapanDiselesaikan(request))
                .picSatker(getPicSatker(request))
                // Section 2
                .kegunaanPksi(request.getKegunaanPksi())
                .tujuanPksi(request.getTujuanPksi())
                .targetPksi(request.getTargetPksi())
                // Section 3
                .ruangLingkup(request.getRuangLingkup())
                .batasanPksi(request.getBatasanPksi())
                .hubunganSistemLain(request.getHubunganSistemLain())
                .asumsi(request.getAsumsi())
                // Section 4
                .batasanDesain(request.getBatasanDesain())
                .risikoBisnis(request.getRisikoBisnis())
                .risikoSuksesPksi(request.getRisikoSuksesPksi())
                .pengendalianRisiko(request.getPengendalianRisiko())
                // Section 5
                .pengelolaAplikasi(request.getPengelolaAplikasi())
                .penggunaAplikasi(request.getPenggunaAplikasi())
                .programInisiatifRbsi(request.getProgramInisiatifRbsi())
                .fungsiAplikasi(request.getFungsiAplikasi())
                .informasiYangDikelola(request.getInformasiYangDikelola())
                .dasarPeraturan(request.getDasarPeraturan())
                // Section 6
                .tahap1Awal(parseDate(request.getTahap1Awal()))
                .tahap1Akhir(parseDate(request.getTahap1Akhir()))
                .tahap5Awal(parseDate(request.getTahap5Awal()))
                .tahap5Akhir(parseDate(request.getTahap5Akhir()))
                .tahap7Awal(parseDate(request.getTahap7Awal()))
                .tahap7Akhir(parseDate(request.getTahap7Akhir()))
                // Section 7
                .rencanaPengelolaan(request.getRencanaPengelolaan())
                // Legacy
                .tujuanPengajuan(request.getTujuanPengajuan())
                // Status
                .status(PksiDocument.DocumentStatus.PENDING)
                .build();

        PksiDocument saved = pksiDocumentRepository.save(document);
        log.info("PKSI document created with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PksiDocumentResponse getDocumentById(UUID id) {
        log.info("Fetching PKSI document: {}", id);
        
        PksiDocument document = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        return mapToResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PksiDocumentResponse> getAllDocuments() {
        log.info("Fetching all PKSI documents");
        
        return pksiDocumentRepository.findAllWithUser().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PksiDocumentResponse> getDocumentsByUser(UUID userId) {
        log.info("Fetching PKSI documents for user: {}", userId);
        
        return pksiDocumentRepository.findByUserUuid(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PksiDocumentResponse> searchDocuments(String search, String status, Pageable pageable) {
        log.info("Searching PKSI documents with search: {}, status: {}", search, status);
        
        return pksiDocumentRepository.searchDocuments(search, status, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public PksiDocumentResponse updateDocument(UUID id, PksiDocumentRequest request) {
        log.info("Updating PKSI document: {}", id);

        PksiDocument document = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        // Header
        document.setNamaPksi(request.getNamaPksi());
        document.setTanggalPengajuan(parseDate(request.getTanggalPengajuan()));
        // Section 1
        document.setDeskripsiPksi(request.getDeskripsiPksi());
        document.setMengapaPksiDiperlukan(request.getMengapaPksiDiperlukan());
        document.setKapanDiselesaikan(getKapanDiselesaikan(request));
        document.setPicSatker(getPicSatker(request));
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

        PksiDocument updated = pksiDocumentRepository.save(document);
        log.info("PKSI document updated: {}", updated.getId());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteDocument(UUID id) {
        log.info("Deleting PKSI document: {}", id);

        if (!pksiDocumentRepository.existsById(id)) {
            throw new ResourceNotFoundException(PKSI_NOT_FOUND);
        }

        pksiDocumentRepository.deleteById(id);
        log.info("PKSI document deleted: {}", id);
    }

    @Override
    @Transactional
    public PksiDocumentResponse updateStatus(UUID id, String status) {
        log.info("Updating status for PKSI document: {}", id);

        PksiDocument document = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        PksiDocument.DocumentStatus newStatus;
        try {
            newStatus = PksiDocument.DocumentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status value provided for document: {}", id);
            String validStatuses = Arrays.toString(PksiDocument.DocumentStatus.values());
            throw new BadRequestException("Invalid status value. Valid values are: " + validStatuses);
        }
        
        document.setStatus(newStatus);

        pksiDocumentRepository.save(document);
        log.info("PKSI document status updated: {} -> {}", id, newStatus);

        // Re-fetch with user to avoid lazy loading issues
        PksiDocument updated = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        return mapToResponse(updated);
    }

    // ==================== HELPER METHODS ====================

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.toString() : null;
    }

    private String getKapanDiselesaikan(PksiDocumentRequest request) {
        // Support both new and legacy field names
        if (request.getKapanHarusDiselesaikan() != null && !request.getKapanHarusDiselesaikan().isEmpty()) {
            return request.getKapanHarusDiselesaikan();
        }
        return request.getKapanDiselesaikan();
    }

    private String getPicSatker(PksiDocumentRequest request) {
        // Support both new and legacy field names
        if (request.getPicSatkerBA() != null && !request.getPicSatkerBA().isEmpty()) {
            return request.getPicSatkerBA();
        }
        return request.getPicSatker();
    }

    private PksiDocumentResponse mapToResponse(PksiDocument document) {
        String userId = null;
        String userName = null;
        if (document.getUser() != null) {
            userId = document.getUser().getUuid() != null ? document.getUser().getUuid().toString() : null;
            userName = document.getUser().getFullName();
        }
        
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
}
