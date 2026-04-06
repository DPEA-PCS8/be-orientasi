package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.config.UserContext;
import com.pcs8.orientasi.domain.dto.request.SubKategoriRequest;
import com.pcs8.orientasi.domain.dto.response.SubKategoriResponse;
import com.pcs8.orientasi.domain.dto.response.SubKategoriSnapshotResponse;
import com.pcs8.orientasi.domain.entity.MstSubKategori;
import com.pcs8.orientasi.domain.entity.MstSubKategoriSnapshot;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstSubKategoriRepository;
import com.pcs8.orientasi.repository.MstSubKategoriSnapshotRepository;
import com.pcs8.orientasi.service.SubKategoriService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubKategoriServiceImpl implements SubKategoriService {

    private static final Logger log = LoggerFactory.getLogger(SubKategoriServiceImpl.class);

    private final MstSubKategoriRepository subKategoriRepository;
    private final MstSubKategoriSnapshotRepository snapshotRepository;
    private final UserContext userContext;

    @Override
    @Transactional
    public SubKategoriResponse create(SubKategoriRequest request) {
        String kode = request.getKode().toUpperCase().trim();

        if (subKategoriRepository.existsByKode(kode)) {
            throw new BadRequestException("Sub Kategori dengan kode '" + kode + "' sudah ada");
        }

        MstSubKategori subKategori = MstSubKategori.builder()
                .kode(kode)
                .nama(request.getNama().trim())
                .categoryCode(request.getCategoryCode().toUpperCase().trim())
                .categoryName(request.getCategoryName().trim())
                .build();

        MstSubKategori saved = subKategoriRepository.save(subKategori);
        log.info("SubKategori created: {} - {}", saved.getKode(), saved.getNama());

        // Auto-create snapshot for current year
        createSnapshot(saved, "CREATED");

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SubKategoriResponse getById(UUID id) {
        MstSubKategori subKategori = subKategoriRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sub Kategori tidak ditemukan"));
        return mapToResponse(subKategori);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubKategoriResponse> getByCategoryCode(String categoryCode) {
        return subKategoriRepository.findByCategoryCodeOrderByKodeAsc(categoryCode.toUpperCase())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubKategoriResponse> getAll() {
        return subKategoriRepository.findAllByOrderByKodeAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategoryCodes() {
        return subKategoriRepository.findDistinctCategoryCodes();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getAllCategoryCodesWithNames() {
        List<MstSubKategori> allSubKategori = subKategoriRepository.findAllByOrderByKodeAsc();
        Map<String, String> result = new LinkedHashMap<>();
        
        for (MstSubKategori sk : allSubKategori) {
            if (!result.containsKey(sk.getCategoryCode())) {
                result.put(sk.getCategoryCode(), sk.getCategoryName());
            }
        }
        
        return result;
    }

    @Override
    @Transactional
    public SubKategoriResponse update(UUID id, SubKategoriRequest request) {
        MstSubKategori subKategori = subKategoriRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sub Kategori tidak ditemukan"));

        String newKode = request.getKode().toUpperCase().trim();

        if (!subKategori.getKode().equals(newKode) && subKategoriRepository.existsByKode(newKode)) {
            throw new BadRequestException("Sub Kategori dengan kode '" + newKode + "' sudah ada");
        }

        subKategori.setKode(newKode);
        subKategori.setNama(request.getNama().trim());
        subKategori.setCategoryCode(request.getCategoryCode().toUpperCase().trim());
        subKategori.setCategoryName(request.getCategoryName().trim());

        MstSubKategori updated = subKategoriRepository.save(subKategori);
        log.info("SubKategori updated: {} - {}", updated.getKode(), updated.getNama());

        // Auto-create snapshot for current year
        createSnapshot(updated, "UPDATED");

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        MstSubKategori subKategori = subKategoriRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sub Kategori tidak ditemukan"));

        // Create snapshot before delete
        createSnapshot(subKategori, "DELETED");

        subKategoriRepository.delete(subKategori);
        log.info("SubKategori deleted: {}", subKategori.getKode());
    }

    @Override
    @Transactional
    public void createYearlySnapshot(Integer year) {
        List<MstSubKategori> allSubKategori = subKategoriRepository.findAllByOrderByKodeAsc();
        String username = userContext.getCurrentUsername();

        for (MstSubKategori sk : allSubKategori) {
            MstSubKategoriSnapshot snapshot = MstSubKategoriSnapshot.builder()
                    .snapshotYear(year)
                    .subKategori(sk)
                    .kode(sk.getKode())
                    .nama(sk.getNama())
                    .categoryCode(sk.getCategoryCode())
                    .categoryName(sk.getCategoryName())
                    .snapshotDate(LocalDateTime.now())
                    .changeType("SNAPSHOT")
                    .createdBy(username)
                    .build();
            snapshotRepository.save(snapshot);
        }
        log.info("Yearly snapshot created for year {} with {} sub kategori", year, allSubKategori.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubKategoriSnapshotResponse> getSnapshotsByYear(Integer year) {
        return snapshotRepository.findByYearOrderByCategoryAndKode(year)
                .stream()
                .map(this::mapSnapshotToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getDistinctSnapshotYears() {
        return snapshotRepository.findDistinctSnapshotYears();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubKategoriSnapshotResponse> getSnapshotHistoryBySubKategoriId(UUID subKategoriId) {
        return snapshotRepository.findBySubKategoriIdOrderBySnapshotYearDescSnapshotDateDesc(subKategoriId)
                .stream()
                .map(this::mapSnapshotToResponse)
                .collect(Collectors.toList());
    }

    private void createSnapshot(MstSubKategori subKategori, String changeType) {
        try {
            int currentYear = Year.now().getValue();
            String username = userContext.getCurrentUsername();

            MstSubKategoriSnapshot snapshot = MstSubKategoriSnapshot.builder()
                    .snapshotYear(currentYear)
                    .subKategori(subKategori)
                    .kode(subKategori.getKode())
                    .nama(subKategori.getNama())
                    .categoryCode(subKategori.getCategoryCode())
                    .categoryName(subKategori.getCategoryName())
                    .snapshotDate(LocalDateTime.now())
                    .changeType(changeType)
                    .createdBy(username)
                    .build();
            snapshotRepository.save(snapshot);
            log.info("Snapshot created for SubKategori {} - {} ({})", subKategori.getKode(), subKategori.getNama(), changeType);
        } catch (Exception e) {
            log.warn("Failed to create snapshot for SubKategori {}: {}", subKategori.getKode(), e.getMessage());
        }
    }

    private SubKategoriResponse mapToResponse(MstSubKategori entity) {
        return SubKategoriResponse.builder()
                .id(entity.getId())
                .kode(entity.getKode())
                .nama(entity.getNama())
                .categoryCode(entity.getCategoryCode())
                .categoryName(entity.getCategoryName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private SubKategoriSnapshotResponse mapSnapshotToResponse(MstSubKategoriSnapshot entity) {
        return SubKategoriSnapshotResponse.builder()
                .id(entity.getId())
                .snapshotYear(entity.getSnapshotYear())
                .subKategoriId(entity.getSubKategori().getId())
                .kode(entity.getKode())
                .nama(entity.getNama())
                .categoryCode(entity.getCategoryCode())
                .categoryName(entity.getCategoryName())
                .snapshotDate(entity.getSnapshotDate())
                .changeType(entity.getChangeType())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }
}
