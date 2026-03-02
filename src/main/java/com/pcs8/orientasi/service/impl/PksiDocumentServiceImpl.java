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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation untuk dokumen T.01 (PKSI) - MVP Version
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
                .namaPksi(request.getNamaPksi())
                .deskripsiPksi(request.getDeskripsiPksi())
                .tujuanPengajuan(request.getTujuanPengajuan())
                .kapanDiselesaikan(request.getKapanDiselesaikan())
                .picSatker(request.getPicSatker())
                .tujuanPksi(request.getTujuanPksi())
                .ruangLingkup(request.getRuangLingkup())
                .pengelolaAplikasi(request.getPengelolaAplikasi())
                .penggunaAplikasi(request.getPenggunaAplikasi())
                .programInisiatifRbsi(request.getProgramInisiatifRbsi())
                .fungsiAplikasi(request.getFungsiAplikasi())
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
        
        PksiDocument document = pksiDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        return mapToResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PksiDocumentResponse> getAllDocuments() {
        log.info("Fetching all PKSI documents");
        
        return pksiDocumentRepository.findAll().stream()
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
    @Transactional
    public PksiDocumentResponse updateDocument(UUID id, PksiDocumentRequest request) {
        log.info("Updating PKSI document: {}", id);

        PksiDocument document = pksiDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        document.setNamaPksi(request.getNamaPksi());
        document.setDeskripsiPksi(request.getDeskripsiPksi());
        document.setTujuanPengajuan(request.getTujuanPengajuan());
        document.setKapanDiselesaikan(request.getKapanDiselesaikan());
        document.setPicSatker(request.getPicSatker());
        document.setTujuanPksi(request.getTujuanPksi());
        document.setRuangLingkup(request.getRuangLingkup());
        document.setPengelolaAplikasi(request.getPengelolaAplikasi());
        document.setPenggunaAplikasi(request.getPenggunaAplikasi());
        document.setProgramInisiatifRbsi(request.getProgramInisiatifRbsi());
        document.setFungsiAplikasi(request.getFungsiAplikasi());

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

        PksiDocument document = pksiDocumentRepository.findById(id)
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

        PksiDocument updated = pksiDocumentRepository.save(document);
        log.info("PKSI document status updated: {} -> {}", id, newStatus);

        return mapToResponse(updated);
    }

    private PksiDocumentResponse mapToResponse(PksiDocument document) {
        return PksiDocumentResponse.builder()
                .id(document.getId().toString())
                .userId(document.getUser().getUuid().toString())
                .userName(document.getUser().getFullName())
                .namaPksi(document.getNamaPksi())
                .deskripsiPksi(document.getDeskripsiPksi())
                .tujuanPengajuan(document.getTujuanPengajuan())
                .kapanDiselesaikan(document.getKapanDiselesaikan())
                .picSatker(document.getPicSatker())
                .tujuanPksi(document.getTujuanPksi())
                .ruangLingkup(document.getRuangLingkup())
                .pengelolaAplikasi(document.getPengelolaAplikasi())
                .penggunaAplikasi(document.getPenggunaAplikasi())
                .programInisiatifRbsi(document.getProgramInisiatifRbsi())
                .fungsiAplikasi(document.getFungsiAplikasi())
                .status(document.getStatus() != null ? document.getStatus().name() : null)
                .createdAt(document.getCreatedAt() != null ? document.getCreatedAt().toString() : null)
                .updatedAt(document.getUpdatedAt() != null ? document.getUpdatedAt().toString() : null)
                .build();
    }
}
