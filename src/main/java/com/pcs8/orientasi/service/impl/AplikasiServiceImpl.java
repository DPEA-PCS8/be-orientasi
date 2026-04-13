package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.config.UserContext;
import com.pcs8.orientasi.domain.dto.request.*;
import com.pcs8.orientasi.domain.dto.response.*;
import com.pcs8.orientasi.domain.entity.*;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstAplikasiRepository;
import com.pcs8.orientasi.repository.MstBidangRepository;
import com.pcs8.orientasi.repository.MstSkpaRepository;
import com.pcs8.orientasi.repository.MstSubKategoriRepository;
import com.pcs8.orientasi.repository.MstVariableRepository;
import com.pcs8.orientasi.service.AplikasiHistorisService;
import com.pcs8.orientasi.service.AplikasiService;
import com.pcs8.orientasi.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AplikasiServiceImpl implements AplikasiService {

    private static final Logger log = LoggerFactory.getLogger(AplikasiServiceImpl.class);
    private static final String ENTITY_NAME = "Aplikasi";

    private final MstAplikasiRepository aplikasiRepository;
    private final MstBidangRepository bidangRepository;
    private final MstSkpaRepository skpaRepository;
    private final MstSubKategoriRepository subKategoriRepository;
    private final MstVariableRepository variableRepository;
    private final AuditService auditService;
    private final UserContext userContext;
    private final AplikasiHistorisService aplikasiHistorisService;

    public AplikasiServiceImpl(
            MstAplikasiRepository aplikasiRepository,
            MstBidangRepository bidangRepository,
            MstSkpaRepository skpaRepository,
            MstSubKategoriRepository subKategoriRepository,
            MstVariableRepository variableRepository,
            AuditService auditService,
            UserContext userContext,
            @Lazy AplikasiHistorisService aplikasiHistorisService
    ) {
        this.aplikasiRepository = aplikasiRepository;
        this.bidangRepository = bidangRepository;
        this.skpaRepository = skpaRepository;
        this.subKategoriRepository = subKategoriRepository;
        this.variableRepository = variableRepository;
        this.auditService = auditService;
        this.userContext = userContext;
        this.aplikasiHistorisService = aplikasiHistorisService;
    }

    @Override
    @Transactional
    public AplikasiResponse create(AplikasiRequest request) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        String kode = request.getKodeAplikasi().toUpperCase().trim();
        if (aplikasiRepository.existsByKodeAplikasi(kode)) {
            throw new BadRequestException("Aplikasi dengan kode '" + kode + "' sudah ada");
        }

        MstAplikasi aplikasi = buildAplikasiFromRequest(request, kode);
        setBidangAndSkpa(request, aplikasi);
        setEntityDetails(request, aplikasi);

        MstAplikasi saved = aplikasiRepository.save(aplikasi);
        log.info("Aplikasi created: {} - {}", saved.getKodeAplikasi(), saved.getNamaAplikasi());
        
        // Auto-create snapshot for current year
        try {
            int currentYear = java.time.Year.now().getValue();
            aplikasiHistorisService.createOrUpdateSnapshot(saved.getId(), currentYear, "CREATED");
            log.info("Auto-created snapshot for new aplikasi: {} - year {}", saved.getKodeAplikasi(), currentYear);
        } catch (Exception e) {
            log.warn("Failed to auto-create snapshot for aplikasi {}: {}", saved.getKodeAplikasi(), e.getMessage());
        }
        
        // Audit log
        AplikasiResponse response = mapToResponse(saved);
        auditService.logCreate(ENTITY_NAME, saved.getId(), response, userId, username);
        
        return response;
    }

    private MstAplikasi buildAplikasiFromRequest(AplikasiRequest request, String kode) {
        MstAplikasi.MstAplikasiBuilder builder = MstAplikasi.builder()
                .kodeAplikasi(kode)
                .namaAplikasi(request.getNamaAplikasi().trim())
                .deskripsi(request.getDeskripsi())
                .statusAplikasi(request.getStatusAplikasi())
                .tanggalImplementasi(request.getTanggalImplementasi())
                .akses(null) // Will be derived from URLs in setEntityDetails
                .prosesDataPribadi(request.getProsesDataPribadi())
                .dataPribadiDiproses(request.getDataPribadiDiproses())
                .urls(new ArrayList<>())
                .satkerInternals(new ArrayList<>())
                .penggunaEksternals(new ArrayList<>())
                .komunikasiSistems(new ArrayList<>())
                .penghargaans(new ArrayList<>());

        if (request.getIdleInfo() != null) {
            builder.kategoriIdle(request.getIdleInfo().getKategoriIdle())
                    .alasanIdle(request.getIdleInfo().getAlasanIdle())
                    .rencanaPengakhiran(request.getIdleInfo().getRencanaPengakhiran())
                    .alasanBelumDiakhiri(request.getIdleInfo().getAlasanBelumDiakhiri());
        }

        return builder.build();
    }

    private void setBidangAndSkpa(AplikasiRequest request, MstAplikasi aplikasi) {
        // Bidang
        if (request.getBidangId() != null) {
            MstBidang bidang = bidangRepository.findById(request.getBidangId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bidang tidak ditemukan"));
            aplikasi.setBidang(bidang);
        } else {
            aplikasi.setBidang(null);
        }
        // SKPA
        if (request.getSkpaId() != null) {
            MstSkpa skpa = skpaRepository.findById(request.getSkpaId())
                    .orElseThrow(() -> new ResourceNotFoundException("SKPA tidak ditemukan"));
            aplikasi.setSkpa(skpa);
        } else {
            aplikasi.setSkpa(null);
        }
        // SubKategori
        if (request.getSubKategoriId() != null) {
            MstSubKategori subKategori = subKategoriRepository.findById(request.getSubKategoriId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sub Kategori tidak ditemukan"));
            aplikasi.setSubKategori(subKategori);
        } else {
            aplikasi.setSubKategori(null);
        }
    }


    private void setEntityDetails(AplikasiRequest request, MstAplikasi aplikasi) {
        // URLs
        aplikasi.getUrls().clear();
        if (request.getUrls() != null) {
            request.getUrls().forEach(urlReq -> aplikasi.getUrls().add(
                    AplikasiUrl.builder()
                            .aplikasi(aplikasi)
                            .url(urlReq.getUrl())
                            .tipeAkses(urlReq.getTipeAkses())
                            .keterangan(urlReq.getKeterangan())
                            .build()
            ));
        }
        // Derive akses from URL tipe_akses values
        String derivedAkses = aplikasi.getUrls().stream()
                .map(AplikasiUrl::getTipeAkses)
                .filter(tipeAkses -> tipeAkses != null && !tipeAkses.isBlank())
                .distinct()
                .collect(Collectors.joining(","));
        aplikasi.setAkses(derivedAkses.isEmpty() ? null : derivedAkses);
        // Satker Internals
        aplikasi.getSatkerInternals().clear();
        if (request.getSatkerInternals() != null) {
            request.getSatkerInternals().forEach(satkerReq -> aplikasi.getSatkerInternals().add(
                    AplikasiSatkerInternal.builder()
                            .aplikasi(aplikasi)
                            .namaSatker(satkerReq.getNamaSatker())
                            .keterangan(satkerReq.getKeterangan())
                            .build()
            ));
        }
        // Pengguna Eksternals
        aplikasi.getPenggunaEksternals().clear();
        if (request.getPenggunaEksternals() != null) {
            request.getPenggunaEksternals().forEach(penggunaReq -> aplikasi.getPenggunaEksternals().add(
                    AplikasiPenggunaEksternal.builder()
                            .aplikasi(aplikasi)
                            .namaPengguna(penggunaReq.getNamaPengguna())
                            .keterangan(penggunaReq.getKeterangan())
                            .build()
            ));
        }
        // Komunikasi Sistems
        aplikasi.getKomunikasiSistems().clear();
        if (request.getKomunikasiSistems() != null) {
            request.getKomunikasiSistems().forEach(komReq -> aplikasi.getKomunikasiSistems().add(
                    AplikasiKomunikasiSistem.builder()
                            .aplikasi(aplikasi)
                            .namaSistem(komReq.getNamaSistem())
                            .tipeSistem(komReq.getTipeSistem())
                            .deskripsiKomunikasi(komReq.getDeskripsiKomunikasi())
                            .keterangan(komReq.getKeterangan())
                            .isPlanned(komReq.getIsPlanned())
                            .build()
            ));
        }
        // Penghargaans
        aplikasi.getPenghargaans().clear();
        if (request.getPenghargaans() != null) {
            request.getPenghargaans().forEach(penghargaanReq -> {
                MstVariable kategori = variableRepository.findById(penghargaanReq.getKategoriId())
                        .orElseThrow(() -> new ResourceNotFoundException("Kategori penghargaan tidak ditemukan"));
                aplikasi.getPenghargaans().add(
                        AplikasiPenghargaan.builder()
                                .aplikasi(aplikasi)
                                .kategori(kategori)
                                .tanggal(penghargaanReq.getTanggal())
                                .deskripsi(penghargaanReq.getDeskripsi())
                                .build()
                );
            });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AplikasiResponse getById(UUID id) {
        MstAplikasi aplikasi = aplikasiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aplikasi tidak ditemukan"));
        return mapToResponse(aplikasi);
    }

    @Override
    @Transactional(readOnly = true)
    public AplikasiResponse getByKode(String kode) {
        MstAplikasi aplikasi = aplikasiRepository.findByKodeAplikasi(kode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Aplikasi dengan kode '" + kode + "' tidak ditemukan"));
        return mapToResponse(aplikasi);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AplikasiResponse> getAll() {
        return aplikasiRepository.findAllByOrderByKodeAplikasiAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AplikasiResponse> search(String search, UUID bidangId, UUID skpaId, String status, Pageable pageable) {
        return aplikasiRepository.searchAplikasi(search, bidangId, skpaId, status, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AplikasiResponse> searchList(String search, UUID bidangId, UUID skpaId, String status) {
        return aplikasiRepository.searchAplikasiList(search, bidangId, skpaId, status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public AplikasiResponse update(UUID id, AplikasiRequest request) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        MstAplikasi aplikasi = aplikasiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aplikasi tidak ditemukan"));

        // Capture old value untuk audit
        AplikasiResponse oldValue = mapToResponse(aplikasi);

        String newKode = request.getKodeAplikasi().toUpperCase().trim();
        if (!aplikasi.getKodeAplikasi().equals(newKode) && aplikasiRepository.existsByKodeAplikasi(newKode)) {
            throw new BadRequestException("Aplikasi dengan kode '" + newKode + "' sudah ada");
        }

        aplikasi.setKodeAplikasi(newKode);
        aplikasi.setNamaAplikasi(request.getNamaAplikasi().trim());
        aplikasi.setDeskripsi(request.getDeskripsi());
        aplikasi.setStatusAplikasi(request.getStatusAplikasi());
        aplikasi.setTanggalImplementasi(request.getTanggalImplementasi());
        // akses will be derived from URLs in setEntityDetails
        aplikasi.setProsesDataPribadi(request.getProsesDataPribadi());
        aplikasi.setDataPribadiDiproses(request.getDataPribadiDiproses());

        if (request.getIdleInfo() != null) {
            aplikasi.setKategoriIdle(request.getIdleInfo().getKategoriIdle());
            aplikasi.setAlasanIdle(request.getIdleInfo().getAlasanIdle());
            aplikasi.setRencanaPengakhiran(request.getIdleInfo().getRencanaPengakhiran());
            aplikasi.setAlasanBelumDiakhiri(request.getIdleInfo().getAlasanBelumDiakhiri());
        } else {
            aplikasi.setKategoriIdle(null);
            aplikasi.setAlasanIdle(null);
            aplikasi.setRencanaPengakhiran(null);
            aplikasi.setAlasanBelumDiakhiri(null);
        }

        setBidangAndSkpa(request, aplikasi);
        setEntityDetails(request, aplikasi);

        MstAplikasi updated = aplikasiRepository.save(aplikasi);
        log.info("Aplikasi updated: {} - {}", updated.getKodeAplikasi(), updated.getNamaAplikasi());
        
        // Audit log
        AplikasiResponse newValue = mapToResponse(updated);
        auditService.logUpdate(ENTITY_NAME, id, oldValue, newValue, userId, username);
        
        // Trigger snapshot update for historis
        aplikasiHistorisService.onAplikasiUpdated(updated, request.getKeteranganPerubahan());
        
        return newValue;
    }


    // All update* and add* methods are now replaced by setEntityDetails()

    @Override
    @Transactional
    public AplikasiResponse updateStatus(UUID id, String status) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        MstAplikasi aplikasi = aplikasiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aplikasi tidak ditemukan"));

        // Capture old value untuk audit
        AplikasiResponse oldValue = mapToResponse(aplikasi);

        // Validate status
        if (!status.equals("AKTIF") && !status.equals("IDLE") && !status.equals("DIAKHIRI")) {
            throw new BadRequestException("Status tidak valid. Gunakan: AKTIF, IDLE, atau DIAKHIRI");
        }

        aplikasi.setStatusAplikasi(status);
        aplikasi.setTanggalStatus(LocalDate.now()); // Set tanggal status saat update
        MstAplikasi updated = aplikasiRepository.save(aplikasi);
        log.info("Aplikasi status updated: {} - {} -> {}", updated.getKodeAplikasi(), updated.getNamaAplikasi(), status);

        // Audit log
        AplikasiResponse newValue = mapToResponse(updated);
        auditService.logUpdate(ENTITY_NAME, id, oldValue, newValue, userId, username);
        
        // Trigger snapshot update for historis
        aplikasiHistorisService.onAplikasiUpdated(updated, "Status diubah menjadi " + status);

        return newValue;
    }

    @Override
    @Transactional
    public AplikasiResponse updateStatusWithDetails(UUID id, AplikasiStatusRequest request) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        MstAplikasi aplikasi = aplikasiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aplikasi tidak ditemukan"));

        // Capture old value untuk audit
        AplikasiResponse oldValue = mapToResponse(aplikasi);

        String status = request.getStatus();
        // Validate status
        if (!status.equals("AKTIF") && !status.equals("IDLE") && !status.equals("DIAKHIRI")) {
            throw new BadRequestException("Status tidak valid. Gunakan: AKTIF, IDLE, atau DIAKHIRI");
        }

        aplikasi.setStatusAplikasi(status);

        // Set tanggal status from request or use current date
        if (request.getTanggalStatus() != null) {
            aplikasi.setTanggalStatus(request.getTanggalStatus());
        } else {
            aplikasi.setTanggalStatus(LocalDate.now());
        }

        // Update idle details if status is IDLE
        if (status.equals("IDLE")) {
            if (request.getIdleInfo() != null) {
                aplikasi.setKategoriIdle(request.getIdleInfo().getKategoriIdle());
                aplikasi.setAlasanIdle(request.getIdleInfo().getAlasanIdle());
                aplikasi.setRencanaPengakhiran(request.getIdleInfo().getRencanaPengakhiran());
                aplikasi.setAlasanBelumDiakhiri(request.getIdleInfo().getAlasanBelumDiakhiri());
            }
        } else {
            // Clear idle details if status is not IDLE
            aplikasi.setKategoriIdle(null);
            aplikasi.setAlasanIdle(null);
            aplikasi.setRencanaPengakhiran(null);
            aplikasi.setAlasanBelumDiakhiri(null);
        }

        MstAplikasi updated = aplikasiRepository.save(aplikasi);
        log.info("Aplikasi status updated with details: {} - {} -> {}", updated.getKodeAplikasi(), updated.getNamaAplikasi(), status);

        // Audit log
        AplikasiResponse newValue = mapToResponse(updated);
        auditService.logUpdate(ENTITY_NAME, id, oldValue, newValue, userId, username);
        
        // Trigger snapshot update for historis
        aplikasiHistorisService.onAplikasiUpdated(updated, "Status diubah menjadi " + status);

        return newValue;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        MstAplikasi aplikasi = aplikasiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aplikasi tidak ditemukan"));

        // Capture old value untuk audit sebelum delete
        AplikasiResponse oldValue = mapToResponse(aplikasi);

        aplikasiRepository.delete(aplikasi);
        log.info("Aplikasi deleted: {}", aplikasi.getKodeAplikasi());
        
        // Audit log
        auditService.logDelete(ENTITY_NAME, id, oldValue, userId, username);
    }

    private AplikasiResponse mapToResponse(MstAplikasi entity) {
        AplikasiResponse.AplikasiResponseBuilder builder = AplikasiResponse.builder()
                .id(entity.getId())
                .kodeAplikasi(entity.getKodeAplikasi())
                .namaAplikasi(entity.getNamaAplikasi())
                .deskripsi(entity.getDeskripsi())
                .statusAplikasi(entity.getStatusAplikasi())
                .tanggalStatus(entity.getTanggalStatus())
                .tanggalImplementasi(entity.getTanggalImplementasi())
                .akses(entity.getAkses())
                .prosesDataPribadi(entity.getProsesDataPribadi())
                .dataPribadiDiproses(entity.getDataPribadiDiproses())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // Map idle info
        if (entity.getKategoriIdle() != null || entity.getAlasanIdle() != null ||
            entity.getRencanaPengakhiran() != null || entity.getAlasanBelumDiakhiri() != null) {
            builder.idleInfo(IdleInfo.builder()
                    .kategoriIdle(entity.getKategoriIdle())
                    .alasanIdle(entity.getAlasanIdle())
                    .rencanaPengakhiran(entity.getRencanaPengakhiran())
                    .alasanBelumDiakhiri(entity.getAlasanBelumDiakhiri())
                    .build());
        }

        // Map bidang
        if (entity.getBidang() != null) {
            builder.bidang(BidangInfo.builder()
                    .id(entity.getBidang().getId())
                    .kodeBidang(entity.getBidang().getKodeBidang())
                    .namaBidang(entity.getBidang().getNamaBidang())
                    .build());
        }

        // Map skpa
        if (entity.getSkpa() != null) {
            builder.skpa(SkpaInfo.builder()
                    .id(entity.getSkpa().getId())
                    .kodeSkpa(entity.getSkpa().getKodeSkpa())
                    .namaSkpa(entity.getSkpa().getNamaSkpa())
                    .build());
        }

        // Map subKategori
        if (entity.getSubKategori() != null) {
            builder.subKategori(SubKategoriInfo.builder()
                    .id(entity.getSubKategori().getId())
                    .kode(entity.getSubKategori().getKode())
                    .nama(entity.getSubKategori().getNama())
                    .categoryCode(entity.getSubKategori().getCategoryCode())
                    .categoryName(entity.getSubKategori().getCategoryName())
                    .build());
        }

        // Map urls
        if (entity.getUrls() != null && !entity.getUrls().isEmpty()) {
            builder.urls(entity.getUrls().stream()
                    .map(url -> UrlInfo.builder()
                            .id(url.getId())
                            .url(url.getUrl())
                            .tipeAkses(url.getTipeAkses())
                            .keterangan(url.getKeterangan())
                            .build())
                    .collect(Collectors.toList()));
        }

        // Map satker internals
        if (entity.getSatkerInternals() != null && !entity.getSatkerInternals().isEmpty()) {
            builder.satkerInternals(entity.getSatkerInternals().stream()
                    .map(satker -> SatkerInternalInfo.builder()
                            .id(satker.getId())
                            .namaSatker(satker.getNamaSatker())
                            .keterangan(satker.getKeterangan())
                            .build())
                    .collect(Collectors.toList()));
        }

        // Map pengguna eksternals
        if (entity.getPenggunaEksternals() != null && !entity.getPenggunaEksternals().isEmpty()) {
            builder.penggunaEksternals(entity.getPenggunaEksternals().stream()
                    .map(pengguna -> PenggunaEksternalInfo.builder()
                            .id(pengguna.getId())
                            .namaPengguna(pengguna.getNamaPengguna())
                            .keterangan(pengguna.getKeterangan())
                            .build())
                    .collect(Collectors.toList()));
        }

        // Map komunikasi sistems
        if (entity.getKomunikasiSistems() != null && !entity.getKomunikasiSistems().isEmpty()) {
            builder.komunikasiSistems(entity.getKomunikasiSistems().stream()
                    .map(kom -> KomunikasiSistemInfo.builder()
                            .id(kom.getId())
                            .namaSistem(kom.getNamaSistem())
                            .tipeSistem(kom.getTipeSistem())
                            .deskripsiKomunikasi(kom.getDeskripsiKomunikasi())
                            .keterangan(kom.getKeterangan())
                            .isPlanned(kom.getIsPlanned())
                            .build())
                    .collect(Collectors.toList()));
        }

        // Map penghargaans
        if (entity.getPenghargaans() != null && !entity.getPenghargaans().isEmpty()) {
            builder.penghargaans(entity.getPenghargaans().stream()
                    .map(penghargaan -> PenghargaanInfo.builder()
                            .id(penghargaan.getId())
                            .kategori(VariableInfo.builder()
                                    .id(penghargaan.getKategori().getId())
                                    .kode(penghargaan.getKategori().getKode())
                                    .nama(penghargaan.getKategori().getNama())
                                    .build())
                            .tanggal(penghargaan.getTanggal())
                            .deskripsi(penghargaan.getDeskripsi())
                            .build())
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
