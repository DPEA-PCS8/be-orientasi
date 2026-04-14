package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.config.UserContext;
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
import com.pcs8.orientasi.repository.TeamRepository;
import com.pcs8.orientasi.service.PksiChangelogService;
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
    private final PksiChangelogService pksiChangelogService;
    private final TeamRepository teamRepository;
    private final UserContext userContext;

    @Override
    @Transactional
    public PksiDocumentResponse createDocument(PksiDocumentRequest request, UUID userId) {
        log.info("Creating PKSI document");

        // userId comes from JWT token via AuthorizationInterceptor (user_uuid attribute)
        // Authentication is enforced at controller level via @RequiresRole
        if (userId == null) {
            throw new BadRequestException("User context tidak tersedia. Silakan login ulang.");
        }
        MstUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User dengan UUID '" + userId + "' tidak ditemukan. Pastikan akun Anda terdaftar di sistem."));

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
    public PksiDocumentResponse updateDocument(UUID id, PksiDocumentRequest request, UUID userId) {
        log.info("Updating PKSI document");

        PksiDocument document = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        // Create a snapshot of the old values for change tracking
        PksiDocument oldSnapshot = createSnapshot(document);

        setAplikasiFromRequest(document, request);
        setInisiatifFromRequest(document, request, true);
        mapper.mapRequestToDocument(request, document);

        PksiDocument updated = pksiDocumentRepository.save(document);
        log.info("PKSI document updated successfully");

        // Track changes
        MstUser updatedBy = userId != null ? userRepository.findById(userId).orElse(null) : null;
        if (updatedBy != null) {
            pksiChangelogService.trackChanges(updated, oldSnapshot, updatedBy);
        }

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
    public PksiDocumentResponse updateStatus(UUID id, UpdateStatusRequest request, UUID userId) {
        log.info("Updating PKSI document status");

        PksiDocument document = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));

        // Create a snapshot of the old values for change tracking
        PksiDocument oldSnapshot = createSnapshot(document);

        PksiDocument.DocumentStatus newStatus = parseDocumentStatus(request.getStatus(), id);
        document.setStatus(newStatus);

        // Save approval fields if status is DISETUJUI (both for new approval and editing existing)
        if (newStatus == PksiDocument.DocumentStatus.DISETUJUI) {
            applyApprovalFields(document, request);
        }

        PksiDocument updated = pksiDocumentRepository.save(document);

        // Track changes
        MstUser updatedBy = userId != null ? userRepository.findById(userId).orElse(null) : null;
        if (updatedBy != null) {
            pksiChangelogService.trackChanges(updated, oldSnapshot, updatedBy);
        }

        log.info("PKSI document status updated successfully");
        return enrichWithSkpaNames(mapper.mapToResponse(initializeLazyRelations(updated)));
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

        // Create snapshot for change tracking BEFORE applying changes
        PksiDocument oldSnapshot = createSnapshot(document);

        applyApprovalFields(document, request);
        
        // Save changes
        PksiDocumentResponse response = saveAndRefresh(id);
        
        // Track changes - get updated document for comparison
        PksiDocument updated = pksiDocumentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException(PKSI_NOT_FOUND));
        
        // Get current logged-in user from request context
        UUID currentUserId = userContext.getCurrentUserId();
        MstUser updatedBy = currentUserId != null 
            ? userRepository.findById(currentUserId).orElse(document.getUser())
            : document.getUser();
        pksiChangelogService.trackChanges(updated, oldSnapshot, updatedBy);
        
        log.info("PKSI document approval fields updated successfully");
        return response;
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
        // Handle team_id - when a team is selected, automatically populate PIC and members
        if (request.getTeamId() != null && !request.getTeamId().isEmpty()) {
            try {
                UUID teamId = UUID.fromString(request.getTeamId());
                teamRepository.findByIdWithDetails(teamId).ifPresent(team -> {
                    document.setTeam(team);
                    // Auto-populate PIC from team
                    if (team.getPic() != null) {
                        document.setPicApproval(team.getPic().getUuid().toString());
                        document.setPicApprovalName(team.getPic().getFullName());
                    }
                    // Auto-populate team members
                    if (team.getTeamMembers() != null && !team.getTeamMembers().isEmpty()) {
                        String memberUuids = team.getTeamMembers().stream()
                                .map(tm -> tm.getUser().getUuid().toString())
                                .collect(Collectors.joining(", "));
                        String memberNames = team.getTeamMembers().stream()
                                .map(tm -> tm.getUser().getFullName())
                                .collect(Collectors.joining(", "));
                        document.setAnggotaTim(memberUuids);
                        document.setAnggotaTimNames(memberNames);
                    }
                });
            } catch (IllegalArgumentException e) {
                log.warn("Invalid team ID format provided");
            }
        }

        // Apply monitoring fields - Anggaran
        if (request.getAnggaranTotal() != null) {
            document.setAnggaranTotal(request.getAnggaranTotal());
        }
        if (request.getAnggaranTahunIni() != null) {
            document.setAnggaranTahunIni(request.getAnggaranTahunIni());
        }
        if (request.getAnggaranTahunDepan() != null) {
            document.setAnggaranTahunDepan(request.getAnggaranTahunDepan());
        }

        // Apply monitoring fields - Target Timeline
        if (request.getTargetUsreq() != null) {
            document.setTargetUsreq(request.getTargetUsreq());
        }
        if (request.getTargetSit() != null) {
            document.setTargetSit(request.getTargetSit());
        }
        if (request.getTargetUat() != null) {
            document.setTargetUat(request.getTargetUat());
        }
        if (request.getTargetGoLive() != null) {
            document.setTargetGoLive(request.getTargetGoLive());
        }

        // Apply monitoring fields - T01/T02 Status
        if (request.getStatusT01T02() != null) {
            document.setStatusT01T02(request.getStatusT01T02());
        }
        if (request.getBerkasT01T02() != null) {
            document.setBerkasT01T02(request.getBerkasT01T02());
        }

        // Apply monitoring fields - T11 Status
        if (request.getStatusT11() != null) {
            document.setStatusT11(request.getStatusT11());
        }
        if (request.getBerkasT11() != null) {
            document.setBerkasT11(request.getBerkasT11());
        }

        // Apply monitoring fields - CD Prinsip
        if (request.getStatusCd() != null) {
            document.setStatusCd(request.getStatusCd());
        }
        if (request.getNomorCd() != null) {
            document.setNomorCd(request.getNomorCd());
        }

        // Apply monitoring fields - Kontrak
        if (request.getKontrakTanggalMulai() != null) {
            document.setKontrakTanggalMulai(request.getKontrakTanggalMulai());
        }
        if (request.getKontrakTanggalSelesai() != null) {
            document.setKontrakTanggalSelesai(request.getKontrakTanggalSelesai());
        }
        if (request.getKontrakNilai() != null) {
            document.setKontrakNilai(request.getKontrakNilai());
        }
        if (request.getKontrakJumlahTermin() != null) {
            document.setKontrakJumlahTermin(request.getKontrakJumlahTermin());
        }
        if (request.getKontrakDetailPembayaran() != null) {
            document.setKontrakDetailPembayaran(request.getKontrakDetailPembayaran());
        }

        // Apply monitoring fields - BA Deploy
        if (request.getBaDeploy() != null) {
            document.setBaDeploy(request.getBaDeploy());
        }
    }

    /**
     * Create a snapshot of the document's current state for change tracking.
     * This captures the current values before any modifications are made.
     */
    private PksiDocument createSnapshot(PksiDocument document) {
        return PksiDocument.builder()
                .id(document.getId())
                .namaPksi(document.getNamaPksi())
                .tanggalPengajuan(document.getTanggalPengajuan())
                .deskripsiPksi(document.getDeskripsiPksi())
                .mengapaPksiDiperlukan(document.getMengapaPksiDiperlukan())
                .kapanDiselesaikan(document.getKapanDiselesaikan())
                .picSatker(document.getPicSatker())
                .kegunaanPksi(document.getKegunaanPksi())
                .tujuanPksi(document.getTujuanPksi())
                .targetPksi(document.getTargetPksi())
                .ruangLingkup(document.getRuangLingkup())
                .batasanPksi(document.getBatasanPksi())
                .hubunganSistemLain(document.getHubunganSistemLain())
                .asumsi(document.getAsumsi())
                .batasanDesain(document.getBatasanDesain())
                .risikoBisnis(document.getRisikoBisnis())
                .risikoSuksesPksi(document.getRisikoSuksesPksi())
                .pengendalianRisiko(document.getPengendalianRisiko())
                .pengelolaAplikasi(document.getPengelolaAplikasi())
                .penggunaAplikasi(document.getPenggunaAplikasi())
                .programInisiatifRbsi(document.getProgramInisiatifRbsi())
                .fungsiAplikasi(document.getFungsiAplikasi())
                .informasiYangDikelola(document.getInformasiYangDikelola())
                .dasarPeraturan(document.getDasarPeraturan())
                .tahap1Awal(document.getTahap1Awal())
                .tahap1Akhir(document.getTahap1Akhir())
                .tahap5Awal(document.getTahap5Awal())
                .tahap5Akhir(document.getTahap5Akhir())
                .tahap7Awal(document.getTahap7Awal())
                .tahap7Akhir(document.getTahap7Akhir())
                .rencanaPengelolaan(document.getRencanaPengelolaan())
                .status(document.getStatus())
                .iku(document.getIku())
                .inhouseOutsource(document.getInhouseOutsource())
                .picApproval(document.getPicApproval())
                .picApprovalName(document.getPicApprovalName())
                .anggotaTim(document.getAnggotaTim())
                .anggotaTimNames(document.getAnggotaTimNames())
                .progress(document.getProgress())
                // Monitoring fields - Anggaran
                .anggaranTotal(document.getAnggaranTotal())
                .anggaranTahunIni(document.getAnggaranTahunIni())
                .anggaranTahunDepan(document.getAnggaranTahunDepan())
                // Monitoring fields - Target Timeline
                .targetUsreq(document.getTargetUsreq())
                .targetSit(document.getTargetSit())
                .targetUat(document.getTargetUat())
                .targetGoLive(document.getTargetGoLive())
                // Monitoring fields - T01/T02 Status
                .statusT01T02(document.getStatusT01T02())
                .berkasT01T02(document.getBerkasT01T02())
                // Monitoring fields - T11 Status
                .statusT11(document.getStatusT11())
                .berkasT11(document.getBerkasT11())
                // Monitoring fields - CD Prinsip
                .statusCd(document.getStatusCd())
                .nomorCd(document.getNomorCd())
                // Monitoring fields - Kontrak
                .kontrakTanggalMulai(document.getKontrakTanggalMulai())
                .kontrakTanggalSelesai(document.getKontrakTanggalSelesai())
                .kontrakNilai(document.getKontrakNilai())
                .kontrakJumlahTermin(document.getKontrakJumlahTermin())
                .kontrakDetailPembayaran(document.getKontrakDetailPembayaran())
                // Monitoring fields - BA Deploy
                .baDeploy(document.getBaDeploy())
                .build();
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
        if (document.getTeam() != null) {
            Hibernate.initialize(document.getTeam());
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
