package com.pcs8.orientasi.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcs8.orientasi.client.catalog.CatalogSnapshotClient;
import com.pcs8.orientasi.client.catalog.dto.aplikasi.CatalogAplikasiResponse;
import com.pcs8.orientasi.client.catalog.dto.changelog.CatalogChangelogRequest;
import com.pcs8.orientasi.client.catalog.dto.changelog.CatalogChangelogResponse;
import com.pcs8.orientasi.client.catalog.dto.snapshot.CatalogBatchGenerateRequest;
import com.pcs8.orientasi.client.catalog.dto.snapshot.CatalogBatchGenerateResponse;
import com.pcs8.orientasi.client.catalog.dto.snapshot.CatalogSnapshotStatistikResponse;
import com.pcs8.orientasi.client.catalog.dto.snapshot.CatalogSnapshotUpdateRequest;
import com.pcs8.orientasi.client.catalog.dto.snapshot.CatalogYearSnapshotListItem;
import com.pcs8.orientasi.client.catalog.dto.snapshot.CatalogYearSnapshotRequest;
import com.pcs8.orientasi.client.catalog.dto.snapshot.CatalogYearSnapshotResponse;
import com.pcs8.orientasi.domain.dto.request.ChangelogRequest;
import com.pcs8.orientasi.domain.dto.request.UpdateSnapshotRequest;
import com.pcs8.orientasi.domain.dto.response.*;
import com.pcs8.orientasi.repository.MstBidangRepository;
import com.pcs8.orientasi.repository.MstSkpaRepository;
import com.pcs8.orientasi.service.AplikasiHistorisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AplikasiHistorisServiceImpl implements AplikasiHistorisService {

    private final CatalogSnapshotClient snapshotClient;
    private final MstBidangRepository bidangRepository;
    private final MstSkpaRepository skpaRepository;
    private final ObjectMapper objectMapper;

    public AplikasiHistorisServiceImpl(
            CatalogSnapshotClient snapshotClient,
            MstBidangRepository bidangRepository,
            MstSkpaRepository skpaRepository,
            ObjectMapper objectMapper
    ) {
        this.snapshotClient = snapshotClient;
        this.bidangRepository = bidangRepository;
        this.skpaRepository = skpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public AplikasiSnapshotResponse createOrUpdateSnapshot(UUID aplikasiId, Integer tahun, String snapshotType) {
        CatalogYearSnapshotRequest req = CatalogYearSnapshotRequest.builder()
                .tahun(tahun).snapshotType(snapshotType).build();
        return mapToSnapshotResponse(snapshotClient.createOrUpdateYearSnapshot(aplikasiId, req));
    }

    @Override
    public AplikasiSnapshotResponse updateSnapshot(UUID snapshotId, UpdateSnapshotRequest request) {
        CatalogSnapshotUpdateRequest.CatalogSnapshotUpdateRequestBuilder builder =
                CatalogSnapshotUpdateRequest.builder()
                        .namaAplikasi(request.getKodeAplikasi())
                        .kepanjangan(request.getNamaAplikasi())
                        .deskripsi(request.getDeskripsi())
                        .statusAplikasi(request.getStatusAplikasi())
                        .tanggalImplementasi(request.getTanggalImplementasi())
                        .prosesDataPribadi(request.getProsesDataPribadi())
                        .keteranganHistoris(request.getKeteranganHistoris());

        if (request.getBidangId() != null) {
            bidangRepository.findById(request.getBidangId())
                    .ifPresent(b -> builder.bidang(b.getKodeBidang()));
        }
        if (request.getSkpaId() != null) {
            skpaRepository.findById(request.getSkpaId())
                    .ifPresent(s -> builder.skpa(s.getKodeSkpa()));
        }

        CatalogYearSnapshotResponse updated = snapshotClient.update(snapshotId, builder.build());

        if (request.getChangelogKeterangan() != null && !request.getChangelogKeterangan().isBlank()) {
            snapshotClient.addChangelog(snapshotId, CatalogChangelogRequest.builder()
                    .tanggalPerubahan(request.getChangelogTanggal() != null
                            ? request.getChangelogTanggal() : LocalDate.now())
                    .keterangan(request.getChangelogKeterangan())
                    .build());
        }

        return mapToSnapshotResponse(updated);
    }

    @Override
    public List<AplikasiSnapshotResponse> generateSnapshotsForYear(Integer tahun) {
        CatalogBatchGenerateResponse result = snapshotClient.batchGenerate(
                CatalogBatchGenerateRequest.builder().tahun(tahun).snapshotType("GENERATED").build());

        log.info("Batch generate tahun {}: generated={}, updated={}",
                tahun, result.getTotalGenerated(), result.getTotalUpdated());

        return snapshotClient.listByTahun(tahun).stream()
                .map(item -> snapshotClient.getByAplikasiAndTahun(item.getAplikasiId(), tahun))
                .map(this::mapToSnapshotResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AplikasiSnapshotResponse getSnapshotById(UUID id) {
        return mapToSnapshotResponse(snapshotClient.getById(id));
    }

    @Override
    public AplikasiSnapshotResponse getSnapshotByAplikasiAndTahun(UUID aplikasiId, Integer tahun) {
        return mapToSnapshotResponse(snapshotClient.getByAplikasiAndTahun(aplikasiId, tahun));
    }

    @Override
    public List<AplikasiHistorisListResponse> getHistorisByPeriode(Integer startYear, Integer endYear) {
        List<AplikasiHistorisListResponse> result = new ArrayList<>();
        for (int year = startYear; year <= endYear; year++) {
            result.addAll(snapshotClient.listByTahun(year).stream()
                    .map(this::mapToListResponse)
                    .toList());
        }
        return result;
    }

    @Override
    public List<AplikasiHistorisListResponse> getHistorisByTahun(Integer tahun) {
        return snapshotClient.listByTahun(tahun).stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AplikasiSnapshotResponse> getFullSnapshotsByTahun(Integer tahun) {
        return snapshotClient.listByTahun(tahun).stream()
                .map(item -> snapshotClient.getByAplikasiAndTahun(item.getAplikasiId(), tahun))
                .map(this::mapToSnapshotResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<Integer> getAvailableYears() {
        return snapshotClient.getAvailableYears();
    }

    @Override
    public AplikasiStatistikResponse getStatistikByTahun(Integer tahun) {
        CatalogSnapshotStatistikResponse s = snapshotClient.getStatistik(tahun);
        return AplikasiStatistikResponse.builder()
                .tahun(s.getTahun())
                .totalAplikasi(s.getTotalAplikasi())
                .byStatus(s.getByStatus())
                .build();
    }

    @Override
    public List<AplikasiStatistikResponse> getStatistikByPeriode(Integer startYear, Integer endYear) {
        List<AplikasiStatistikResponse> result = new ArrayList<>();
        for (int year = startYear; year <= endYear; year++) {
            result.add(getStatistikByTahun(year));
        }
        return result;
    }

    @Override
    public ChangelogInfo addChangelog(UUID snapshotId, ChangelogRequest request) {
        CatalogChangelogResponse r = snapshotClient.addChangelog(snapshotId,
                CatalogChangelogRequest.builder()
                        .tanggalPerubahan(request.getTanggalPerubahan())
                        .keterangan(request.getKeterangan())
                        .build());
        return mapToChangelogInfo(r);
    }

    @Override
    public List<ChangelogInfo> getChangelogsBySnapshotId(UUID snapshotId) {
        return snapshotClient.getChangelogs(snapshotId).stream()
                .map(this::mapToChangelogInfo)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteChangelog(UUID changelogId) {
        snapshotClient.deleteChangelog(changelogId);
    }

    @Override
    public void deleteSnapshot(UUID id) {
        snapshotClient.delete(id);
    }

    @Override
    public List<AplikasiSnapshotResponse> getSnapshotsByAplikasiId(UUID aplikasiId) {
        return snapshotClient.listByAplikasi(aplikasiId).stream()
                .map(item -> snapshotClient.getByAplikasiAndTahun(item.getAplikasiId(), item.getTahun()))
                .map(this::mapToSnapshotResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void onAplikasiUpdated(UUID aplikasiId, String keterangan) {
        int currentYear = LocalDate.now().getYear();
        try {
            snapshotClient.createOrUpdateYearSnapshot(aplikasiId,
                    CatalogYearSnapshotRequest.builder().tahun(currentYear).snapshotType("AUTO").build());

            if (keterangan != null && !keterangan.isBlank()) {
                CatalogYearSnapshotResponse snapshot = snapshotClient.getByAplikasiAndTahun(aplikasiId, currentYear);
                snapshotClient.addChangelog(snapshot.getId(),
                        CatalogChangelogRequest.builder()
                                .tanggalPerubahan(LocalDate.now())
                                .keterangan(keterangan)
                                .build());
            }
            log.info("Snapshot auto-updated for aplikasi {} tahun {}", aplikasiId, currentYear);
        } catch (Exception e) {
            log.warn("Failed to auto-update snapshot for aplikasi {}: {}", aplikasiId, e.getMessage());
        }
    }

    // ── MAPPERS ───────────────────────────────────────────────────────────────

    private AplikasiSnapshotResponse mapToSnapshotResponse(CatalogYearSnapshotResponse s) {
        AplikasiSnapshotResponse.AplikasiSnapshotResponseBuilder b = AplikasiSnapshotResponse.builder()
                .id(s.getId())
                .aplikasiId(s.getAplikasiId())
                .tahun(s.getTahun())
                .kodeAplikasi(s.getNamaAplikasi())
                .namaAplikasi(s.getKepanjangan())
                .deskripsi(s.getDeskripsi())
                .statusAplikasi(s.getStatusAplikasi())
                .tanggalStatus(s.getTanggalStatus())
                .tanggalImplementasi(s.getTanggalImplementasi())
                .prosesDataPribadi(s.getProsesDataPribadi())
                .dataPribadiDiproses(s.getDataPribadiDiproses())
                .snapshotDate(s.getSnapshotDate())
                .snapshotType(s.getSnapshotType())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt());

        if (s.getIdleInfo() != null) {
            b.idleInfo(IdleInfo.builder()
                    .kategoriIdle(s.getIdleInfo().getKategoriIdle())
                    .alasanIdle(s.getIdleInfo().getAlasanIdle())
                    .rencanaPengakhiran(s.getIdleInfo().getRencanaPengakhiran())
                    .alasanBelumDiakhiri(s.getIdleInfo().getAlasanBelumDiakhiri())
                    .build());
        }

        enrichBidang(b, s.getBidang());
        enrichSkpa(b, s.getSkpa());

        b.urls(parseJsonList(s.getUrls(), new TypeReference<List<CatalogAplikasiResponse.UrlInfo>>() {})
                .stream().map(u -> UrlInfo.builder().url(u.getUrl())
                        .tipeAkses(u.getTipeAkses()).keterangan(u.getKeterangan()).build()).toList());

        b.satkerInternals(parseJsonList(s.getPenggunaInternal(),
                new TypeReference<List<CatalogAplikasiResponse.PenggunaInternalInfo>>() {})
                .stream().map(p -> SatkerInternalInfo.builder()
                        .namaSatker(p.getNamaSatker()).keterangan(p.getKeterangan()).build()).toList());

        b.penggunaEksternals(parseJsonList(s.getPenggunaEksternal(),
                new TypeReference<List<CatalogAplikasiResponse.PenggunaEksternalInfo>>() {})
                .stream().map(p -> PenggunaEksternalInfo.builder()
                        .namaPengguna(p.getNamaPengguna()).keterangan(p.getKeterangan()).build()).toList());

        b.penghargaans(parseJsonList(s.getPenghargaanAplikasi(),
                new TypeReference<List<CatalogAplikasiResponse.PenghargaanInfo>>() {})
                .stream().map(p -> PenghargaanInfo.builder()
                        .deskripsi(p.getDeskripsi()).tanggal(p.getTanggal())
                        .kategori(p.getKategori() != null
                                ? VariableInfo.builder().nama(p.getKategori()).build() : null)
                        .build()).toList());

        if (s.getChangelogs() != null && !s.getChangelogs().isEmpty()) {
            b.changelogs(s.getChangelogs().stream().map(c -> ChangelogInfo.builder()
                    .id(c.getId()).tanggalPerubahan(c.getTanggalPerubahan())
                    .keterangan(c.getKeterangan()).perubahanDetail(c.getPerubahanDetail())
                    .createdAt(c.getCreatedAt()).build()).toList());
        }

        return b.build();
    }

    private AplikasiHistorisListResponse mapToListResponse(CatalogYearSnapshotListItem item) {
        AplikasiHistorisListResponse.AplikasiHistorisListResponseBuilder b =
                AplikasiHistorisListResponse.builder()
                        .aplikasiId(item.getAplikasiId())
                        .kodeAplikasi(item.getNamaAplikasi())
                        .namaAplikasi(item.getKepanjangan())
                        .statusAplikasi(item.getStatusAplikasi())
                        .tahun(item.getTahun())
                        .snapshotDate(item.getSnapshotDate());

        if (item.getBidang() != null) {
            b.bidangKode(item.getBidang());
            bidangRepository.findByKodeBidang(item.getBidang())
                    .ifPresent(bidang -> b.bidangNama(bidang.getNamaBidang()));
        }
        if (item.getSkpa() != null) {
            b.skpaKode(item.getSkpa());
            skpaRepository.findByKodeSkpa(item.getSkpa())
                    .ifPresent(skpa -> b.skpaNama(skpa.getNamaSkpa()));
        }

        return b.build();
    }

    private ChangelogInfo mapToChangelogInfo(CatalogChangelogResponse r) {
        return ChangelogInfo.builder()
                .id(r.getId())
                .tanggalPerubahan(r.getTanggalPerubahan())
                .keterangan(r.getKeterangan())
                .perubahanDetail(r.getPerubahanDetail())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private void enrichBidang(AplikasiSnapshotResponse.AplikasiSnapshotResponseBuilder b, String kodeBidang) {
        if (kodeBidang == null) return;
        bidangRepository.findByKodeBidang(kodeBidang).ifPresentOrElse(
                bidang -> b.bidang(BidangInfo.builder()
                        .id(bidang.getId()).kodeBidang(bidang.getKodeBidang()).namaBidang(bidang.getNamaBidang()).build()),
                () -> b.bidang(BidangInfo.builder().kodeBidang(kodeBidang).build()));
    }

    private void enrichSkpa(AplikasiSnapshotResponse.AplikasiSnapshotResponseBuilder b, String kodeSkpa) {
        if (kodeSkpa == null) return;
        skpaRepository.findByKodeSkpa(kodeSkpa).ifPresentOrElse(
                skpa -> b.skpa(SkpaInfo.builder()
                        .id(skpa.getId()).kodeSkpa(skpa.getKodeSkpa()).namaSkpa(skpa.getNamaSkpa()).build()),
                () -> b.skpa(SkpaInfo.builder().kodeSkpa(kodeSkpa).build()));
    }

    private <T> List<T> parseJsonList(String json, TypeReference<List<T>> typeRef) {
        if (json == null || json.isBlank() || "null".equals(json)) return List.of();
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.warn("Failed to parse JSON field: {}", e.getMessage());
            return List.of();
        }
    }
}
