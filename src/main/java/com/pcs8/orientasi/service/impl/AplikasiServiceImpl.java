package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.AplikasiRequest;
import com.pcs8.orientasi.domain.dto.response.AplikasiResponse;
import com.pcs8.orientasi.domain.entity.MstAplikasi;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstAplikasiRepository;
import com.pcs8.orientasi.service.AplikasiService;
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
public class AplikasiServiceImpl implements AplikasiService {

    private static final Logger log = LoggerFactory.getLogger(AplikasiServiceImpl.class);

    private final MstAplikasiRepository aplikasiRepository;

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
                .build();

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

        MstAplikasi updated = aplikasiRepository.save(aplikasi);
        log.info("Aplikasi updated: {} - {}", updated.getKodeAplikasi(), updated.getNamaAplikasi());

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
        return AplikasiResponse.builder()
                .id(entity.getId())
                .kodeAplikasi(entity.getKodeAplikasi())
                .namaAplikasi(entity.getNamaAplikasi())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
