package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.PksiDocumentRequest;
import com.pcs8.orientasi.domain.dto.response.PksiDocumentResponse;
import com.pcs8.orientasi.domain.entity.MstUser;
import com.pcs8.orientasi.domain.entity.PksiDocument;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstUserRepository;
import com.pcs8.orientasi.repository.PksiDocumentRepository;
import com.pcs8.orientasi.service.PksiDocumentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation untuk dokumen T.01 (PKSI) - Refactored Version
 */
@Service
@RequiredArgsConstructor
public class PksiDocumentServiceImpl implements PksiDocumentService {

    private static final Logger log = LoggerFactory.getLogger(PksiDocumentServiceImpl.class);
    private static final String PKSI_NOT_FOUND = "PKSI document not found";

    private final PksiDocumentRepository pksiDocumentRepository;
    private final MstUserRepository userRepository;
    private final PksiDocumentMapper mapper;

    @Override
    @Transactional
    public PksiDocumentResponse createDocument(PksiDocumentRequest request, UUID userId) {
        log.info("Creating PKSI document for user: {}", userId);

// userId is optional - used for audit/tracking only
        // Authentication is enforced at controller level via @RequiresRole
        MstUser user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("User not found for tracking, proceeding without user association");
            }
        }

        PksiDocument document = PksiDocument.builder()
                .user(user)
                .status(PksiDocument.DocumentStatus.PENDING)
                .build();

        // Use mapper to set all fields from request
        mapper.mapRequestToDocument(request, document);

        PksiDocument saved = pksiDocumentRepository.save(document);
        log.info("PKSI document created with ID: {}", saved.getId());

        return mapper.mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PksiDocumentResponse getDocumentById(UUID id) {
        log.info("Fetching PKSI document: {}", id);
        
        PksiDocument document = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        return mapper.mapToResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PksiDocumentResponse> getAllDocuments() {
        log.info("Fetching all PKSI documents");
        
        return pksiDocumentRepository.findAllWithUser().stream()
                .map(mapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PksiDocumentResponse> getDocumentsByUser(UUID userId) {
        log.info("Fetching PKSI documents for user: {}", userId);
        
        return pksiDocumentRepository.findByUserUuid(userId).stream()
                .map(mapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PksiDocumentResponse> searchDocuments(String search, String status, Pageable pageable) {
        log.info("Searching PKSI documents");
        
        return pksiDocumentRepository.searchDocuments(search, status, pageable)
                .map(mapper::mapToResponse);
    }

    @Override
    @Transactional
    public PksiDocumentResponse updateDocument(UUID id, PksiDocumentRequest request) {
        log.info("Updating PKSI document: {}", id);

        PksiDocument document = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        // Use mapper to update all fields from request
        mapper.mapRequestToDocument(request, document);

        PksiDocument updated = pksiDocumentRepository.save(document);
        log.info("PKSI document updated: {}", updated.getId());

        return mapper.mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteDocument(UUID id) {
        log.info("Deleting PKSI document: {}", id);

        if (!pksiDocumentRepository.existsById(id)) {
            throw new ResourceNotFoundException(PKSI_NOT_FOUND);
        }

        pksiDocumentRepository.deleteById(id);
        log.info("PKSI document deleted: {}", id);
    }

    @Override
    @Transactional
    public PksiDocumentResponse updateStatus(UUID id, String status) {
        log.info("Updating status for PKSI document: {}", id);

        PksiDocument document = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        PksiDocument.DocumentStatus newStatus = parseDocumentStatus(status, id);
        document.setStatus(newStatus);

        pksiDocumentRepository.save(document);
        log.info("PKSI document status updated: {} -> {}", id, newStatus);

        // Re-fetch with user to avoid lazy loading issues
        PksiDocument updated = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        return mapper.mapToResponse(updated);
    }

    private PksiDocument.DocumentStatus parseDocumentStatus(String status, UUID documentId) {
        try {
            return PksiDocument.DocumentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status value provided for document: {}", documentId);
            String validStatuses = Arrays.toString(PksiDocument.DocumentStatus.values());
            throw new BadRequestException("Invalid status value. Valid values are: " + validStatuses);
        }
    }
}
