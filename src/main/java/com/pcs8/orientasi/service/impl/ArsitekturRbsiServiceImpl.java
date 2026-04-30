package com.pcs8.orientasi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcs8.orientasi.config.UserContext;
import com.pcs8.orientasi.domain.dto.request.ArsitekturRbsiRequest;
import com.pcs8.orientasi.domain.dto.response.AplikasiResponse;
import com.pcs8.orientasi.domain.dto.response.ArsitekturRbsiResponse;
import com.pcs8.orientasi.domain.dto.response.SkpaResponse;
import com.pcs8.orientasi.domain.dto.response.SnapshotArsitekturRbsiResponse;
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

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArsitekturRbsiServiceImpl implements ArsitekturRbsiService {

    private static final Logger log = LoggerFactory.getLogger(ArsitekturRbsiServiceImpl.class);
    private static final String ENTITY_NAME = "Arsitektur RBSI";

    private static final Map<String, String> APLIKASI_STATUS_TO_YEAR_STATUS = Map.of(
            "AKTIF", "Aktif",
            "IDLE", "Idle",
            "DIAKHIRI", "Diakhiri"
    );

    private final MstArsitekturRbsiRepository arsitekturRepository;
    private final RbsiRepository rbsiRepository;
    private final MstSubKategoriRepository subKategoriRepository;
    private final MstAplikasiRepository aplikasiRepository;
    private final InisiatifGroupRepository inisiatifGroupRepository;
    private final MstSkpaRepository skpaRepository;
    private final SnapshotArsitekturRbsiRepository snapshotRepository;
    private final AuditService auditService;
    private final UserContext userContext;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ArsitekturRbsiResponse create(ArsitekturRbsiRequest request) {
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();

        MstArsitekturRbsi arsitektur = buildArsitektur(new MstArsitekturRbsi(), request);
        MstArsitekturRbsi saved = arsitekturRepository.save(arsitektur);
        log.info("ArsitekturRbsi created: {}", saved.getId());

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
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();

        MstArsitekturRbsi existing = arsitekturRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Arsitektur RBSI tidak ditemukan"));
        ArsitekturRbsiResponse oldValue = mapToResponse(existing);

        buildArsitektur(existing, request);
        MstArsitekturRbsi saved = arsitekturRepository.save(existing);
        log.info("ArsitekturRbsi updated: {}", saved.getId());

        ArsitekturRbsiResponse newValue = mapToResponse(saved);
        auditService.logUpdate(ENTITY_NAME, id, oldValue, newValue, userId, username);
        return newValue;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();

        MstArsitekturRbsi arsitektur = arsitekturRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Arsitektur RBSI tidak ditemukan"));
        ArsitekturRbsiResponse oldValue = mapToResponse(arsitektur);

        arsitekturRepository.delete(arsitektur);
        log.info("ArsitekturRbsi deleted: {}", id);
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
        return requests.stream().map(this::create).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ArsitekturRbsiResponse> bulkUpdate(List<ArsitekturRbsiRequest> requests) {
        return requests.stream()
                .filter(r -> r.getRbsiId() != null)
                .map(this::create)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ArsitekturRbsiResponse> updateData(UUID rbsiId) {
        Rbsi rbsi = rbsiRepository.findById(rbsiId)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        List<MstArsitekturRbsi> arsitekturList = arsitekturRepository.findByRbsiIdWithRelations(rbsiId);
        if (arsitekturList.isEmpty()) {
            return List.of();
        }

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();

        List<ArsitekturRbsiResponse> results = new ArrayList<>();

        for (MstArsitekturRbsi arsitektur : arsitekturList) {
            // 1. Simpan snapshot state saat ini — sebelum ada perubahan apapun
            saveSnapshot(rbsi, arsitektur, today);

            // 2. Sinkronisasi year_status tahun ini dengan status aplikasi aktual
            MstAplikasi aplikasiRef = arsitektur.getAplikasi();

            if (aplikasiRef != null && arsitektur.getYearStatuses() != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, String> yearStatuses = objectMapper.readValue(
                            arsitektur.getYearStatuses(), Map.class);

                    String yearKey = String.valueOf(currentYear);
                    String currentYearStatus = yearStatuses.get(yearKey);
                    String actualAppStatus = aplikasiRef.getStatusAplikasi();
                    String expectedYearStatus = APLIKASI_STATUS_TO_YEAR_STATUS.get(actualAppStatus);

                    if (expectedYearStatus != null && !expectedYearStatus.equals(currentYearStatus)) {
                        yearStatuses.put(yearKey, expectedYearStatus);
                        arsitektur.setYearStatuses(objectMapper.writeValueAsString(yearStatuses));
                        arsitektur = arsitekturRepository.save(arsitektur);
                        log.info("ArsitekturRbsi {} year_status {} updated: {} → {}",
                                arsitektur.getId(), currentYear, currentYearStatus, expectedYearStatus);
                    }
                } catch (JsonProcessingException e) {
                    log.error("Failed to parse year_statuses for arsitektur {}: {}", arsitektur.getId(), e.getMessage());
                }
            }

            results.add(mapToResponse(arsitektur));
        }

        log.info("UpdateData completed for RBSI {}: {} records processed", rbsiId, results.size());
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SnapshotArsitekturRbsiResponse.SnapshotGroup> getSnapshots(UUID rbsiId) {
        rbsiRepository.findById(rbsiId)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        List<SnapshotArsitekturRbsi> allSnapshots = snapshotRepository
                .findByRbsiIdOrderBySnapshotDateDesc(rbsiId);

        // Kelompokkan per tanggal
        Map<LocalDate, List<SnapshotArsitekturRbsi>> byDate = allSnapshots.stream()
                .collect(Collectors.groupingBy(SnapshotArsitekturRbsi::getSnapshotDate,
                        LinkedHashMap::new, Collectors.toList()));

        return byDate.entrySet().stream()
                .map(entry -> {
                    List<SnapshotArsitekturRbsiResponse> items = entry.getValue().stream()
                            .map(this::mapToSnapshotResponse)
                            .collect(Collectors.toList());
                    long changedItems = items.stream()
                            .filter(i -> i.getChanges() != null && !i.getChanges().equals("[]"))
                            .count();
                    return SnapshotArsitekturRbsiResponse.SnapshotGroup.builder()
                            .snapshotDate(entry.getKey())
                            .totalItems(items.size())
                            .changedItems((int) changedItems)
                            .items(items)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ==================== Helpers ====================

    private MstArsitekturRbsi buildArsitektur(MstArsitekturRbsi target, ArsitekturRbsiRequest request) {
        Rbsi rbsi = rbsiRepository.findById(request.getRbsiId())
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));
        target.setRbsi(rbsi);

        // Resolve aplikasi utama (sumber auto-fill)
        MstAplikasi aplikasi = null;
        if (request.getAplikasiId() != null) {
            aplikasi = aplikasiRepository.findById(request.getAplikasiId())
                    .orElseThrow(() -> new ResourceNotFoundException("Aplikasi tidak ditemukan"));
        }
        target.setAplikasi(aplikasi);

        // Sub kategori: pakai dari request jika ada, fallback ke aplikasi
        if (request.getSubKategoriId() != null) {
            target.setSubKategori(subKategoriRepository.findById(request.getSubKategoriId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sub Kategori tidak ditemukan")));
        } else if (aplikasi != null && aplikasi.getSubKategori() != null) {
            target.setSubKategori(aplikasi.getSubKategori());
        } else {
            target.setSubKategori(null);
        }

        // SKPA: pakai dari request jika ada, fallback ke aplikasi
        if (request.getSkpaId() != null) {
            target.setSkpa(skpaRepository.findById(request.getSkpaId())
                    .orElseThrow(() -> new ResourceNotFoundException("SKPA tidak ditemukan")));
        } else if (aplikasi != null && aplikasi.getSkpa() != null) {
            target.setSkpa(aplikasi.getSkpa());
        } else {
            target.setSkpa(null);
        }

        // Baseline & target aplikasi
        target.setAplikasiBaseline(request.getAplikasiBaseline());
        target.setAplikasiTarget(request.getAplikasiTarget());

        target.setAction(request.getAction());
        target.setYearStatuses(request.getYearStatuses());
        target.setKeterangan(request.getKeterangan());

        // Inisiatif group
        if (request.getInisiatifGroupId() != null) {
            InisiatifGroup group = inisiatifGroupRepository.findById(request.getInisiatifGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inisiatif Group tidak ditemukan"));
            target.setInisiatifGroup(group);
        } else {
            target.setInisiatifGroup(null);
        }

        return target;
    }

    private void saveSnapshot(Rbsi rbsi, MstArsitekturRbsi arsitektur, LocalDate date) {
        // Upsert: kalau sudah ada snapshot untuk (rbsi, date, arsitektur), update data-nya
        Optional<SnapshotArsitekturRbsi> existing = snapshotRepository
                .findByRbsiIdAndSnapshotDateAndArsitekturId(rbsi.getId(), date, arsitektur.getId());

        // Ambil snapshot terakhir sebelum hari ini untuk menghitung diff
        Optional<SnapshotArsitekturRbsi> previous = snapshotRepository
                .findLatestBeforeDate(rbsi.getId(), arsitektur.getId(), date);

        String changesJson = buildChangesJson(arsitektur, previous.orElse(null));

        SnapshotArsitekturRbsi snapshot = existing.orElseGet(() -> SnapshotArsitekturRbsi.builder()
                .rbsi(rbsi)
                .arsitekturId(arsitektur.getId())
                .snapshotDate(date)
                .build());

        // Selalu update data fields (state aktual saat ini)
        snapshot.setSubKategoriKode(arsitektur.getSubKategori() != null ? arsitektur.getSubKategori().getKode() : null);
        snapshot.setSubKategoriNama(arsitektur.getSubKategori() != null ? arsitektur.getSubKategori().getNama() : null);
        snapshot.setAplikasiKode(arsitektur.getAplikasi() != null ? arsitektur.getAplikasi().getKodeAplikasi() : null);
        snapshot.setAplikasiNama(arsitektur.getAplikasi() != null ? arsitektur.getAplikasi().getNamaAplikasi() : null);
        snapshot.setAplikasiBaselineKode(arsitektur.getAplikasiBaseline());
        snapshot.setAplikasiBaselineNama(arsitektur.getAplikasiBaseline());
        snapshot.setAplikasiTargetKode(arsitektur.getAplikasiTarget());
        snapshot.setAplikasiTargetNama(arsitektur.getAplikasiTarget());
        snapshot.setAction(arsitektur.getAction());
        snapshot.setYearStatuses(arsitektur.getYearStatuses());
        snapshot.setInisiatifGroupId(arsitektur.getInisiatifGroup() != null ? arsitektur.getInisiatifGroup().getId() : null);
        snapshot.setInisiatifGroupNama(arsitektur.getInisiatifGroup() != null ? arsitektur.getInisiatifGroup().getNamaInisiatif() : null);
        snapshot.setSkpaKode(arsitektur.getSkpa() != null ? arsitektur.getSkpa().getKodeSkpa() : null);
        snapshot.setSkpaNama(arsitektur.getSkpa() != null ? arsitektur.getSkpa().getNamaSkpa() : null);
        snapshot.setKeterangan(arsitektur.getKeterangan());
        snapshot.setChanges(changesJson);

        snapshotRepository.save(snapshot);
    }

    private String buildChangesJson(MstArsitekturRbsi current, SnapshotArsitekturRbsi previous) {
        if (previous == null) return null;

        List<Map<String, String>> changes = new ArrayList<>();

        checkField(changes, "sub_kategori",
                previous.getSubKategoriKode(),
                current.getSubKategori() != null ? current.getSubKategori().getKode() : null);
        checkField(changes, "aplikasi_baseline",
                previous.getAplikasiBaselineKode(),
                current.getAplikasiBaseline() != null ? current.getAplikasiBaseline() : null);
        checkField(changes, "aplikasi_target",
                previous.getAplikasiTargetKode(),
                current.getAplikasiTarget() != null ? current.getAplikasiTarget() : null);
        checkField(changes, "action",
                previous.getAction(),
                current.getAction());
        checkField(changes, "inisiatif_group",
                previous.getInisiatifGroupNama(),
                current.getInisiatifGroup() != null ? current.getInisiatifGroup().getNamaInisiatif() : null);
        checkField(changes, "skpa",
                previous.getSkpaKode(),
                current.getSkpa() != null ? current.getSkpa().getKodeSkpa() : null);
        checkField(changes, "keterangan",
                previous.getKeterangan(),
                current.getKeterangan());

        // Diff year_statuses per tahun
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> prevYS = previous.getYearStatuses() != null
                    ? objectMapper.readValue(previous.getYearStatuses(), Map.class) : Map.of();
            @SuppressWarnings("unchecked")
            Map<String, String> currYS = current.getYearStatuses() != null
                    ? objectMapper.readValue(current.getYearStatuses(), Map.class) : Map.of();

            Set<String> allYears = new HashSet<>(prevYS.keySet());
            allYears.addAll(currYS.keySet());
            for (String y : allYears) {
                checkField(changes, "year_status_" + y, prevYS.get(y), currYS.get(y));
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to diff year_statuses for arsitektur {}: {}", current.getId(), e.getMessage());
        }

        if (changes.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(changes);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private void checkField(List<Map<String, String>> changes, String field, String oldVal, String newVal) {
        String o = oldVal != null ? oldVal : "";
        String n = newVal != null ? newVal : "";
        if (!o.equals(n)) {
            changes.add(Map.of("field", field, "oldValue", o, "newValue", n));
        }
    }

    private ArsitekturRbsiResponse mapToResponse(MstArsitekturRbsi entity) {
        ArsitekturRbsiResponse.ArsitekturRbsiResponseBuilder builder = ArsitekturRbsiResponse.builder()
                .id(entity.getId())
                .action(entity.getAction())
                .yearStatuses(entity.getYearStatuses())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        if (entity.getRbsi() != null) {
            builder.rbsiId(entity.getRbsi().getId())
                   .rbsiPeriode(entity.getRbsi().getPeriode());
        }
        if (entity.getSubKategori() != null) {
            builder.subKategori(SubKategoriResponse.builder()
                    .id(entity.getSubKategori().getId())
                    .kode(entity.getSubKategori().getKode())
                    .nama(entity.getSubKategori().getNama())
                    .categoryCode(entity.getSubKategori().getCategoryCode())
                    .build());
        }
        if (entity.getAplikasi() != null) {
            builder.aplikasi(buildAplikasiResponse(entity.getAplikasi()));
        }
        if (entity.getAplikasiBaseline() != null) {
            builder.aplikasiBaseline(entity.getAplikasiBaseline());
        }
        if (entity.getAplikasiTarget() != null) {
            builder.aplikasiTarget(entity.getAplikasiTarget());
        }
        if (entity.getInisiatifGroup() != null) {
            builder.inisiatifGroup(ArsitekturRbsiResponse.InisiatifGroupSimpleResponse.builder()
                    .id(entity.getInisiatifGroup().getId())
                    .namaInisiatif(entity.getInisiatifGroup().getNamaInisiatif())
                    .keterangan(entity.getInisiatifGroup().getKeterangan())
                    .build());
        }
        if (entity.getSkpa() != null) {
            builder.skpa(SkpaResponse.builder()
                    .id(entity.getSkpa().getId())
                    .kodeSkpa(entity.getSkpa().getKodeSkpa())
                    .namaSkpa(entity.getSkpa().getNamaSkpa())
                    .build());
        }
        builder.keterangan(entity.getKeterangan());
        return builder.build();
    }

    private AplikasiResponse buildAplikasiResponse(MstAplikasi app) {
        return AplikasiResponse.builder()
                .id(app.getId())
                .kodeAplikasi(app.getKodeAplikasi())
                .namaAplikasi(app.getNamaAplikasi())
                .statusAplikasi(app.getStatusAplikasi())
                .build();
    }

    private SnapshotArsitekturRbsiResponse mapToSnapshotResponse(SnapshotArsitekturRbsi entity) {
        return SnapshotArsitekturRbsiResponse.builder()
                .id(entity.getId())
                .rbsiId(entity.getRbsi().getId())
                .snapshotDate(entity.getSnapshotDate())
                .arsitekturId(entity.getArsitekturId())
                .subKategoriKode(entity.getSubKategoriKode())
                .subKategoriNama(entity.getSubKategoriNama())
                .aplikasiKode(entity.getAplikasiKode())
                .aplikasiNama(entity.getAplikasiNama())
                .aplikasiBaselineKode(entity.getAplikasiBaselineKode())
                .aplikasiBaselineNama(entity.getAplikasiBaselineNama())
                .aplikasiTargetKode(entity.getAplikasiTargetKode())
                .aplikasiTargetNama(entity.getAplikasiTargetNama())
                .action(entity.getAction())
                .yearStatuses(entity.getYearStatuses())
                .inisiatifGroupId(entity.getInisiatifGroupId())
                .inisiatifGroupNama(entity.getInisiatifGroupNama())
                .skpaKode(entity.getSkpaKode())
                .skpaNama(entity.getSkpaNama())
                .keterangan(entity.getKeterangan())
                .changes(entity.getChanges())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
