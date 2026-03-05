package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.AplikasiRequest;
import com.pcs8.orientasi.domain.dto.request.AplikasiStatusRequest;
import com.pcs8.orientasi.domain.dto.response.AplikasiResponse;
import com.pcs8.orientasi.domain.entity.*;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstAplikasiRepository;
import com.pcs8.orientasi.repository.MstBidangRepository;
import com.pcs8.orientasi.repository.MstSkpaRepository;
import com.pcs8.orientasi.service.AplikasiService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AplikasiServiceImpl implements AplikasiService {

    private static final Logger log = LoggerFactory.getLogger(AplikasiServiceImpl.class);

    private final MstAplikasiRepository aplikasiRepository;
    private final MstBidangRepository bidangRepository;
    private final MstSkpaRepository skpaRepository;

    @Override
    @Transactional
    public AplikasiResponse create(AplikasiRequest request) {
        String kode = request.getKodeAplikasi().toUpperCase().trim();

        if (aplikasiRepository.existsByKodeAplikasi(kode)) {
            throw new BadRequestException("Aplikasi dengan kode '" + kode + "' sudah ada");
        }

        MstAplikasi aplikasi = MstAplikasi.builder()
                .kodeAplikasi(kode)
                .namaAplikasi(request.getNamaAplikasi().trim())
                .deskripsi(request.getDeskripsi())
                .statusAplikasi(request.getStatusAplikasi())
                .akses(request.getAkses())
                .prosesDataPribadi(request.getProsesDataPribadi() != null ? request.getProsesDataPribadi() : false)
                .dataPribadiDiproses(request.getDataPribadiDiproses())
                .kategoriIdle(request.getKategoriIdle())
                .alasanIdle(request.getAlasanIdle())
                .rencanaPengakhiran(request.getRencanaPengakhiran())
                .alasanBelumDiakhiri(request.getAlasanBelumDiakhiri())
                .urls(new ArrayList<>())
                .satkerInternals(new ArrayList<>())
                .penggunaEksternals(new ArrayList<>())
                .komunikasiSistems(new ArrayList<>())
                .build();

        // Set bidang if provided
        if (request.getBidangId() != null) {
            MstBidang bidang = bidangRepository.findById(request.getBidangId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bidang tidak ditemukan"));
            aplikasi.setBidang(bidang);
        }

        // Set skpa if provided
        if (request.getSkpaId() != null) {
            MstSkpa skpa = skpaRepository.findById(request.getSkpaId())
                    .orElseThrow(() -> new ResourceNotFoundException("SKPA tidak ditemukan"));
            aplikasi.setSkpa(skpa);
        }

        // Add URLs
        if (request.getUrls() != null) {
            for (AplikasiRequest.UrlRequest urlReq : request.getUrls()) {
                AplikasiUrl url = AplikasiUrl.builder()
                        .aplikasi(aplikasi)
                        .url(urlReq.getUrl())
                        .tipeAkses(urlReq.getTipeAkses())
                        .keterangan(urlReq.getKeterangan())
                        .build();
                aplikasi.getUrls().add(url);
            }
        }

        // Add Satker Internals
        if (request.getSatkerInternals() != null) {
            for (AplikasiRequest.SatkerInternalRequest satkerReq : request.getSatkerInternals()) {
                AplikasiSatkerInternal satker = AplikasiSatkerInternal.builder()
                        .aplikasi(aplikasi)
                        .namaSatker(satkerReq.getNamaSatker())
                        .keterangan(satkerReq.getKeterangan())
                        .build();
                aplikasi.getSatkerInternals().add(satker);
            }
        }

        // Add Pengguna Eksternals
        if (request.getPenggunaEksternals() != null) {
            for (AplikasiRequest.PenggunaEksternalRequest penggunaReq : request.getPenggunaEksternals()) {
                AplikasiPenggunaEksternal pengguna = AplikasiPenggunaEksternal.builder()
                        .aplikasi(aplikasi)
                        .namaPengguna(penggunaReq.getNamaPengguna())
                        .keterangan(penggunaReq.getKeterangan())
                        .build();
                aplikasi.getPenggunaEksternals().add(pengguna);
            }
        }

        // Add Komunikasi Sistems
        if (request.getKomunikasiSistems() != null) {
            for (AplikasiRequest.KomunikasiSistemRequest komReq : request.getKomunikasiSistems()) {
                AplikasiKomunikasiSistem kom = AplikasiKomunikasiSistem.builder()
                        .aplikasi(aplikasi)
                        .namaSistem(komReq.getNamaSistem())
                        .tipeSistem(komReq.getTipeSistem())
                        .deskripsiKomunikasi(komReq.getDeskripsiKomunikasi())
                        .keterangan(komReq.getKeterangan())
                        .isPlanned(komReq.getIsPlanned() != null ? komReq.getIsPlanned() : false)
                        .build();
                aplikasi.getKomunikasiSistems().add(kom);
            }
        }

        MstAplikasi saved = aplikasiRepository.save(aplikasi);
        log.info("Aplikasi created: {} - {}", saved.getKodeAplikasi(), saved.getNamaAplikasi());

        return mapToResponse(saved);
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
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AplikasiResponse update(UUID id, AplikasiRequest request) {
        MstAplikasi aplikasi = aplikasiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aplikasi tidak ditemukan"));

        String newKode = request.getKodeAplikasi().toUpperCase().trim();

        if (!aplikasi.getKodeAplikasi().equals(newKode) && aplikasiRepository.existsByKodeAplikasi(newKode)) {
            throw new BadRequestException("Aplikasi dengan kode '" + newKode + "' sudah ada");
        }

        aplikasi.setKodeAplikasi(newKode);
        aplikasi.setNamaAplikasi(request.getNamaAplikasi().trim());
        aplikasi.setDeskripsi(request.getDeskripsi());
        aplikasi.setStatusAplikasi(request.getStatusAplikasi());
        aplikasi.setAkses(request.getAkses());
        aplikasi.setProsesDataPribadi(request.getProsesDataPribadi() != null ? request.getProsesDataPribadi() : false);
        aplikasi.setDataPribadiDiproses(request.getDataPribadiDiproses());
        aplikasi.setKategoriIdle(request.getKategoriIdle());
        aplikasi.setAlasanIdle(request.getAlasanIdle());
        aplikasi.setRencanaPengakhiran(request.getRencanaPengakhiran());
        aplikasi.setAlasanBelumDiakhiri(request.getAlasanBelumDiakhiri());

        // Update bidang
        if (request.getBidangId() != null) {
            MstBidang bidang = bidangRepository.findById(request.getBidangId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bidang tidak ditemukan"));
            aplikasi.setBidang(bidang);
        } else {
            aplikasi.setBidang(null);
        }

        // Update skpa
        if (request.getSkpaId() != null) {
            MstSkpa skpa = skpaRepository.findById(request.getSkpaId())
                    .orElseThrow(() -> new ResourceNotFoundException("SKPA tidak ditemukan"));
            aplikasi.setSkpa(skpa);
        } else {
            aplikasi.setSkpa(null);
        }

        // Clear and re-add URLs
        aplikasi.getUrls().clear();
        if (request.getUrls() != null) {
            for (AplikasiRequest.UrlRequest urlReq : request.getUrls()) {
                AplikasiUrl url = AplikasiUrl.builder()
                        .aplikasi(aplikasi)
                        .url(urlReq.getUrl())
                        .tipeAkses(urlReq.getTipeAkses())
                        .keterangan(urlReq.getKeterangan())
                        .build();
                aplikasi.getUrls().add(url);
            }
        }

        // Clear and re-add Satker Internals
        aplikasi.getSatkerInternals().clear();
        if (request.getSatkerInternals() != null) {
            for (AplikasiRequest.SatkerInternalRequest satkerReq : request.getSatkerInternals()) {
                AplikasiSatkerInternal satker = AplikasiSatkerInternal.builder()
                        .aplikasi(aplikasi)
                        .namaSatker(satkerReq.getNamaSatker())
                        .keterangan(satkerReq.getKeterangan())
                        .build();
                aplikasi.getSatkerInternals().add(satker);
            }
        }

        // Clear and re-add Pengguna Eksternals
        aplikasi.getPenggunaEksternals().clear();
        if (request.getPenggunaEksternals() != null) {
            for (AplikasiRequest.PenggunaEksternalRequest penggunaReq : request.getPenggunaEksternals()) {
                AplikasiPenggunaEksternal pengguna = AplikasiPenggunaEksternal.builder()
                        .aplikasi(aplikasi)
                        .namaPengguna(penggunaReq.getNamaPengguna())
                        .keterangan(penggunaReq.getKeterangan())
                        .build();
                aplikasi.getPenggunaEksternals().add(pengguna);
            }
        }

        // Clear and re-add Komunikasi Sistems
        aplikasi.getKomunikasiSistems().clear();
        if (request.getKomunikasiSistems() != null) {
            for (AplikasiRequest.KomunikasiSistemRequest komReq : request.getKomunikasiSistems()) {
                AplikasiKomunikasiSistem kom = AplikasiKomunikasiSistem.builder()
                        .aplikasi(aplikasi)
                        .namaSistem(komReq.getNamaSistem())
                        .tipeSistem(komReq.getTipeSistem())
                        .deskripsiKomunikasi(komReq.getDeskripsiKomunikasi())
                        .keterangan(komReq.getKeterangan())
                        .isPlanned(komReq.getIsPlanned() != null ? komReq.getIsPlanned() : false)
                        .build();
                aplikasi.getKomunikasiSistems().add(kom);
            }
        }

        MstAplikasi updated = aplikasiRepository.save(aplikasi);
        log.info("Aplikasi updated: {} - {}", updated.getKodeAplikasi(), updated.getNamaAplikasi());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public AplikasiResponse updateStatus(UUID id, String status) {
        MstAplikasi aplikasi = aplikasiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aplikasi tidak ditemukan"));

        // Validate status
        if (!status.equals("AKTIF") && !status.equals("IDLE") && !status.equals("DIAKHIRI")) {
            throw new BadRequestException("Status tidak valid. Gunakan: AKTIF, IDLE, atau DIAKHIRI");
        }

        aplikasi.setStatusAplikasi(status);
        MstAplikasi updated = aplikasiRepository.save(aplikasi);
        log.info("Aplikasi status updated: {} - {} -> {}", updated.getKodeAplikasi(), updated.getNamaAplikasi(), status);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public AplikasiResponse updateStatusWithDetails(UUID id, AplikasiStatusRequest request) {
        MstAplikasi aplikasi = aplikasiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aplikasi tidak ditemukan"));

        String status = request.getStatus();
        // Validate status
        if (!status.equals("AKTIF") && !status.equals("IDLE") && !status.equals("DIAKHIRI")) {
            throw new BadRequestException("Status tidak valid. Gunakan: AKTIF, IDLE, atau DIAKHIRI");
        }

        aplikasi.setStatusAplikasi(status);

        // Update idle details if status is IDLE
        if (status.equals("IDLE")) {
            aplikasi.setKategoriIdle(request.getKategoriIdle());
            aplikasi.setAlasanIdle(request.getAlasanIdle());
            aplikasi.setRencanaPengakhiran(request.getRencanaPengakhiran());
            aplikasi.setAlasanBelumDiakhiri(request.getAlasanBelumDiakhiri());
        } else {
            // Clear idle details if status is not IDLE
            aplikasi.setKategoriIdle(null);
            aplikasi.setAlasanIdle(null);
            aplikasi.setRencanaPengakhiran(null);
            aplikasi.setAlasanBelumDiakhiri(null);
        }

        MstAplikasi updated = aplikasiRepository.save(aplikasi);
        log.info("Aplikasi status updated with details: {} - {} -> {}", updated.getKodeAplikasi(), updated.getNamaAplikasi(), status);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        MstAplikasi aplikasi = aplikasiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aplikasi tidak ditemukan"));

        aplikasiRepository.delete(aplikasi);
        log.info("Aplikasi deleted: {}", aplikasi.getKodeAplikasi());
    }

    private AplikasiResponse mapToResponse(MstAplikasi entity) {
        AplikasiResponse.AplikasiResponseBuilder builder = AplikasiResponse.builder()
                .id(entity.getId())
                .kodeAplikasi(entity.getKodeAplikasi())
                .namaAplikasi(entity.getNamaAplikasi())
                .deskripsi(entity.getDeskripsi())
                .statusAplikasi(entity.getStatusAplikasi())
                .akses(entity.getAkses())
                .prosesDataPribadi(entity.getProsesDataPribadi())
                .dataPribadiDiproses(entity.getDataPribadiDiproses())
                .kategoriIdle(entity.getKategoriIdle())
                .alasanIdle(entity.getAlasanIdle())
                .rencanaPengakhiran(entity.getRencanaPengakhiran())
                .alasanBelumDiakhiri(entity.getAlasanBelumDiakhiri())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // Map bidang
        if (entity.getBidang() != null) {
            builder.bidang(AplikasiResponse.BidangInfo.builder()
                    .id(entity.getBidang().getId())
                    .kodeBidang(entity.getBidang().getKodeBidang())
                    .namaBidang(entity.getBidang().getNamaBidang())
                    .build());
        }

        // Map skpa
        if (entity.getSkpa() != null) {
            builder.skpa(AplikasiResponse.SkpaInfo.builder()
                    .id(entity.getSkpa().getId())
                    .kodeSkpa(entity.getSkpa().getKodeSkpa())
                    .namaSkpa(entity.getSkpa().getNamaSkpa())
                    .build());
        }

        // Map urls
        if (entity.getUrls() != null && !entity.getUrls().isEmpty()) {
            builder.urls(entity.getUrls().stream()
                    .map(url -> AplikasiResponse.UrlInfo.builder()
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
                    .map(satker -> AplikasiResponse.SatkerInternalInfo.builder()
                            .id(satker.getId())
                            .namaSatker(satker.getNamaSatker())
                            .keterangan(satker.getKeterangan())
                            .build())
                    .collect(Collectors.toList()));
        }

        // Map pengguna eksternals
        if (entity.getPenggunaEksternals() != null && !entity.getPenggunaEksternals().isEmpty()) {
            builder.penggunaEksternals(entity.getPenggunaEksternals().stream()
                    .map(pengguna -> AplikasiResponse.PenggunaEksternalInfo.builder()
                            .id(pengguna.getId())
                            .namaPengguna(pengguna.getNamaPengguna())
                            .keterangan(pengguna.getKeterangan())
                            .build())
                    .collect(Collectors.toList()));
        }

        // Map komunikasi sistems
        if (entity.getKomunikasiSistems() != null && !entity.getKomunikasiSistems().isEmpty()) {
            builder.komunikasiSistems(entity.getKomunikasiSistems().stream()
                    .map(kom -> AplikasiResponse.KomunikasiSistemInfo.builder()
                            .id(kom.getId())
                            .namaSistem(kom.getNamaSistem())
                            .tipeSistem(kom.getTipeSistem())
                            .deskripsiKomunikasi(kom.getDeskripsiKomunikasi())
                            .keterangan(kom.getKeterangan())
                            .isPlanned(kom.getIsPlanned())
                            .build())
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
