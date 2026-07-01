package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.client.catalog.ApplicationCatalogClient;
import com.pcs8.orientasi.client.catalog.dto.aplikasi.CatalogAplikasiResponse;
import com.pcs8.orientasi.client.catalog.dto.common.CatalogPageResponse;
import com.pcs8.orientasi.config.UserContext;
import com.pcs8.orientasi.domain.dto.request.AplikasiRequest;
import com.pcs8.orientasi.domain.dto.request.AplikasiStatusRequest;
import com.pcs8.orientasi.domain.dto.response.AplikasiListResponse;
import com.pcs8.orientasi.domain.dto.response.AplikasiResponse;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstBidangRepository;
import com.pcs8.orientasi.repository.MstSkpaRepository;
import com.pcs8.orientasi.service.AplikasiHistorisService;
import com.pcs8.orientasi.service.AplikasiService;
import com.pcs8.orientasi.service.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AplikasiServiceImpl implements AplikasiService {

    private static final String ENTITY_NAME = "Aplikasi";
    private static final int FETCH_ALL_SIZE = 1000;

    private final ApplicationCatalogClient catalogClient;
    private final CatalogAplikasiMapper mapper;
    private final MstBidangRepository bidangRepository;
    private final MstSkpaRepository skpaRepository;
    private final AuditService auditService;
    private final UserContext userContext;
    private final AplikasiHistorisService aplikasiHistorisService;

    public AplikasiServiceImpl(
            ApplicationCatalogClient catalogClient,
            CatalogAplikasiMapper mapper,
            MstBidangRepository bidangRepository,
            MstSkpaRepository skpaRepository,
            AuditService auditService,
            UserContext userContext,
            AplikasiHistorisService aplikasiHistorisService
    ) {
        this.catalogClient = catalogClient;
        this.mapper = mapper;
        this.bidangRepository = bidangRepository;
        this.skpaRepository = skpaRepository;
        this.auditService = auditService;
        this.userContext = userContext;
        this.aplikasiHistorisService = aplikasiHistorisService;
    }

    @Override
    public AplikasiResponse create(AplikasiRequest request) {
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();

        CatalogAplikasiResponse catalogResponse = catalogClient.create(mapper.toCatalogRequest(request));
        AplikasiResponse response = mapper.toAplikasiResponse(catalogResponse);

        log.info("Aplikasi created: {} via catalog", response.getKodeAplikasi());
        auditService.logCreate(ENTITY_NAME, response.getId(), response, userId, username);
        aplikasiHistorisService.onAplikasiUpdated(response.getId(), "Aplikasi baru dibuat");

        return response;
    }

    @Override
    public AplikasiResponse getById(UUID id) {
        CatalogAplikasiResponse catalogResponse = catalogClient.getById(id);
        return mapper.toAplikasiResponse(catalogResponse);
    }

    @Override
    public AplikasiResponse getByKode(String kode) {
        // Catalog searches by nama_aplikasi (partial), filter for exact match
        CatalogPageResponse<CatalogAplikasiResponse> page = catalogClient.search(
                kode, null, null, null, null, null, 0, 50);

        return page.getContent().stream()
                .filter(c -> kode.equalsIgnoreCase(c.getNamaAplikasi()))
                .findFirst()
                .map(mapper::toAplikasiResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Aplikasi dengan kode '" + kode + "' tidak ditemukan"));
    }

    @Override
    public List<AplikasiResponse> getAll() {
        return catalogClient.search(null, null, null, null, null, null, 1, FETCH_ALL_SIZE)
                .getContent().stream()
                .map(mapper::toAplikasiResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AplikasiResponse> getAllForDropdown() {
        return catalogClient.search(null, null, null, null, null, null, 1, FETCH_ALL_SIZE)
                .getContent().stream()
                .map(mapper::toAplikasiResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<AplikasiResponse> search(String search, UUID bidangId, UUID skpaId, String status, Pageable pageable) {
        String bidangKode = resolveKodeBidang(bidangId);
        String skpaKode = resolveKodeSkpa(skpaId);

        CatalogPageResponse<CatalogAplikasiResponse> catalogPage = catalogClient.search(
                search, status, skpaKode, bidangKode, null, null,
                pageable.getPageNumber(), pageable.getPageSize());

        List<AplikasiResponse> content = catalogPage.getContent().stream()
                .map(mapper::toAplikasiResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, catalogPage.getTotalElements());
    }

    @Override
    public Page<AplikasiListResponse> searchLight(String search, UUID bidangId, UUID skpaId, String status, Pageable pageable) {
        String bidangKode = resolveKodeBidang(bidangId);
        String skpaKode = resolveKodeSkpa(skpaId);

        CatalogPageResponse<CatalogAplikasiResponse> catalogPage = catalogClient.search(
                search, status, skpaKode, bidangKode, null, null,
                pageable.getPageNumber(), pageable.getPageSize());

        List<AplikasiListResponse> content = catalogPage.getContent().stream()
                .map(mapper::toAplikasiListResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, catalogPage.getTotalElements());
    }

    @Override
    public List<AplikasiResponse> searchList(String search, UUID bidangId, UUID skpaId, String status) {
        String bidangKode = resolveKodeBidang(bidangId);
        String skpaKode = resolveKodeSkpa(skpaId);

        return catalogClient.search(search, status, skpaKode, bidangKode, null, null, 0, FETCH_ALL_SIZE)
                .getContent().stream()
                .map(mapper::toAplikasiResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AplikasiResponse update(UUID id, AplikasiRequest request) {
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();

        AplikasiResponse oldValue = getById(id);
        CatalogAplikasiResponse catalogResponse = catalogClient.update(id, mapper.toCatalogRequest(request));
        AplikasiResponse newValue = mapper.toAplikasiResponse(catalogResponse);

        log.info("Aplikasi updated: {} via catalog", newValue.getKodeAplikasi());
        auditService.logUpdate(ENTITY_NAME, id, oldValue, newValue, userId, username);
        aplikasiHistorisService.onAplikasiUpdated(id, request.getKeteranganPerubahan());

        return newValue;
    }

    @Override
    public AplikasiResponse updateStatus(UUID id, String status) {
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();

        AplikasiResponse oldValue = getById(id);
        CatalogAplikasiResponse catalogResponse = catalogClient.updateStatus(id, mapper.toCatalogStatusRequest(status));
        AplikasiResponse newValue = mapper.toAplikasiResponse(catalogResponse);

        log.info("Aplikasi status updated: {} -> {}", id, status);
        auditService.logUpdate(ENTITY_NAME, id, oldValue, newValue, userId, username);
        aplikasiHistorisService.onAplikasiUpdated(id, "Status diubah menjadi " + status);

        return newValue;
    }

    @Override
    public AplikasiResponse updateStatusWithDetails(UUID id, AplikasiStatusRequest request) {
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();

        AplikasiResponse oldValue = getById(id);
        CatalogAplikasiResponse catalogResponse = catalogClient.updateStatus(id, mapper.toCatalogStatusRequest(request));
        AplikasiResponse newValue = mapper.toAplikasiResponse(catalogResponse);

        log.info("Aplikasi status updated with details: {} -> {}", id, request.getStatus());
        auditService.logUpdate(ENTITY_NAME, id, oldValue, newValue, userId, username);
        aplikasiHistorisService.onAplikasiUpdated(id, "Status diubah menjadi " + request.getStatus());

        return newValue;
    }

    @Override
    public void delete(UUID id) {
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();

        AplikasiResponse oldValue = getById(id);
        catalogClient.delete(id);

        log.info("Aplikasi deleted: {} via catalog", id);
        auditService.logDelete(ENTITY_NAME, id, oldValue, userId, username);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private String resolveKodeBidang(UUID bidangId) {
        if (bidangId == null) return null;
        return bidangRepository.findById(bidangId)
                .map(b -> b.getKodeBidang())
                .orElse(null);
    }

    private String resolveKodeSkpa(UUID skpaId) {
        if (skpaId == null) return null;
        return skpaRepository.findById(skpaId)
                .map(s -> s.getKodeSkpa())
                .orElse(null);
    }
}
