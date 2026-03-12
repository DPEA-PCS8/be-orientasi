package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.PksiDocumentRequest;
import com.pcs8.orientasi.domain.dto.request.UpdateApprovalRequest;
import com.pcs8.orientasi.domain.dto.request.UpdateStatusRequest;
import com.pcs8.orientasi.domain.dto.response.PksiDocumentResponse;
import com.pcs8.orientasi.domain.entity.MstUser;
import com.pcs8.orientasi.domain.entity.PksiDocument;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstAplikasiRepository;
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
    private final MstAplikasiRepository aplikasiRepository;

    @Override
    @Transactional
    public PksiDocumentResponse createDocument(PksiDocumentRequest request, UUID userId) {
        log.info("Creating PKSI document");

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

        // Set aplikasi if provided
        if (request.getAplikasiId() != null && !request.getAplikasiId().isEmpty()) {
            try {
                UUID aplikasiId = UUID.fromString(request.getAplikasiId());
                aplikasiRepository.findById(aplikasiId).ifPresent(document::setAplikasi);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid aplikasi ID format: {}", request.getAplikasiId());
            }
        }

        // Use mapper to set all fields from request
        mapper.mapRequestToDocument(request, document);

        PksiDocument saved = pksiDocumentRepository.save(document);
        log.info("PKSI document created successfully");

        return mapper.mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PksiDocumentResponse getDocumentById(UUID id) {
        log.info("Fetching PKSI document by ID");
        
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
        log.info("Fetching PKSI documents for user");
        
        return pksiDocumentRepository.findByUserUuid(userId).stream()
                .map(mapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PksiDocumentResponse> searchDocuments(String search, String status, Pageable pageable) {
        log.info("Searching PKSI documents");
        
        // Sanitize and format search input with wildcards
        String searchPattern = formatSearchPattern(search);
        String sanitizedStatus = sanitizeSearchInput(status);
        
        return pksiDocumentRepository.searchDocuments(searchPattern, sanitizedStatus, pageable)
                .map(mapper::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PksiDocumentResponse> searchDocuments(String search, String status, Pageable pageable, String userDepartment, boolean canSeeAll) {
        log.info("Searching PKSI documents - canSeeAll: {}, userDepartment: {}", canSeeAll, userDepartment);
        
        // Sanitize and format search input with wildcards
        String searchPattern = formatSearchPattern(search);
        String sanitizedStatus = sanitizeSearchInput(status);
        
        // Admin/Pengembang can see all documents
        if (canSeeAll) {
            log.info("User can see all - fetching all documents");
            return pksiDocumentRepository.searchDocuments(searchPattern, sanitizedStatus, pageable)
                    .map(mapper::mapToResponse);
        }
        
        // SKPA users only see documents where SKPA kode matches their department
        log.info("User is SKPA - filtering by department: {}", userDepartment);
        return pksiDocumentRepository.searchDocumentsByDepartment(searchPattern, sanitizedStatus, userDepartment, pageable)
                .map(mapper::mapToResponse);
    }
    
    /**
     * Format search input as LIKE pattern with wildcards.
     * Sanitizes input and adds % wildcards for partial matching.
     */
    private String formatSearchPattern(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        // Remove potentially dangerous characters and format as LIKE pattern
        String sanitized = input.replaceAll("[<>\"'%;()&+\\\\]", "").trim().toLowerCase();
        if (sanitized.isEmpty()) {
            return null;
        }
        return "%" + sanitized + "%";
    }
    
    /**
     * Sanitize search input to prevent SQL/JPQL injection
     */
    private String sanitizeSearchInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        // Remove potentially dangerous characters
        return input.replaceAll("[<>\"'%;()&+\\\\]", "").trim();
    }

    @Override
    @Transactional
    public PksiDocumentResponse updateDocument(UUID id, PksiDocumentRequest request) {
        log.info("Updating PKSI document");

        PksiDocument document = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        // Update aplikasi if provided
        if (request.getAplikasiId() != null && !request.getAplikasiId().isEmpty()) {
            try {
                UUID aplikasiId = UUID.fromString(request.getAplikasiId());
                aplikasiRepository.findById(aplikasiId).ifPresent(document::setAplikasi);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid aplikasi ID format: {}", request.getAplikasiId());
            }
        }

        // Use mapper to update all fields from request
        mapper.mapRequestToDocument(request, document);

        PksiDocument updated = pksiDocumentRepository.save(document);
        log.info("PKSI document updated successfully");

        return mapper.mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteDocument(UUID id) {
        log.info("Deleting PKSI document");

        if (!pksiDocumentRepository.existsById(id)) {
            throw new ResourceNotFoundException(PKSI_NOT_FOUND);
        }

        pksiDocumentRepository.deleteById(id);
        log.info("PKSI document deleted successfully");
    }

    @Override
    @Transactional
    public PksiDocumentResponse updateStatus(UUID id, UpdateStatusRequest request) {
        log.info("Updating PKSI document status");

        PksiDocument document = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        PksiDocument.DocumentStatus newStatus = parseDocumentStatus(request.getStatus(), id);
        document.setStatus(newStatus);

        // Save approval fields if status is DISETUJUI (both for new approval and editing existing)
        if (newStatus == PksiDocument.DocumentStatus.DISETUJUI) {
            // Always update approval fields when provided, regardless of previous status
            if (request.getIku() != null) {
                document.setIku(request.getIku());
            }
            if (request.getInhouseOutsource() != null) {
                document.setInhouseOutsource(request.getInhouseOutsource());
            }
            if (request.getPicApproval() != null) {
                document.setPicApproval(request.getPicApproval());
            }
            if (request.getPicApprovalName() != null) {
                document.setPicApprovalName(request.getPicApprovalName());
            }
            if (request.getAnggotaTim() != null) {
                document.setAnggotaTim(request.getAnggotaTim());
            }
            if (request.getAnggotaTimNames() != null) {
                document.setAnggotaTimNames(request.getAnggotaTimNames());
            }
            if (request.getProgress() != null) {
                document.setProgress(request.getProgress());
            }
        }

        pksiDocumentRepository.save(document);
        log.info("PKSI document status updated successfully");

        // Re-fetch with user to avoid lazy loading issues
        PksiDocument updated = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        return mapper.mapToResponse(updated);
    }

    private PksiDocument.DocumentStatus parseDocumentStatus(String status, UUID documentId) {
        try {
            return PksiDocument.DocumentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status value provided");
            String validStatuses = Arrays.toString(PksiDocument.DocumentStatus.values());
            throw new BadRequestException("Invalid status value. Valid values are: " + validStatuses);
        }
    }

    @Override
    @Transactional
    public PksiDocumentResponse updateApprovalFields(UUID id, UpdateApprovalRequest request) {
        log.info("Updating PKSI document approval fields");

        PksiDocument document = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        // Only allow updating approval fields for approved documents
        if (document.getStatus() != PksiDocument.DocumentStatus.DISETUJUI) {
            throw new BadRequestException("Cannot update approval fields for non-approved documents");
        }

        // Update approval fields when provided
        if (request.getIku() != null) {
            document.setIku(request.getIku());
        }
        if (request.getInhouseOutsource() != null) {
            document.setInhouseOutsource(request.getInhouseOutsource());
        }
        if (request.getPicApproval() != null) {
            document.setPicApproval(request.getPicApproval());
        }
        if (request.getPicApprovalName() != null) {
            document.setPicApprovalName(request.getPicApprovalName());
        }
        if (request.getAnggotaTim() != null) {
            document.setAnggotaTim(request.getAnggotaTim());
        }
        if (request.getAnggotaTimNames() != null) {
            document.setAnggotaTimNames(request.getAnggotaTimNames());
        }
        if (request.getProgress() != null) {
            document.setProgress(request.getProgress());
        }

        pksiDocumentRepository.save(document);
        log.info("PKSI document approval fields updated successfully");

        // Re-fetch with user to avoid lazy loading issues
        PksiDocument updated = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        return mapper.mapToResponse(updated);
    }
}
