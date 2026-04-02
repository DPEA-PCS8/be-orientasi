package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.ApprovalFields;
import com.pcs8.orientasi.domain.dto.request.PksiDocumentRequest;
import com.pcs8.orientasi.domain.dto.request.UpdateApprovalRequest;
import com.pcs8.orientasi.domain.dto.request.UpdateStatusRequest;
import com.pcs8.orientasi.domain.dto.response.PksiDocumentResponse;
import com.pcs8.orientasi.domain.entity.MstSkpa;
import com.pcs8.orientasi.domain.entity.MstUser;
import com.pcs8.orientasi.domain.entity.PksiDocument;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstAplikasiRepository;
import com.pcs8.orientasi.repository.RbsiInisiatifRepository;
import com.pcs8.orientasi.repository.MstSkpaRepository;
import com.pcs8.orientasi.repository.MstUserRepository;
import com.pcs8.orientasi.repository.PksiDocumentRepository;
import com.pcs8.orientasi.service.PksiDocumentService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
    private final MstSkpaRepository skpaRepository;
    private final RbsiInisiatifRepository rbsiInisiatifRepository;

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

        setAplikasiFromRequest(document, request);
        setInisiatifFromRequest(document, request, false);
        mapper.mapRequestToDocument(request, document);

        PksiDocument saved = pksiDocumentRepository.save(document);
        log.info("PKSI document created successfully");

        return enrichWithSkpaNames(mapper.mapToResponse(initializeLazyRelations(saved)));
    }

    @Override
    @Transactional(readOnly = true)
    public PksiDocumentResponse getDocumentById(UUID id) {
        log.info("Fetching PKSI document by ID");
        
        PksiDocument document = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        return enrichWithSkpaNames(mapper.mapToResponse(initializeLazyRelations(document)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PksiDocumentResponse> getAllDocuments() {
        log.info("Fetching all PKSI documents");
        
        return pksiDocumentRepository.findAllWithUser().stream()
                .map(this::initializeLazyRelations)
                .map(mapper::mapToResponse)
                .map(this::enrichWithSkpaNames)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PksiDocumentResponse> getDocumentsByUser(UUID userId) {
        log.info("Fetching PKSI documents for user");
        
        return pksiDocumentRepository.findByUserUuid(userId).stream()
                .map(this::initializeLazyRelations)
                .map(mapper::mapToResponse)
                .map(this::enrichWithSkpaNames)
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
                .map(this::initializeLazyRelations)
                .map(mapper::mapToResponse)
                .map(this::enrichWithSkpaNames);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PksiDocumentResponse> searchDocuments(String search, String status, Pageable pageable, String userDepartment, boolean canSeeAll) {
        log.info("Searching PKSI documents - canSeeAll: {}, userDepartment: '{}'", canSeeAll, userDepartment);
        
        // Sanitize and format search input with wildcards
        String searchPattern = formatSearchPattern(search);
        String sanitizedStatus = sanitizeSearchInput(status);
        
        // Admin/Pengembang can see all documents
        if (canSeeAll) {
            log.info("User can see all - fetching all documents");
            return pksiDocumentRepository.searchDocuments(searchPattern, sanitizedStatus, pageable)
                    .map(this::initializeLazyRelations)
                    .map(mapper::mapToResponse)
                    .map(this::enrichWithSkpaNames);
        }
        
        // SKPA users: if department is empty, return empty result (security)
        if (userDepartment == null || userDepartment.trim().isEmpty()) {
            log.warn("SKPA user has no department set - returning empty result for security");
            return Page.empty(pageable);
        }
        
        // SKPA users only see documents where SKPA kode matches their department
        log.info("User is SKPA - filtering by department: '{}'", userDepartment);
        
        // Debug: Find SKPA UUID for the user's department
        Optional<MstSkpa> userSkpa = skpaRepository.findByKodeSkpa(userDepartment.trim().toUpperCase());
        if (userSkpa.isPresent()) {
            log.info("Found SKPA for department '{}': UUID = {}", userDepartment, userSkpa.get().getId());
        } else {
            log.warn("No SKPA found for department '{}' - user will see no PKSI", userDepartment);
        }
        
        Page<PksiDocumentResponse> result = pksiDocumentRepository.searchDocumentsByDepartment(searchPattern, sanitizedStatus, userDepartment.trim(), pageable)
                .map(this::initializeLazyRelations)
                .map(mapper::mapToResponse)
                .map(this::enrichWithSkpaNames);
        
        log.info("Search result: {} documents found for department '{}'", result.getTotalElements(), userDepartment);
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PksiDocumentResponse> searchDocuments(String search, String status, Integer year, boolean noInisiatif, Pageable pageable, String userDepartment, boolean canSeeAll) {

        
        // Sanitize and format search input with wildcards
        String searchPattern = formatSearchPattern(search);
        String sanitizedStatus = sanitizeSearchInput(status);
        
        // Admin/Pengembang can see all documents
        if (canSeeAll) {
            log.info("User can see all - fetching all documents with filters");
            return pksiDocumentRepository.searchDocumentsWithFilters(searchPattern, sanitizedStatus, year, noInisiatif, pageable)
                    .map(mapper::mapToResponse)
                    .map(this::enrichWithSkpaNames);
        }
        
        // SKPA users: if department is empty, return empty result (security)
        if (userDepartment == null || userDepartment.trim().isEmpty()) {
            log.warn("SKPA user has no department set - returning empty result for security");
            return Page.empty(pageable);
        }
        
        // SKPA users only see documents where SKPA kode matches their department
        log.info("User is SKPA - filtering by department with year: {} and noInisiatif: {}", year, noInisiatif);
        
        Page<PksiDocumentResponse> result = pksiDocumentRepository.searchDocumentsByDepartmentWithFilters(
                searchPattern, sanitizedStatus, year, noInisiatif, userDepartment.trim(), pageable)
                .map(mapper::mapToResponse)
                .map(this::enrichWithSkpaNames);
        
        log.info("Search result with filters: {} documents found", result.getTotalElements());
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public long countDocuments(String status, Integer year, boolean noInisiatif) {
        String sanitizedStatus = sanitizeSearchInput(status);
        return pksiDocumentRepository.countByStatusYearAndNoInisiatif(sanitizedStatus, year, noInisiatif);
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

        setAplikasiFromRequest(document, request);
        setInisiatifFromRequest(document, request, true);
        mapper.mapRequestToDocument(request, document);

        PksiDocument updated = pksiDocumentRepository.save(document);
        log.info("PKSI document updated successfully");

        return enrichWithSkpaNames(mapper.mapToResponse(initializeLazyRelations(updated)));
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
            applyApprovalFields(document, request);
        }

        log.info("PKSI document status updated successfully");
        return saveAndRefresh(id);
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

        applyApprovalFields(document, request);
        log.info("PKSI document approval fields updated successfully");
        return saveAndRefresh(id);
    }

    /**
     * Set aplikasi from request if provided.
     */
    private void setAplikasiFromRequest(PksiDocument document, PksiDocumentRequest request) {
        if (request.getAplikasiId() != null && !request.getAplikasiId().isEmpty()) {
            try {
                UUID aplikasiId = UUID.fromString(request.getAplikasiId());
                aplikasiRepository.findById(aplikasiId).ifPresent(document::setAplikasi);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid aplikasi ID format: {}", request.getAplikasiId());
            }
        }
    }

    /**
     * Set inisiatif from request if provided (and derive group from it).
     * @param allowClear if true, allows clearing inisiatif when empty string is provided
     */
    private void setInisiatifFromRequest(PksiDocument document, PksiDocumentRequest request, boolean allowClear) {
        if (request.getInisiatifId() != null && !request.getInisiatifId().isEmpty()) {
            try {
                UUID inisiatifId = UUID.fromString(request.getInisiatifId());
                rbsiInisiatifRepository.findById(inisiatifId).ifPresent(inisiatif -> {
                    document.setInisiatif(inisiatif);
                    // Also set the group for dashboard/analytics purposes
                    if (inisiatif.getGroup() != null) {
                        document.setInisiatifGroup(inisiatif.getGroup());
                    }
                });
            } catch (IllegalArgumentException e) {
                log.warn("Invalid inisiatif ID format: {}", request.getInisiatifId());
            }
        } else if (allowClear && request.getInisiatifId() != null && request.getInisiatifId().isEmpty()) {
            // Clear the inisiatif if explicitly set to empty string
            document.setInisiatif(null);
            document.setInisiatifGroup(null);
        }
    }

    /**
     * Apply approval fields from request to document.
     * Works with any request that extends ApprovalFields.
     */
    private void applyApprovalFields(PksiDocument document, ApprovalFields request) {
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

    /**
     * Save document and re-fetch with initialized lazy relations.
     * Common pattern used after update operations.
     */
    private PksiDocumentResponse saveAndRefresh(UUID id) {
        PksiDocument document = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));
        pksiDocumentRepository.save(document);
        // Re-fetch to ensure all lazy relations are loaded
        PksiDocument updated = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));
        return enrichWithSkpaNames(mapper.mapToResponse(initializeLazyRelations(updated)));
    }

    /**
     * Initialize lazy-loaded relationships to avoid LazyInitializationException.
     * Forces Hibernate to load inisiatif and inisiatifGroup before the entity is detached.
     */
    private PksiDocument initializeLazyRelations(PksiDocument document) {
        if (document.getInisiatif() != null) {
            Hibernate.initialize(document.getInisiatif());
            if (document.getInisiatif().getGroup() != null) {
                Hibernate.initialize(document.getInisiatif().getGroup());
            }
        }
        if (document.getInisiatifGroup() != null) {
            Hibernate.initialize(document.getInisiatifGroup());
        }
        return document;
    }

    /**
     * Resolve SKPA GUIDs in picSatker to their kode_skpa names.
     * picSatker contains comma-separated UUIDs like "uuid1, uuid2"
     * This method looks up each UUID and returns comma-separated kode_skpa values.
     */
    private PksiDocumentResponse enrichWithSkpaNames(PksiDocumentResponse response) {
        String picSatker = response.getPicSatkerBA();
        if (picSatker == null || picSatker.trim().isEmpty()) {
            response.setPicSatkerNames(null);
            return response;
        }

        String[] guids = picSatker.split(",");
        String resolvedNames = Arrays.stream(guids)
                .map(String::trim)
                .filter(guid -> !guid.isEmpty())
                .map(guid -> {
                    try {
                        UUID uuid = UUID.fromString(guid);
                        Optional<MstSkpa> skpa = skpaRepository.findById(uuid);
                        return skpa.map(MstSkpa::getKodeSkpa).orElse(null);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(name -> name != null)
                .collect(Collectors.joining(", "));

        response.setPicSatkerNames(resolvedNames.isEmpty() ? null : resolvedNames);
        return response;
    }
}
