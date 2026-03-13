package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.constant.ConstantVariable;
import com.pcs8.orientasi.domain.dto.request.ChangelogRequest;
import com.pcs8.orientasi.domain.dto.request.UpdateSnapshotRequest;
import com.pcs8.orientasi.domain.dto.response.*;
import com.pcs8.orientasi.domain.entity.*;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.AplikasiChangelogRepository;
import com.pcs8.orientasi.repository.AplikasiSnapshotRepository;
import com.pcs8.orientasi.repository.MstAplikasiRepository;
import com.pcs8.orientasi.service.AplikasiHistorisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AplikasiHistorisServiceImpl implements AplikasiHistorisService {

    private static final Logger log = LoggerFactory.getLogger(AplikasiHistorisServiceImpl.class);

    private final AplikasiSnapshotRepository snapshotRepository;
    private final AplikasiChangelogRepository changelogRepository;
    private final MstAplikasiRepository aplikasiRepository;

    @Override
    @Transactional
    public AplikasiSnapshotResponse createOrUpdateSnapshot(UUID aplikasiId, Integer tahun, String snapshotType) {
        MstAplikasi aplikasi = aplikasiRepository.findById(aplikasiId)
                .orElseThrow(() -> new ResourceNotFoundException("Aplikasi tidak ditemukan"));
        
        return createSnapshotFromAplikasi(aplikasi, tahun, snapshotType);
    }

    @Override
    @Transactional
    public AplikasiSnapshotResponse createSnapshotFromAplikasi(MstAplikasi aplikasi, Integer tahun, String snapshotType) {
        // Check if snapshot already exists for this aplikasi and year
        Optional<AplikasiSnapshot> existingSnapshot = snapshotRepository.findByAplikasiIdAndTahun(aplikasi.getId(), tahun);
        
        AplikasiSnapshot snapshot;
        if (existingSnapshot.isPresent()) {
            // Update existing snapshot
            snapshot = existingSnapshot.get();
            updateSnapshotFromAplikasi(snapshot, aplikasi);
            snapshot.setSnapshotDate(LocalDateTime.now());
        } else {
            // Create new snapshot
            snapshot = buildSnapshotFromAplikasi(aplikasi, tahun, snapshotType);
        }
        
        AplikasiSnapshot saved = snapshotRepository.save(snapshot);
        log.info("Snapshot created/updated for aplikasi {} tahun {}", aplikasi.getKodeAplikasi(), tahun);
        
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AplikasiSnapshotResponse updateSnapshot(UUID snapshotId, UpdateSnapshotRequest request) {
        AplikasiSnapshot snapshot = snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new ResourceNotFoundException(ConstantVariable.SNAPSHOT_NOT_FOUND));
        
        // Update fields if provided
        if (request.getKodeAplikasi() != null) {
            snapshot.setKodeAplikasi(request.getKodeAplikasi());
        }
        if (request.getNamaAplikasi() != null) {
            snapshot.setNamaAplikasi(request.getNamaAplikasi());
        }
        if (request.getDeskripsi() != null) {
            snapshot.setDeskripsi(request.getDeskripsi());
        }
        if (request.getStatusAplikasi() != null) {
            snapshot.setStatusAplikasi(request.getStatusAplikasi());
        }
        if (request.getTanggalImplementasi() != null) {
            snapshot.setTanggalImplementasi(request.getTanggalImplementasi());
        }
        if (request.getAkses() != null) {
            snapshot.setAkses(request.getAkses());
        }
        if (request.getProsesDataPribadi() != null) {
            snapshot.setProsesDataPribadi(request.getProsesDataPribadi());
        }
        if (request.getKeteranganHistoris() != null) {
            snapshot.setKeteranganHistoris(request.getKeteranganHistoris());
        }
        
        // Update snapshot date
        snapshot.setSnapshotDate(LocalDateTime.now());
        
        AplikasiSnapshot saved = snapshotRepository.save(snapshot);
        
        // Add changelog entry if keterangan provided
        if (request.getChangelogKeterangan() != null && !request.getChangelogKeterangan().isBlank()) {
            AplikasiChangelog changelog = AplikasiChangelog.builder()
                    .snapshot(saved)
                    .tanggalPerubahan(request.getChangelogTanggal() != null ? request.getChangelogTanggal() : LocalDate.now())
                    .keterangan(request.getChangelogKeterangan())
                    .build();
            changelogRepository.save(changelog);
            log.info("Changelog added during snapshot update: {}", snapshotId);
        }
        
        log.info("Snapshot updated: {}", snapshotId);
        
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public List<AplikasiSnapshotResponse> generateSnapshotsForYear(Integer tahun) {
        List<MstAplikasi> allAplikasi = aplikasiRepository.findAllByOrderByKodeAplikasiAsc();
        List<AplikasiSnapshotResponse> results = new ArrayList<>();
        
        for (MstAplikasi aplikasi : allAplikasi) {
            AplikasiSnapshotResponse response = createSnapshotFromAplikasi(aplikasi, tahun, "MANUAL");
            results.add(response);
        }
        
        log.info("Generated {} snapshots for year {}", results.size(), tahun);
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public AplikasiSnapshotResponse getSnapshotById(UUID id) {
        AplikasiSnapshot snapshot = snapshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ConstantVariable.SNAPSHOT_NOT_FOUND));
        return mapToResponse(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public AplikasiSnapshotResponse getSnapshotByAplikasiAndTahun(UUID aplikasiId, Integer tahun) {
        AplikasiSnapshot snapshot = snapshotRepository.findByAplikasiIdAndTahun(aplikasiId, tahun)
                .orElseThrow(() -> new ResourceNotFoundException("Snapshot tidak ditemukan untuk aplikasi dan tahun tersebut"));
        return mapToResponse(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AplikasiHistorisListResponse> getHistorisByPeriode(Integer startYear, Integer endYear) {
        List<AplikasiSnapshot> snapshots = snapshotRepository.findByPeriode(startYear, endYear);
        return snapshots.stream()
                .map(this::mapToListResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AplikasiHistorisListResponse> getHistorisByTahun(Integer tahun) {
        List<AplikasiSnapshot> snapshots = snapshotRepository.findByTahunOrderByNamaAplikasi(tahun);
        return snapshots.stream()
                .map(this::mapToListResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getAvailableYears() {
        return snapshotRepository.findDistinctTahun();
    }

    @Override
    @Transactional(readOnly = true)
    public AplikasiStatistikResponse getStatistikByTahun(Integer tahun) {
        Long total = snapshotRepository.countByTahun(tahun);
        List<Object[]> statusCounts = snapshotRepository.countByStatusAndTahun(tahun);
        
        Map<String, Long> byStatus = new HashMap<>();
        for (Object[] row : statusCounts) {
            String status = (String) row[0];
            Long count = (Long) row[1];
            byStatus.put(status, count);
        }
        
        return AplikasiStatistikResponse.builder()
                .tahun(tahun)
                .totalAplikasi(total)
                .byStatus(byStatus)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AplikasiStatistikResponse> getStatistikByPeriode(Integer startYear, Integer endYear) {
        List<AplikasiStatistikResponse> results = new ArrayList<>();
        for (int year = startYear; year <= endYear; year++) {
            results.add(getStatistikByTahun(year));
        }
        return results;
    }

    @Override
    @Transactional
    public ChangelogInfo addChangelog(UUID snapshotId, ChangelogRequest request) {
        AplikasiSnapshot snapshot = snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new ResourceNotFoundException(ConstantVariable.SNAPSHOT_NOT_FOUND));
        
        AplikasiChangelog changelog = AplikasiChangelog.builder()
                .snapshot(snapshot)
                .tanggalPerubahan(request.getTanggalPerubahan())
                .keterangan(request.getKeterangan())
                .build();
        
        AplikasiChangelog saved = changelogRepository.save(changelog);
        log.info("Changelog added to snapshot {}", snapshotId);
        
        return mapToChangelogInfo(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChangelogInfo> getChangelogsBySnapshotId(UUID snapshotId) {
        return changelogRepository.findBySnapshotIdOrderByTanggalPerubahanDesc(snapshotId)
                .stream()
                .map(this::mapToChangelogInfo)
                .toList();
    }

    @Override
    @Transactional
    public void deleteChangelog(UUID changelogId) {
        if (!changelogRepository.existsById(changelogId)) {
            throw new ResourceNotFoundException("Changelog tidak ditemukan");
        }
        changelogRepository.deleteById(changelogId);
        log.info("Changelog deleted: {}", changelogId);
    }

    @Override
    @Transactional
    public void deleteSnapshot(UUID id) {
        if (!snapshotRepository.existsById(id)) {
            throw new ResourceNotFoundException(ConstantVariable.SNAPSHOT_NOT_FOUND);
        }
        snapshotRepository.deleteById(id);
        log.info("Snapshot deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AplikasiSnapshotResponse> getSnapshotsByAplikasiId(UUID aplikasiId) {
        return snapshotRepository.findByAplikasiIdOrderByTahunDesc(aplikasiId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void onAplikasiUpdated(MstAplikasi aplikasi, String keterangan) {
        int currentYear = LocalDate.now().getYear();
        
        // Create or update snapshot for current year
        Optional<AplikasiSnapshot> existingSnapshot = snapshotRepository.findByAplikasiIdAndTahun(aplikasi.getId(), currentYear);
        
        AplikasiSnapshot snapshot;
        if (existingSnapshot.isPresent()) {
            snapshot = existingSnapshot.get();
            updateSnapshotFromAplikasi(snapshot, aplikasi);
            snapshot.setSnapshotDate(LocalDateTime.now());
        } else {
            snapshot = buildSnapshotFromAplikasi(aplikasi, currentYear, "AUTO");
        }
        
        AplikasiSnapshot saved = snapshotRepository.save(snapshot);
        
        // Add changelog entry if keterangan provided
        if (keterangan != null && !keterangan.isBlank()) {
            AplikasiChangelog changelog = AplikasiChangelog.builder()
                    .snapshot(saved)
                    .tanggalPerubahan(LocalDate.now())
                    .keterangan(keterangan)
                    .build();
            changelogRepository.save(changelog);
        }
        
        log.info("Snapshot auto-updated for aplikasi {} tahun {}", aplikasi.getKodeAplikasi(), currentYear);
    }

    // ==================== Private Helper Methods ====================

    private AplikasiSnapshot buildSnapshotFromAplikasi(MstAplikasi aplikasi, Integer tahun, String snapshotType) {
        AplikasiSnapshot snapshot = AplikasiSnapshot.builder()
                .aplikasi(aplikasi)
                .tahun(tahun)
                .kodeAplikasi(aplikasi.getKodeAplikasi())
                .namaAplikasi(aplikasi.getNamaAplikasi())
                .deskripsi(aplikasi.getDeskripsi())
                .statusAplikasi(aplikasi.getStatusAplikasi())
                .tanggalStatus(aplikasi.getTanggalStatus())
                .tanggalImplementasi(aplikasi.getTanggalImplementasi())
                .akses(aplikasi.getAkses())
                .prosesDataPribadi(aplikasi.getProsesDataPribadi())
                .dataPribadiDiproses(aplikasi.getDataPribadiDiproses())
                .kategoriIdle(aplikasi.getKategoriIdle())
                .alasanIdle(aplikasi.getAlasanIdle())
                .rencanaPengakhiran(aplikasi.getRencanaPengakhiran())
                .alasanBelumDiakhiri(aplikasi.getAlasanBelumDiakhiri())
                .snapshotDate(LocalDateTime.now())
                .snapshotType(snapshotType)
                .urls(new ArrayList<>())
                .satkerInternals(new ArrayList<>())
                .penggunaEksternals(new ArrayList<>())
                .komunikasiSistems(new ArrayList<>())
                .penghargaans(new ArrayList<>())
                .changelogs(new ArrayList<>())
                .build();

        // Set bidang info
        if (aplikasi.getBidang() != null) {
            snapshot.setBidangId(aplikasi.getBidang().getId());
            snapshot.setBidangKode(aplikasi.getBidang().getKodeBidang());
            snapshot.setBidangNama(aplikasi.getBidang().getNamaBidang());
        }

        // Set skpa info
        if (aplikasi.getSkpa() != null) {
            snapshot.setSkpaId(aplikasi.getSkpa().getId());
            snapshot.setSkpaKode(aplikasi.getSkpa().getKodeSkpa());
            snapshot.setSkpaNama(aplikasi.getSkpa().getNamaSkpa());
        }

        // Copy URLs
        if (aplikasi.getUrls() != null) {
            for (AplikasiUrl url : aplikasi.getUrls()) {
                snapshot.getUrls().add(AplikasiSnapshotUrl.builder()
                        .snapshot(snapshot)
                        .url(url.getUrl())
                        .tipeAkses(url.getTipeAkses())
                        .keterangan(url.getKeterangan())
                        .build());
            }
        }

        // Copy Satker Internals
        if (aplikasi.getSatkerInternals() != null) {
            for (AplikasiSatkerInternal satker : aplikasi.getSatkerInternals()) {
                snapshot.getSatkerInternals().add(AplikasiSnapshotSatkerInternal.builder()
                        .snapshot(snapshot)
                        .namaSatker(satker.getNamaSatker())
                        .keterangan(satker.getKeterangan())
                        .build());
            }
        }

        // Copy Pengguna Eksternals
        if (aplikasi.getPenggunaEksternals() != null) {
            for (AplikasiPenggunaEksternal pengguna : aplikasi.getPenggunaEksternals()) {
                snapshot.getPenggunaEksternals().add(AplikasiSnapshotPenggunaEksternal.builder()
                        .snapshot(snapshot)
                        .namaPengguna(pengguna.getNamaPengguna())
                        .keterangan(pengguna.getKeterangan())
                        .build());
            }
        }

        // Copy Komunikasi Sistems
        if (aplikasi.getKomunikasiSistems() != null) {
            for (AplikasiKomunikasiSistem kom : aplikasi.getKomunikasiSistems()) {
                snapshot.getKomunikasiSistems().add(AplikasiSnapshotKomunikasiSistem.builder()
                        .snapshot(snapshot)
                        .namaSistem(kom.getNamaSistem())
                        .tipeSistem(kom.getTipeSistem())
                        .deskripsiKomunikasi(kom.getDeskripsiKomunikasi())
                        .keterangan(kom.getKeterangan())
                        .isPlanned(kom.getIsPlanned())
                        .build());
            }
        }

        // Copy Penghargaans
        if (aplikasi.getPenghargaans() != null) {
            for (AplikasiPenghargaan penghargaan : aplikasi.getPenghargaans()) {
                AplikasiSnapshotPenghargaan sp = AplikasiSnapshotPenghargaan.builder()
                        .snapshot(snapshot)
                        .tanggal(penghargaan.getTanggal())
                        .deskripsi(penghargaan.getDeskripsi())
                        .build();
                
                if (penghargaan.getKategori() != null) {
                    sp.setKategoriId(penghargaan.getKategori().getId());
                    sp.setKategoriKode(penghargaan.getKategori().getKode());
                    sp.setKategoriNama(penghargaan.getKategori().getNama());
                }
                
                snapshot.getPenghargaans().add(sp);
            }
        }

        return snapshot;
    }

    private void updateSnapshotFromAplikasi(AplikasiSnapshot snapshot, MstAplikasi aplikasi) {
        snapshot.setKodeAplikasi(aplikasi.getKodeAplikasi());
        snapshot.setNamaAplikasi(aplikasi.getNamaAplikasi());
        snapshot.setDeskripsi(aplikasi.getDeskripsi());
        snapshot.setStatusAplikasi(aplikasi.getStatusAplikasi());
        snapshot.setTanggalStatus(aplikasi.getTanggalStatus());
        snapshot.setTanggalImplementasi(aplikasi.getTanggalImplementasi());
        snapshot.setAkses(aplikasi.getAkses());
        snapshot.setProsesDataPribadi(aplikasi.getProsesDataPribadi());
        snapshot.setDataPribadiDiproses(aplikasi.getDataPribadiDiproses());
        snapshot.setKategoriIdle(aplikasi.getKategoriIdle());
        snapshot.setAlasanIdle(aplikasi.getAlasanIdle());
        snapshot.setRencanaPengakhiran(aplikasi.getRencanaPengakhiran());
        snapshot.setAlasanBelumDiakhiri(aplikasi.getAlasanBelumDiakhiri());

        // Update bidang info
        if (aplikasi.getBidang() != null) {
            snapshot.setBidangId(aplikasi.getBidang().getId());
            snapshot.setBidangKode(aplikasi.getBidang().getKodeBidang());
            snapshot.setBidangNama(aplikasi.getBidang().getNamaBidang());
        } else {
            snapshot.setBidangId(null);
            snapshot.setBidangKode(null);
            snapshot.setBidangNama(null);
        }

        // Update skpa info
        if (aplikasi.getSkpa() != null) {
            snapshot.setSkpaId(aplikasi.getSkpa().getId());
            snapshot.setSkpaKode(aplikasi.getSkpa().getKodeSkpa());
            snapshot.setSkpaNama(aplikasi.getSkpa().getNamaSkpa());
        } else {
            snapshot.setSkpaId(null);
            snapshot.setSkpaKode(null);
            snapshot.setSkpaNama(null);
        }

        // Clear and rebuild URLs
        snapshot.getUrls().clear();
        if (aplikasi.getUrls() != null) {
            for (AplikasiUrl url : aplikasi.getUrls()) {
                snapshot.getUrls().add(AplikasiSnapshotUrl.builder()
                        .snapshot(snapshot)
                        .url(url.getUrl())
                        .tipeAkses(url.getTipeAkses())
                        .keterangan(url.getKeterangan())
                        .build());
            }
        }

        // Clear and rebuild Satker Internals
        snapshot.getSatkerInternals().clear();
        if (aplikasi.getSatkerInternals() != null) {
            for (AplikasiSatkerInternal satker : aplikasi.getSatkerInternals()) {
                snapshot.getSatkerInternals().add(AplikasiSnapshotSatkerInternal.builder()
                        .snapshot(snapshot)
                        .namaSatker(satker.getNamaSatker())
                        .keterangan(satker.getKeterangan())
                        .build());
            }
        }

        // Clear and rebuild Pengguna Eksternals
        snapshot.getPenggunaEksternals().clear();
        if (aplikasi.getPenggunaEksternals() != null) {
            for (AplikasiPenggunaEksternal pengguna : aplikasi.getPenggunaEksternals()) {
                snapshot.getPenggunaEksternals().add(AplikasiSnapshotPenggunaEksternal.builder()
                        .snapshot(snapshot)
                        .namaPengguna(pengguna.getNamaPengguna())
                        .keterangan(pengguna.getKeterangan())
                        .build());
            }
        }

        // Clear and rebuild Komunikasi Sistems
        snapshot.getKomunikasiSistems().clear();
        if (aplikasi.getKomunikasiSistems() != null) {
            for (AplikasiKomunikasiSistem kom : aplikasi.getKomunikasiSistems()) {
                snapshot.getKomunikasiSistems().add(AplikasiSnapshotKomunikasiSistem.builder()
                        .snapshot(snapshot)
                        .namaSistem(kom.getNamaSistem())
                        .tipeSistem(kom.getTipeSistem())
                        .deskripsiKomunikasi(kom.getDeskripsiKomunikasi())
                        .keterangan(kom.getKeterangan())
                        .isPlanned(kom.getIsPlanned())
                        .build());
            }
        }

        // Clear and rebuild Penghargaans
        snapshot.getPenghargaans().clear();
        if (aplikasi.getPenghargaans() != null) {
            for (AplikasiPenghargaan penghargaan : aplikasi.getPenghargaans()) {
                AplikasiSnapshotPenghargaan sp = AplikasiSnapshotPenghargaan.builder()
                        .snapshot(snapshot)
                        .tanggal(penghargaan.getTanggal())
                        .deskripsi(penghargaan.getDeskripsi())
                        .build();
                
                if (penghargaan.getKategori() != null) {
                    sp.setKategoriId(penghargaan.getKategori().getId());
                    sp.setKategoriKode(penghargaan.getKategori().getKode());
                    sp.setKategoriNama(penghargaan.getKategori().getNama());
                }
                
                snapshot.getPenghargaans().add(sp);
            }
        }
    }

    private AplikasiSnapshotResponse mapToResponse(AplikasiSnapshot snapshot) {
        AplikasiSnapshotResponse.AplikasiSnapshotResponseBuilder builder = AplikasiSnapshotResponse.builder()
                .id(snapshot.getId())
                .aplikasiId(snapshot.getAplikasi().getId())
                .tahun(snapshot.getTahun())
                .kodeAplikasi(snapshot.getKodeAplikasi())
                .namaAplikasi(snapshot.getNamaAplikasi())
                .deskripsi(snapshot.getDeskripsi())
                .statusAplikasi(snapshot.getStatusAplikasi())
                .tanggalStatus(snapshot.getTanggalStatus())
                .tanggalImplementasi(snapshot.getTanggalImplementasi())
                .akses(snapshot.getAkses())
                .prosesDataPribadi(snapshot.getProsesDataPribadi())
                .dataPribadiDiproses(snapshot.getDataPribadiDiproses())
                .snapshotDate(snapshot.getSnapshotDate())
                .snapshotType(snapshot.getSnapshotType())
                .createdAt(snapshot.getCreatedAt())
                .updatedAt(snapshot.getUpdatedAt());

        // Map idle info
        if (snapshot.getKategoriIdle() != null || snapshot.getAlasanIdle() != null) {
            builder.idleInfo(IdleInfo.builder()
                    .kategoriIdle(snapshot.getKategoriIdle())
                    .alasanIdle(snapshot.getAlasanIdle())
                    .rencanaPengakhiran(snapshot.getRencanaPengakhiran())
                    .alasanBelumDiakhiri(snapshot.getAlasanBelumDiakhiri())
                    .build());
        }

        // Map bidang
        if (snapshot.getBidangId() != null) {
            builder.bidang(BidangInfo.builder()
                    .id(snapshot.getBidangId())
                    .kodeBidang(snapshot.getBidangKode())
                    .namaBidang(snapshot.getBidangNama())
                    .build());
        }

        // Map skpa
        if (snapshot.getSkpaId() != null) {
            builder.skpa(SkpaInfo.builder()
                    .id(snapshot.getSkpaId())
                    .kodeSkpa(snapshot.getSkpaKode())
                    .namaSkpa(snapshot.getSkpaNama())
                    .build());
        }

        // Map URLs
        if (snapshot.getUrls() != null && !snapshot.getUrls().isEmpty()) {
            builder.urls(snapshot.getUrls().stream()
                    .map(url -> UrlInfo.builder()
                            .id(url.getId())
                            .url(url.getUrl())
                            .tipeAkses(url.getTipeAkses())
                            .keterangan(url.getKeterangan())
                            .build())
                    .toList());
        }

        // Map Satker Internals
        if (snapshot.getSatkerInternals() != null && !snapshot.getSatkerInternals().isEmpty()) {
            builder.satkerInternals(snapshot.getSatkerInternals().stream()
                    .map(satker -> SatkerInternalInfo.builder()
                            .id(satker.getId())
                            .namaSatker(satker.getNamaSatker())
                            .keterangan(satker.getKeterangan())
                            .build())
                    .toList());
        }

        // Map Pengguna Eksternals
        if (snapshot.getPenggunaEksternals() != null && !snapshot.getPenggunaEksternals().isEmpty()) {
            builder.penggunaEksternals(snapshot.getPenggunaEksternals().stream()
                    .map(pengguna -> PenggunaEksternalInfo.builder()
                            .id(pengguna.getId())
                            .namaPengguna(pengguna.getNamaPengguna())
                            .keterangan(pengguna.getKeterangan())
                            .build())
                    .toList());
        }

        // Map Komunikasi Sistems
        if (snapshot.getKomunikasiSistems() != null && !snapshot.getKomunikasiSistems().isEmpty()) {
            builder.komunikasiSistems(snapshot.getKomunikasiSistems().stream()
                    .map(kom -> KomunikasiSistemInfo.builder()
                            .id(kom.getId())
                            .namaSistem(kom.getNamaSistem())
                            .tipeSistem(kom.getTipeSistem())
                            .deskripsiKomunikasi(kom.getDeskripsiKomunikasi())
                            .keterangan(kom.getKeterangan())
                            .isPlanned(kom.getIsPlanned())
                            .build())
                    .toList());
        }

        // Map Penghargaans
        if (snapshot.getPenghargaans() != null && !snapshot.getPenghargaans().isEmpty()) {
            builder.penghargaans(snapshot.getPenghargaans().stream()
                    .map(p -> PenghargaanInfo.builder()
                            .id(p.getId())
                            .kategori(p.getKategoriId() != null ? 
                                    VariableInfo.builder()
                                            .id(p.getKategoriId())
                                            .kode(p.getKategoriKode())
                                            .nama(p.getKategoriNama())
                                            .build() : null)
                            .tanggal(p.getTanggal())
                            .deskripsi(p.getDeskripsi())
                            .build())
                    .toList());
        }

        // Map Changelogs
        if (snapshot.getChangelogs() != null && !snapshot.getChangelogs().isEmpty()) {
            builder.changelogs(snapshot.getChangelogs().stream()
                    .map(this::mapToChangelogInfo)
                    .toList());
        }

        return builder.build();
    }

    private AplikasiHistorisListResponse mapToListResponse(AplikasiSnapshot snapshot) {
        // Get the latest changelog keterangan if any
        String keteranganHistoris = null;
        if (snapshot.getChangelogs() != null && !snapshot.getChangelogs().isEmpty()) {
            keteranganHistoris = snapshot.getChangelogs().stream()
                    .sorted((a, b) -> b.getTanggalPerubahan().compareTo(a.getTanggalPerubahan()))
                    .map(AplikasiChangelog::getKeterangan)
                    .collect(Collectors.joining("; "));
        }

        return AplikasiHistorisListResponse.builder()
                .aplikasiId(snapshot.getAplikasi().getId())
                .kodeAplikasi(snapshot.getKodeAplikasi())
                .namaAplikasi(snapshot.getNamaAplikasi())
                .bidangKode(snapshot.getBidangKode())
                .bidangNama(snapshot.getBidangNama())
                .skpaKode(snapshot.getSkpaKode())
                .skpaNama(snapshot.getSkpaNama())
                .statusAplikasi(snapshot.getStatusAplikasi())
                .keteranganHistoris(keteranganHistoris)
                .tahun(snapshot.getTahun())
                .snapshotDate(snapshot.getSnapshotDate())
                .build();
    }

    private ChangelogInfo mapToChangelogInfo(AplikasiChangelog changelog) {
        return ChangelogInfo.builder()
                .id(changelog.getId())
                .tanggalPerubahan(changelog.getTanggalPerubahan())
                .keterangan(changelog.getKeterangan())
                .perubahanDetail(changelog.getPerubahanDetail())
                .createdAt(changelog.getCreatedAt())
                .build();
    }
}
