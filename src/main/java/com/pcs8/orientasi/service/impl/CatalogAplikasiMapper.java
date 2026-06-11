package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.client.catalog.dto.aplikasi.CatalogAplikasiRequest;
import com.pcs8.orientasi.client.catalog.dto.aplikasi.CatalogAplikasiResponse;
import com.pcs8.orientasi.client.catalog.dto.aplikasi.CatalogStatusRequest;
import com.pcs8.orientasi.client.catalog.dto.common.CatalogIdleInfo;
import com.pcs8.orientasi.client.catalog.dto.common.CatalogIdleInfoRequest;
import com.pcs8.orientasi.domain.dto.request.AplikasiRequest;
import com.pcs8.orientasi.domain.dto.request.AplikasiStatusRequest;
import com.pcs8.orientasi.domain.dto.response.*;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstBidangRepository;
import com.pcs8.orientasi.repository.MstSkpaRepository;
import com.pcs8.orientasi.repository.MstSubKategoriRepository;
import com.pcs8.orientasi.repository.MstVariableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CatalogAplikasiMapper {

    private final MstBidangRepository bidangRepository;
    private final MstSkpaRepository skpaRepository;
    private final MstSubKategoriRepository subKategoriRepository;
    private final MstVariableRepository variableRepository;

    // ── DOMAIN REQUEST → CATALOG REQUEST ─────────────────────────────────────

    public CatalogAplikasiRequest toCatalogRequest(AplikasiRequest request) {
        CatalogAplikasiRequest.CatalogAplikasiRequestBuilder builder = CatalogAplikasiRequest.builder()
                .namaAplikasi(request.getKodeAplikasi() != null ? request.getKodeAplikasi().toUpperCase().trim() : null)
                .kepanjangan(request.getNamaAplikasi() != null ? request.getNamaAplikasi().trim() : null)
                .deskripsi(request.getDeskripsi())
                .statusAplikasi(request.getStatusAplikasi())
                .tanggalImplementasi(request.getTanggalImplementasi())
                .prosesDataPribadi(request.getProsesDataPribadi())
                .dataPribadiDiproses(request.getDataPribadiDiproses());

        if (request.getBidangId() != null) {
            bidangRepository.findById(request.getBidangId())
                    .ifPresent(b -> builder.bidang(b.getKodeBidang()));
        }
        if (request.getSkpaId() != null) {
            skpaRepository.findById(request.getSkpaId())
                    .ifPresent(s -> builder.skpa(s.getKodeSkpa()));
        }
        if (request.getSubKategoriId() != null) {
            subKategoriRepository.findById(request.getSubKategoriId())
                    .ifPresent(sk -> builder.kategori(sk.getKode()));
        }

        if (request.getUrls() != null) {
            builder.urls(request.getUrls().stream()
                    .map(u -> CatalogAplikasiRequest.UrlRequest.builder()
                            .url(u.getUrl())
                            .tipeAkses(u.getTipeAkses())
                            .keterangan(u.getKeterangan())
                            .build())
                    .collect(Collectors.toList()));
        }
        if (request.getSatkerInternals() != null) {
            builder.penggunaInternal(request.getSatkerInternals().stream()
                    .map(s -> CatalogAplikasiRequest.PenggunaInternalRequest.builder()
                            .namaSatker(s.getNamaSatker())
                            .keterangan(s.getKeterangan())
                            .build())
                    .collect(Collectors.toList()));
        }
        if (request.getPenggunaEksternals() != null) {
            builder.penggunaEksternal(request.getPenggunaEksternals().stream()
                    .map(p -> CatalogAplikasiRequest.PenggunaEksternalRequest.builder()
                            .namaPengguna(p.getNamaPengguna())
                            .keterangan(p.getKeterangan())
                            .build())
                    .collect(Collectors.toList()));
        }
        if (request.getPenghargaans() != null) {
            builder.penghargaanAplikasi(request.getPenghargaans().stream()
                    .map(p -> {
                        String kategoriNama = variableRepository.findById(p.getKategoriId())
                                .orElseThrow(() -> new ResourceNotFoundException("Kategori penghargaan tidak ditemukan"))
                                .getNama();
                        return CatalogAplikasiRequest.PenghargaanRequest.builder()
                                .deskripsi(p.getDeskripsi())
                                .tanggal(p.getTanggal())
                                .kategori(kategoriNama)
                                .build();
                    })
                    .collect(Collectors.toList()));
        }
        // komunikasiSistems not supported by catalog — intentionally omitted

        return builder.build();
    }

    public CatalogStatusRequest toCatalogStatusRequest(AplikasiStatusRequest request) {
        CatalogStatusRequest.CatalogStatusRequestBuilder builder = CatalogStatusRequest.builder()
                .status(request.getStatus())
                .tanggalStatus(request.getTanggalStatus());

        if (request.getIdleInfo() != null) {
            builder.idleInfo(CatalogIdleInfoRequest.builder()
                    .kategoriIdle(request.getIdleInfo().getKategoriIdle())
                    .alasanIdle(request.getIdleInfo().getAlasanIdle())
                    .rencanaPengakhiran(request.getIdleInfo().getRencanaPengakhiran())
                    .alasanBelumDiakhiri(request.getIdleInfo().getAlasanBelumDiakhiri())
                    .build());
        }
        return builder.build();
    }

    public CatalogStatusRequest toCatalogStatusRequest(String status) {
        return CatalogStatusRequest.builder().status(status).build();
    }

    // ── CATALOG RESPONSE → DOMAIN RESPONSE ───────────────────────────────────

    public AplikasiResponse toAplikasiResponse(CatalogAplikasiResponse catalog) {
        AplikasiResponse.AplikasiResponseBuilder builder = AplikasiResponse.builder()
                .id(catalog.getId())
                .kodeAplikasi(catalog.getNamaAplikasi())
                .namaAplikasi(catalog.getKepanjangan())
                .deskripsi(catalog.getDeskripsi())
                .statusAplikasi(catalog.getStatusAplikasi())
                .tanggalStatus(catalog.getTanggalStatus())
                .tanggalImplementasi(catalog.getTanggalImplementasi())
                .prosesDataPribadi(catalog.getProsesDataPribadi())
                .dataPribadiDiproses(catalog.getDataPribadiDiproses())
                .createdAt(catalog.getCreatedAt())
                .updatedAt(catalog.getUpdatedAt());

        enrichBidang(catalog.getBidang(), builder);
        enrichSkpa(catalog.getSkpa(), builder);
        enrichSubKategori(catalog.getKategori(), builder);
        mapIdleInfo(catalog.getIdleInfo(), builder);
        mapUrls(catalog.getUrls(), builder);
        mapSatkerInternals(catalog.getPenggunaInternal(), builder);
        mapPenggunaEksternals(catalog.getPenggunaEksternal(), builder);
        mapPenghargaans(catalog.getPenghargaanAplikasi(), builder);
        deriveAkses(catalog.getUrls(), builder);

        return builder.build();
    }

    public AplikasiListResponse toAplikasiListResponse(CatalogAplikasiResponse catalog) {
        AplikasiListResponse.AplikasiListResponseBuilder builder = AplikasiListResponse.builder()
                .id(catalog.getId())
                .kodeAplikasi(catalog.getNamaAplikasi())
                .namaAplikasi(catalog.getKepanjangan())
                .statusAplikasi(catalog.getStatusAplikasi());

        enrichBidangForList(catalog.getBidang(), builder);
        enrichSkpaForList(catalog.getSkpa(), builder);
        enrichSubKategoriForList(catalog.getKategori(), builder);

        return builder.build();
    }

    // ── ENRICH FROM LOCAL MASTER DATA ────────────────────────────────────────

    private void enrichBidang(String kodeBidang, AplikasiResponse.AplikasiResponseBuilder builder) {
        if (kodeBidang == null) return;
        bidangRepository.findByKodeBidang(kodeBidang).ifPresent(b ->
                builder.bidang(BidangInfo.builder()
                        .id(b.getId())
                        .kodeBidang(b.getKodeBidang())
                        .namaBidang(b.getNamaBidang())
                        .build()));
    }

    private void enrichSkpa(String kodeSkpa, AplikasiResponse.AplikasiResponseBuilder builder) {
        if (kodeSkpa == null) return;
        skpaRepository.findByKodeSkpa(kodeSkpa).ifPresent(s ->
                builder.skpa(SkpaInfo.builder()
                        .id(s.getId())
                        .kodeSkpa(s.getKodeSkpa())
                        .namaSkpa(s.getNamaSkpa())
                        .build()));
    }

    private void enrichSubKategori(String kode, AplikasiResponse.AplikasiResponseBuilder builder) {
        if (kode == null) return;
        subKategoriRepository.findByKode(kode).ifPresent(sk ->
                builder.subKategori(SubKategoriInfo.builder()
                        .id(sk.getId())
                        .kode(sk.getKode())
                        .nama(sk.getNama())
                        .categoryCode(sk.getCategoryCode())
                        .categoryName(sk.getCategoryName())
                        .build()));
    }

    private void enrichBidangForList(String kodeBidang, AplikasiListResponse.AplikasiListResponseBuilder builder) {
        if (kodeBidang == null) return;
        bidangRepository.findByKodeBidang(kodeBidang).ifPresent(b ->
                builder.bidang(BidangInfo.builder()
                        .id(b.getId())
                        .kodeBidang(b.getKodeBidang())
                        .namaBidang(b.getNamaBidang())
                        .build()));
    }

    private void enrichSkpaForList(String kodeSkpa, AplikasiListResponse.AplikasiListResponseBuilder builder) {
        if (kodeSkpa == null) return;
        skpaRepository.findByKodeSkpa(kodeSkpa).ifPresent(s ->
                builder.skpa(SkpaInfo.builder()
                        .id(s.getId())
                        .kodeSkpa(s.getKodeSkpa())
                        .namaSkpa(s.getNamaSkpa())
                        .build()));
    }

    private void enrichSubKategoriForList(String kode, AplikasiListResponse.AplikasiListResponseBuilder builder) {
        if (kode == null) return;
        subKategoriRepository.findByKode(kode).ifPresent(sk ->
                builder.subKategori(SubKategoriInfo.builder()
                        .id(sk.getId())
                        .kode(sk.getKode())
                        .nama(sk.getNama())
                        .categoryCode(sk.getCategoryCode())
                        .categoryName(sk.getCategoryName())
                        .build()));
    }

    // ── NESTED FIELD MAPPINGS ─────────────────────────────────────────────────

    private void mapIdleInfo(CatalogIdleInfo idle, AplikasiResponse.AplikasiResponseBuilder builder) {
        if (idle == null) return;
        builder.idleInfo(IdleInfo.builder()
                .kategoriIdle(idle.getKategoriIdle())
                .alasanIdle(idle.getAlasanIdle())
                .rencanaPengakhiran(idle.getRencanaPengakhiran())
                .alasanBelumDiakhiri(idle.getAlasanBelumDiakhiri())
                .build());
    }

    private void mapUrls(List<CatalogAplikasiResponse.UrlInfo> urls, AplikasiResponse.AplikasiResponseBuilder builder) {
        if (urls == null || urls.isEmpty()) return;
        builder.urls(urls.stream()
                .map(u -> UrlInfo.builder()
                        .id(u.getId())
                        .url(u.getUrl())
                        .tipeAkses(u.getTipeAkses())
                        .keterangan(u.getKeterangan())
                        .build())
                .collect(Collectors.toList()));
    }

    private void mapSatkerInternals(List<CatalogAplikasiResponse.PenggunaInternalInfo> list, AplikasiResponse.AplikasiResponseBuilder builder) {
        if (list == null || list.isEmpty()) return;
        builder.satkerInternals(list.stream()
                .map(s -> SatkerInternalInfo.builder()
                        .id(s.getId())
                        .namaSatker(s.getNamaSatker())
                        .keterangan(s.getKeterangan())
                        .build())
                .collect(Collectors.toList()));
    }

    private void mapPenggunaEksternals(List<CatalogAplikasiResponse.PenggunaEksternalInfo> list, AplikasiResponse.AplikasiResponseBuilder builder) {
        if (list == null || list.isEmpty()) return;
        builder.penggunaEksternals(list.stream()
                .map(p -> PenggunaEksternalInfo.builder()
                        .id(p.getId())
                        .namaPengguna(p.getNamaPengguna())
                        .keterangan(p.getKeterangan())
                        .build())
                .collect(Collectors.toList()));
    }

    private void mapPenghargaans(List<CatalogAplikasiResponse.PenghargaanInfo> list, AplikasiResponse.AplikasiResponseBuilder builder) {
        if (list == null || list.isEmpty()) return;
        builder.penghargaans(list.stream()
                .map(p -> PenghargaanInfo.builder()
                        .id(p.getId())
                        .deskripsi(p.getDeskripsi())
                        .tanggal(p.getTanggal())
                        .kategori(p.getKategori() != null
                                ? VariableInfo.builder().nama(p.getKategori()).build()
                                : null)
                        .build())
                .collect(Collectors.toList()));
    }

    private void deriveAkses(List<CatalogAplikasiResponse.UrlInfo> urls, AplikasiResponse.AplikasiResponseBuilder builder) {
        if (urls == null || urls.isEmpty()) return;
        String akses = urls.stream()
                .map(CatalogAplikasiResponse.UrlInfo::getTipeAkses)
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .collect(Collectors.joining(","));
        if (!akses.isEmpty()) builder.akses(akses);
    }
}
