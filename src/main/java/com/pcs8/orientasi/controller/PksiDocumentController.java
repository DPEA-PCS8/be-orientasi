package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.domain.dto.request.PksiDashboardRequest;
import com.pcs8.orientasi.domain.dto.request.PksiDocumentRequest;
import com.pcs8.orientasi.domain.dto.request.UpdateApprovalRequest;
import com.pcs8.orientasi.domain.dto.request.UpdateStatusRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.PksiChangelogResponse;
import com.pcs8.orientasi.domain.dto.response.PksiDashboardResponse;
import com.pcs8.orientasi.domain.dto.response.PksiDocumentResponse;
import com.pcs8.orientasi.service.PksiChangelogService;
import com.pcs8.orientasi.service.PksiDashboardService;
import com.pcs8.orientasi.service.PksiDocumentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/pksi")
@RequiredArgsConstructor
@RequiresRole({"Admin", "Pengembang", "SKPA"})
public class PksiDocumentController {

    private static final Logger log = LoggerFactory.getLogger(PksiDocumentController.class);
    private static final String SUCCESS_MESSAGE = "Success";
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "updatedAt", "namaPksi", "status", "tanggalPengajuan"
    );
    private static final String DEFAULT_SORT_FIELD = "createdAt";
    
    private final PksiDocumentService pksiDocumentService;
    private final PksiChangelogService pksiChangelogService;
    private final PksiDashboardService pksiDashboardService;

    /**
     * Create a new PKSI document.
     * 
     * Security Note: Authentication and authorization are enforced by @RequiresRole annotation
     * at class level, which validates JWT token and user roles via AuthorizationInterceptor.
     * The userId extraction is optional and used only for audit/tracking purposes.
     */
    @PostMapping
    public ResponseEntity<BaseResponse> createDocument(
            @Valid @RequestBody PksiDocumentRequest request,
            HttpServletRequest httpRequest) {
        
// User is already authenticated via @RequiresRole - extract userId for tracking (optional)
        UUID userId = extractUserIdFromRequest(httpRequest);
        if (userId == null) {
            log.info("Creating document without user tracking - user authenticated via role-based auth");
        }
        
        PksiDocumentResponse response = pksiDocumentService.createDocument(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "PKSI document created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getDocument(@PathVariable UUID id) {
        PksiDocumentResponse response = pksiDocumentService.getDocumentById(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse> getAllDocuments() {
        List<PksiDocumentResponse> responses = pksiDocumentService.getAllDocuments();
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, responses));
    }

    @GetMapping("/search")
    @SuppressWarnings("java:S1192") // defaultValue in annotation must be literal, cannot use constant
    public ResponseEntity<BaseResponse> searchDocuments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false, defaultValue = "false") boolean noInisiatif,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy, // NOSONAR - must match DEFAULT_SORT_FIELD
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest
    ) {
        // Validate sortBy to prevent injection - use whitelist approach
        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : DEFAULT_SORT_FIELD;
        
        Sort sort = "desc".equalsIgnoreCase(sortDir) 
                ? Sort.by(safeSortBy).descending() 
                : Sort.by(safeSortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Extract user info from request attributes (set by AuthorizationInterceptor)
        @SuppressWarnings("unchecked")
        Set<String> userRoles = (Set<String>) httpRequest.getAttribute("user_roles");
        String userDepartment = (String) httpRequest.getAttribute("department");
        
        
        // Admin and Pengembang can see all PKSI, SKPA role only sees matching department
        boolean canSeeAll = userRoles != null && userRoles.stream()
                .anyMatch(role -> "admin".equalsIgnoreCase(role) || "pengembang".equalsIgnoreCase(role));
        
        log.info("PKSI Search - canSeeAll: {}", canSeeAll);
        
        // Use new method with year and noInisiatif filters
        Page<PksiDocumentResponse> pageResult = pksiDocumentService.searchDocuments(
                search, status, year, noInisiatif, pageable, userDepartment, canSeeAll);
        
        // Get total count for the specified status, year, and noInisiatif filter
        long totalCount = pksiDocumentService.countDocuments(status, year, noInisiatif);
        
        log.info("PKSI Search - Results count: {}, Total count: {}", pageResult.getTotalElements(), totalCount);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("content", pageResult.getContent());
        responseData.put("total_elements", pageResult.getTotalElements());
        responseData.put("total_pages", pageResult.getTotalPages());
        responseData.put("page", pageResult.getNumber());
        responseData.put("size", pageResult.getSize());
        responseData.put("has_next", pageResult.hasNext());
        responseData.put("has_previous", pageResult.hasPrevious());
        responseData.put("total_count", totalCount); // Total count filtered by year (if provided)
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, responseData));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<BaseResponse> getDocumentsByUser(@PathVariable UUID userId) {
        List<PksiDocumentResponse> responses = pksiDocumentService.getDocumentsByUser(userId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> updateDocument(
            @PathVariable UUID id,
            @Valid @RequestBody PksiDocumentRequest request,
            HttpServletRequest httpRequest) {
        
        UUID userId = extractUserIdFromRequest(httpRequest);
        PksiDocumentResponse response = pksiDocumentService.updateDocument(id, request, userId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "PKSI document updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BaseResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request,
            HttpServletRequest httpRequest) {
        
        UUID userId = extractUserIdFromRequest(httpRequest);
        PksiDocumentResponse response = pksiDocumentService.updateStatus(id, request, userId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "PKSI document status updated successfully", response));
    }

    @PatchMapping("/{id}/approval")
    public ResponseEntity<BaseResponse> updateApprovalFields(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateApprovalRequest request) {
        
        PksiDocumentResponse response = pksiDocumentService.updateApprovalFields(id, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "PKSI document approval fields updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> deleteDocument(@PathVariable UUID id) {
        pksiDocumentService.deleteDocument(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "PKSI document deleted successfully", null));
    }

    /**
     * Get changelogs for a PKSI document
     */
    @GetMapping("/{pksiId}/changelogs")
    public ResponseEntity<BaseResponse> getChangelogs(@PathVariable UUID pksiId) {
        List<PksiChangelogResponse> changelogs = pksiChangelogService.getChangelogsByPksiId(pksiId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, changelogs));
    }

    /**
     * Get changelog count for a PKSI document
     */
    @GetMapping("/{pksiId}/changelogs/count")
    public ResponseEntity<BaseResponse> getChangelogCount(@PathVariable UUID pksiId) {
        long count = pksiChangelogService.countChangelogsByPksiId(pksiId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, count));
    }

    /**
     * Get PKSI Dashboard data with analytics and insights
     */
    @GetMapping("/dashboard")
    public ResponseEntity<BaseResponse> getDashboardData(
            @RequestParam(required = false) Integer tahun,
            @RequestParam(required = false) Integer bulan,
            @RequestParam(required = false) String status) {
        
        PksiDashboardRequest request = PksiDashboardRequest.builder()
                .tahun(tahun)
                .bulan(bulan)
                .status(status)
                .build();
        
        PksiDashboardResponse response = pksiDashboardService.getDashboardData(request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, response));
    }

    private UUID extractUserIdFromRequest(HttpServletRequest httpRequest) {
        String userUuidStr = (String) httpRequest.getAttribute("user_uuid");
        if (userUuidStr == null || userUuidStr.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(userUuidStr);
        } catch (IllegalArgumentException e) {
            // Sanitize log output to prevent log injection
            log.warn("Invalid UUID format received");
            return null;
        }
    }
}
