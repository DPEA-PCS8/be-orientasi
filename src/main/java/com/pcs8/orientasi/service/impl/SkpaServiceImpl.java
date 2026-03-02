package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.SkpaRequest;
import com.pcs8.orientasi.domain.dto.response.SkpaResponse;
import com.pcs8.orientasi.domain.entity.MstSkpa;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstSkpaRepository;
import com.pcs8.orientasi.service.SkpaService;
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
public class SkpaServiceImpl implements SkpaService {

    private static final Logger log = LoggerFactory.getLogger(SkpaServiceImpl.class);

    private final MstSkpaRepository skpaRepository;

    @Override
    @Transactional
    public SkpaResponse create(SkpaRequest request) {
        String kode = request.getKodeSkpa().toUpperCase().trim();

        if (skpaRepository.existsByKodeSkpa(kode)) {
            throw new BadRequestException("SKPA dengan kode '" + kode + "' sudah ada");
        }

        MstSkpa skpa = MstSkpa.builder()
                .kodeSkpa(kode)
                .namaSkpa(request.getNamaSkpa().trim())
                .build();

        MstSkpa saved = skpaRepository.save(skpa);
        log.info("SKPA created: {} - {}", saved.getKodeSkpa(), saved.getNamaSkpa());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SkpaResponse getById(UUID id) {
        MstSkpa skpa = skpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SKPA tidak ditemukan"));
        return mapToResponse(skpa);
    }

    @Override
    @Transactional(readOnly = true)
    public SkpaResponse getByKode(String kode) {
        MstSkpa skpa = skpaRepository.findByKodeSkpa(kode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("SKPA dengan kode '" + kode + "' tidak ditemukan"));
        return mapToResponse(skpa);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkpaResponse> getAll() {
        return skpaRepository.findAllByOrderByKodeSkpaAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SkpaResponse update(UUID id, SkpaRequest request) {
        MstSkpa skpa = skpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SKPA tidak ditemukan"));

        String newKode = request.getKodeSkpa().toUpperCase().trim();

        if (!skpa.getKodeSkpa().equals(newKode) && skpaRepository.existsByKodeSkpa(newKode)) {
            throw new BadRequestException("SKPA dengan kode '" + newKode + "' sudah ada");
        }

        skpa.setKodeSkpa(newKode);
        skpa.setNamaSkpa(request.getNamaSkpa().trim());

        MstSkpa updated = skpaRepository.save(skpa);
        log.info("SKPA updated: {} - {}", updated.getKodeSkpa(), updated.getNamaSkpa());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        MstSkpa skpa = skpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SKPA tidak ditemukan"));

        skpaRepository.delete(skpa);
        log.info("SKPA deleted: {}", skpa.getKodeSkpa());
    }

    private SkpaResponse mapToResponse(MstSkpa entity) {
        return SkpaResponse.builder()
                .id(entity.getId())
                .kodeSkpa(entity.getKodeSkpa())
                .namaSkpa(entity.getNamaSkpa())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
