package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.RbsiRequest;
import com.pcs8.orientasi.domain.dto.response.RbsiResponse;
import com.pcs8.orientasi.domain.entity.MstRbsi;
import com.pcs8.orientasi.repository.RbsiRepository;
import com.pcs8.orientasi.service.RbsiService;
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
public class RbsiServiceImpl implements RbsiService {

    private static final Logger log = LoggerFactory.getLogger(RbsiServiceImpl.class);

    private final RbsiRepository rbsiRepository;

    @Override
    @Transactional
    public RbsiResponse create(RbsiRequest request) {
        log.info("Creating new RBSI with periode: {}", request.getPeriode());

        if (rbsiRepository.existsByPeriode(request.getPeriode())) {
            throw new IllegalArgumentException("RBSI with periode " + request.getPeriode() + " already exists");
        }

        MstRbsi rbsi = MstRbsi.builder()
                .periode(request.getPeriode())
                .isActive(true)
                .build();

        MstRbsi saved = rbsiRepository.save(rbsi);
        log.info("RBSI created successfully with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RbsiResponse> findAll() {
        log.debug("Fetching all active RBSI");
        return rbsiRepository.findAllActive().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RbsiResponse findById(UUID id) {
        log.debug("Fetching RBSI by id: {}", id);
        MstRbsi rbsi = rbsiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("RBSI not found with id: " + id));
        return mapToResponse(rbsi);
    }

    @Override
    @Transactional
    public RbsiResponse update(UUID id, RbsiRequest request) {
        log.info("Updating RBSI with id: {}", id);
        
        MstRbsi rbsi = rbsiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("RBSI not found with id: " + id));

        if (!rbsi.getPeriode().equals(request.getPeriode()) && 
            rbsiRepository.existsByPeriode(request.getPeriode())) {
            throw new IllegalArgumentException("RBSI with periode " + request.getPeriode() + " already exists");
        }

        rbsi.setPeriode(request.getPeriode());

        MstRbsi saved = rbsiRepository.save(rbsi);
        log.info("RBSI updated successfully");

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting RBSI with id: {}", id);
        
        MstRbsi rbsi = rbsiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("RBSI not found with id: " + id));

        rbsi.setIsActive(false);
        rbsiRepository.save(rbsi);
        
        log.info("RBSI soft deleted successfully");
    }

    private RbsiResponse mapToResponse(MstRbsi rbsi) {
        return RbsiResponse.builder()
                .id(rbsi.getId())
                .periode(rbsi.getPeriode())
                .isActive(rbsi.getIsActive())
                .createdAt(rbsi.getCreatedAt())
                .updatedAt(rbsi.getUpdatedAt())
                .build();
    }
}
