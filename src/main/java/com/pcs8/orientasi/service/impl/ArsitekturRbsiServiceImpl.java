package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.config.UserContext;
import com.pcs8.orientasi.domain.dto.request.ArsitekturRbsiRequest;
import com.pcs8.orientasi.domain.dto.response.AplikasiResponse;
import com.pcs8.orientasi.domain.dto.response.ArsitekturRbsiResponse;
import com.pcs8.orientasi.domain.dto.response.SkpaResponse;
import com.pcs8.orientasi.domain.dto.response.SubKategoriResponse;
import com.pcs8.orientasi.domain.entity.*;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.*;
import com.pcs8.orientasi.service.ArsitekturRbsiService;
import com.pcs8.orientasi.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArsitekturRbsiServiceImpl implements ArsitekturRbsiService {

    private static final Logger log = LoggerFactory.getLogger(ArsitekturRbsiServiceImpl.class);
    private static final String ENTITY_NAME = "Arsitektur RBSI";

    private final MstArsitekturRbsiRepository arsitekturRepository;
    private final RbsiRepository rbsiRepository;
    private final MstSubKategoriRepository subKategoriRepository;
    private final MstAplikasiRepository aplikasiRepository;
    private final RbsiInisiatifRepository inisiatifRepository;
    private final MstSkpaRepository skpaRepository;
    private final AuditService auditService;
    private final UserContext userContext;

    @Override
    @Transactional
    public ArsitekturRbsiResponse create(ArsitekturRbsiRequest request) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        MstArsitekturRbsi arsitektur = buildNewArsitektur(request);
        MstArsitekturRbsi saved = arsitekturRepository.save(arsitektur);
        log.info("ArsitekturRbsi created: {}", saved.getId());
        
        // Audit log
        ArsitekturRbsiResponse response = mapToResponse(saved);
        auditService.logCreate(ENTITY_NAME, saved.getId(), response, userId, username);
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ArsitekturRbsiResponse getById(UUID id) {
        MstArsitekturRbsi arsitektur = arsitekturRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Arsitektur RBSI tidak ditemukan"));
        return mapToResponse(arsitektur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArsitekturRbsiResponse> getByRbsiId(UUID rbsiId) {
        return arsitekturRepository.findByRbsiIdWithRelations(rbsiId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ArsitekturRbsiResponse update(UUID id, ArsitekturRbsiRequest request) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        MstArsitekturRbsi existing = arsitekturRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Arsitektur RBSI tidak ditemukan"));

        // Capture old value untuk audit
        ArsitekturRbsiResponse oldValue = mapToResponse(existing);

        updateArsitekturFromRequest(existing, request);
        MstArsitekturRbsi saved = arsitekturRepository.save(existing);
        log.info("ArsitekturRbsi updated: {}", saved.getId());
        
        // Audit log
        ArsitekturRbsiResponse newValue = mapToResponse(saved);
        auditService.logUpdate(ENTITY_NAME, id, oldValue, newValue, userId, username);
        
        return newValue;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        MstArsitekturRbsi arsitektur = arsitekturRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Arsitektur RBSI tidak ditemukan"));
        
        // Capture old value untuk audit sebelum delete
        ArsitekturRbsiResponse oldValue = mapToResponse(arsitektur);
        
        arsitekturRepository.delete(arsitektur);
        log.info("ArsitekturRbsi deleted: {}", id);
        
        // Audit log
        auditService.logDelete(ENTITY_NAME, id, oldValue, userId, username);
    }

    @Override
    @Transactional
    public void deleteByRbsiId(UUID rbsiId) {
        arsitekturRepository.deleteByRbsiId(rbsiId);
        log.info("All ArsitekturRbsi deleted for RBSI: {}", rbsiId);
    }

    @Override
    @Transactional
    public List<ArsitekturRbsiResponse> bulkCreate(List<ArsitekturRbsiRequest> requests) {
        List<ArsitekturRbsiResponse> responses = new ArrayList<>();
        for (ArsitekturRbsiRequest request : requests) {
            responses.add(create(request));
        }
        return responses;
    }

    @Override
    @Transactional
    public List<ArsitekturRbsiResponse> bulkUpdate(List<ArsitekturRbsiRequest> requests) {
        List<ArsitekturRbsiResponse> responses = new ArrayList<>();
        for (ArsitekturRbsiRequest request : requests) {
            if (request.getRbsiId() != null) {
                responses.add(create(request));
            }
        }
        return responses;
    }

    private MstArsitekturRbsi buildNewArsitektur(ArsitekturRbsiRequest request) {
        Rbsi rbsi = rbsiRepository.findById(request.getRbsiId())
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        MstSubKategori subKategori = subKategoriRepository.findById(request.getSubKategoriId())
                .orElseThrow(() -> new ResourceNotFoundException("Sub Kategori tidak ditemukan"));

        MstAplikasi aplikasiBaseline = null;
        if (request.getAplikasiBaselineId() != null) {
            aplikasiBaseline = aplikasiRepository.findById(request.getAplikasiBaselineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Aplikasi Baseline tidak ditemukan"));
        }

        MstAplikasi aplikasiTarget = null;
        if (request.getAplikasiTargetId() != null) {
            aplikasiTarget = aplikasiRepository.findById(request.getAplikasiTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Aplikasi Target tidak ditemukan"));
        }

        RbsiInisiatif inisiatif = null;
        if (request.getInisiatifId() != null) {
            inisiatif = inisiatifRepository.findById(request.getInisiatifId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inisiatif tidak ditemukan"));
        }

        MstSkpa skpa = null;
        if (request.getSkpaId() != null) {
            skpa = skpaRepository.findById(request.getSkpaId())
                    .orElseThrow(() -> new ResourceNotFoundException("SKPA tidak ditemukan"));
        }

        return MstArsitekturRbsi.builder()
                .rbsi(rbsi)
                .subKategori(subKategori)
                .aplikasiBaseline(aplikasiBaseline)
                .aplikasiTarget(aplikasiTarget)
                .action(request.getAction())
                .yearStatuses(request.getYearStatuses())
                .inisiatif(inisiatif)
                .skpa(skpa)
                .build();
    }

    private void updateArsitekturFromRequest(MstArsitekturRbsi existing, ArsitekturRbsiRequest request) {
        Rbsi rbsi = rbsiRepository.findById(request.getRbsiId())
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));
        existing.setRbsi(rbsi);

        MstSubKategori subKategori = subKategoriRepository.findById(request.getSubKategoriId())
                .orElseThrow(() -> new ResourceNotFoundException("Sub Kategori tidak ditemukan"));
        existing.setSubKategori(subKategori);

        if (request.getAplikasiBaselineId() != null) {
            MstAplikasi aplikasiBaseline = aplikasiRepository.findById(request.getAplikasiBaselineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Aplikasi Baseline tidak ditemukan"));
            existing.setAplikasiBaseline(aplikasiBaseline);
        } else {
            existing.setAplikasiBaseline(null);
        }

        if (request.getAplikasiTargetId() != null) {
            MstAplikasi aplikasiTarget = aplikasiRepository.findById(request.getAplikasiTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Aplikasi Target tidak ditemukan"));
            existing.setAplikasiTarget(aplikasiTarget);
        } else {
            existing.setAplikasiTarget(null);
        }

        existing.setAction(request.getAction());
        existing.setYearStatuses(request.getYearStatuses());

        if (request.getInisiatifId() != null) {
            RbsiInisiatif inisiatif = inisiatifRepository.findById(request.getInisiatifId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inisiatif tidak ditemukan"));
            existing.setInisiatif(inisiatif);
        } else {
            existing.setInisiatif(null);
        }

        if (request.getSkpaId() != null) {
            MstSkpa skpa = skpaRepository.findById(request.getSkpaId())
                    .orElseThrow(() -> new ResourceNotFoundException("SKPA tidak ditemukan"));
            existing.setSkpa(skpa);
        } else {
            existing.setSkpa(null);
        }
    }

    private ArsitekturRbsiResponse mapToResponse(MstArsitekturRbsi entity) {
        ArsitekturRbsiResponse.ArsitekturRbsiResponseBuilder builder = ArsitekturRbsiResponse.builder()
                .id(entity.getId())
                .action(entity.getAction())
                .yearStatuses(entity.getYearStatuses())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // RBSI
        if (entity.getRbsi() != null) {
            builder.rbsiId(entity.getRbsi().getId())
                   .rbsiPeriode(entity.getRbsi().getPeriode());
        }

        // Sub Kategori
        if (entity.getSubKategori() != null) {
            builder.subKategori(SubKategoriResponse.builder()
                    .id(entity.getSubKategori().getId())
                    .kode(entity.getSubKategori().getKode())
                    .nama(entity.getSubKategori().getNama())
                    .categoryCode(entity.getSubKategori().getCategoryCode())
                    .build());
        }

        // Aplikasi Baseline
        if (entity.getAplikasiBaseline() != null) {
            builder.aplikasiBaseline(AplikasiResponse.builder()
                    .id(entity.getAplikasiBaseline().getId())
                    .kodeAplikasi(entity.getAplikasiBaseline().getKodeAplikasi())
                    .namaAplikasi(entity.getAplikasiBaseline().getNamaAplikasi())
                    .build());
        }

        // Aplikasi Target
        if (entity.getAplikasiTarget() != null) {
            builder.aplikasiTarget(AplikasiResponse.builder()
                    .id(entity.getAplikasiTarget().getId())
                    .kodeAplikasi(entity.getAplikasiTarget().getKodeAplikasi())
                    .namaAplikasi(entity.getAplikasiTarget().getNamaAplikasi())
                    .build());
        }

        // Inisiatif
        if (entity.getInisiatif() != null) {
            ArsitekturRbsiResponse.InisiatifSimpleResponse.InisiatifSimpleResponseBuilder iniBuilder =
                    ArsitekturRbsiResponse.InisiatifSimpleResponse.builder()
                            .id(entity.getInisiatif().getId())
                            .nomorInisiatif(entity.getInisiatif().getNomorInisiatif())
                            .namaInisiatif(entity.getInisiatif().getNamaInisiatif());

            if (entity.getInisiatif().getProgram() != null) {
                iniBuilder.programId(entity.getInisiatif().getProgram().getId())
                          .namaProgram(entity.getInisiatif().getProgram().getNamaProgram());
            }

            builder.inisiatif(iniBuilder.build());
        }

        // SKPA
        if (entity.getSkpa() != null) {
            builder.skpa(SkpaResponse.builder()
                    .id(entity.getSkpa().getId())
                    .kodeSkpa(entity.getSkpa().getKodeSkpa())
                    .namaSkpa(entity.getSkpa().getNamaSkpa())
                    .build());
        }

        return builder.build();
    }
}
