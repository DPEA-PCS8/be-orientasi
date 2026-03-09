package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.config.UserContext;
import com.pcs8.orientasi.domain.dto.request.BidangRequest;
import com.pcs8.orientasi.domain.dto.response.BidangResponse;
import com.pcs8.orientasi.domain.entity.MstBidang;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstBidangRepository;
import com.pcs8.orientasi.service.AuditService;
import com.pcs8.orientasi.service.BidangService;
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
public class BidangServiceImpl implements BidangService {

    private static final Logger log = LoggerFactory.getLogger(BidangServiceImpl.class);
    private static final String ENTITY_NAME = "Bidang";

    private final MstBidangRepository bidangRepository;
    private final AuditService auditService;
    private final UserContext userContext;

    @Override
    @Transactional
    public BidangResponse create(BidangRequest request) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        String kode = request.getKodeBidang().toUpperCase().trim();

        if (bidangRepository.existsByKodeBidang(kode)) {
            throw new BadRequestException("Bidang dengan kode '" + kode + "' sudah ada");
        }

        MstBidang bidang = MstBidang.builder()
                .kodeBidang(kode)
                .namaBidang(request.getNamaBidang().trim())
                .build();

        MstBidang saved = bidangRepository.save(bidang);
        log.info("Bidang created: {} - {}", saved.getKodeBidang(), saved.getNamaBidang());

        BidangResponse response = mapToResponse(saved);
        
        // Audit log
        auditService.logCreate(ENTITY_NAME, saved.getId(), response, userId, username);
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BidangResponse getById(UUID id) {
        MstBidang bidang = bidangRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bidang tidak ditemukan"));
        return mapToResponse(bidang);
    }

    @Override
    @Transactional(readOnly = true)
    public BidangResponse getByKode(String kode) {
        MstBidang bidang = bidangRepository.findByKodeBidang(kode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Bidang dengan kode '" + kode + "' tidak ditemukan"));
        return mapToResponse(bidang);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidangResponse> getAll() {
        return bidangRepository.findAllByOrderByKodeBidangAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BidangResponse update(UUID id, BidangRequest request) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        MstBidang bidang = bidangRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bidang tidak ditemukan"));

        // Capture old value untuk audit
        BidangResponse oldValue = mapToResponse(bidang);

        String newKode = request.getKodeBidang().toUpperCase().trim();

        if (!bidang.getKodeBidang().equals(newKode) && bidangRepository.existsByKodeBidang(newKode)) {
            throw new BadRequestException("Bidang dengan kode '" + newKode + "' sudah ada");
        }

        bidang.setKodeBidang(newKode);
        bidang.setNamaBidang(request.getNamaBidang().trim());

        MstBidang updated = bidangRepository.save(bidang);
        log.info("Bidang updated: {} - {}", updated.getKodeBidang(), updated.getNamaBidang());

        BidangResponse newValue = mapToResponse(updated);
        
        // Audit log
        auditService.logUpdate(ENTITY_NAME, id, oldValue, newValue, userId, username);
        
        return newValue;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        MstBidang bidang = bidangRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bidang tidak ditemukan"));

        // Capture old value untuk audit sebelum delete
        BidangResponse oldValue = mapToResponse(bidang);

        bidangRepository.delete(bidang);
        log.info("Bidang deleted: {}", bidang.getKodeBidang());
        
        // Audit log
        auditService.logDelete(ENTITY_NAME, id, oldValue, userId, username);
    }

    private BidangResponse mapToResponse(MstBidang entity) {
        return BidangResponse.builder()
                .id(entity.getId())
                .kodeBidang(entity.getKodeBidang())
                .namaBidang(entity.getNamaBidang())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
