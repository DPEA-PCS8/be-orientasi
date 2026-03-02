package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.SubKategoriRequest;
import com.pcs8.orientasi.domain.dto.response.SubKategoriResponse;
import com.pcs8.orientasi.domain.entity.MstSubKategori;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstSubKategoriRepository;
import com.pcs8.orientasi.service.SubKategoriService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubKategoriServiceImpl implements SubKategoriService {

    private static final Logger log = LoggerFactory.getLogger(SubKategoriServiceImpl.class);

    private final MstSubKategoriRepository subKategoriRepository;

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
                .build();

        MstSubKategori saved = subKategoriRepository.save(subKategori);
        log.info("SubKategori created: {} - {}", saved.getKode(), saved.getNama());

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

        MstSubKategori updated = subKategoriRepository.save(subKategori);
        log.info("SubKategori updated: {} - {}", updated.getKode(), updated.getNama());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        MstSubKategori subKategori = subKategoriRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sub Kategori tidak ditemukan"));

        subKategoriRepository.delete(subKategori);
        log.info("SubKategori deleted: {}", subKategori.getKode());
    }

    private SubKategoriResponse mapToResponse(MstSubKategori entity) {
        return SubKategoriResponse.builder()
                .id(entity.getId())
                .kode(entity.getKode())
                .nama(entity.getNama())
                .categoryCode(entity.getCategoryCode())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
