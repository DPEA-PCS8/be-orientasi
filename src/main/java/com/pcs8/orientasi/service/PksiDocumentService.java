package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.PksiDocumentRequest;
import com.pcs8.orientasi.domain.dto.request.UpdateApprovalRequest;
import com.pcs8.orientasi.domain.dto.request.UpdateStatusRequest;
import com.pcs8.orientasi.domain.dto.response.ParentPksiSummary;
import com.pcs8.orientasi.domain.dto.response.PksiDocumentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PksiDocumentService {
    
    PksiDocumentResponse createDocument(PksiDocumentRequest request, UUID userId);
    
    PksiDocumentResponse getDocumentById(UUID id);
    
    List<PksiDocumentResponse> getAllDocuments();
    
    List<PksiDocumentResponse> getDocumentsByUser(UUID userId);
    
    Page<PksiDocumentResponse> searchDocuments(String search, String status, Pageable pageable);
    
    Page<PksiDocumentResponse> searchDocuments(String search, String status, Pageable pageable, String userDepartment, boolean canSeeAll);
    
    /**
     * Search documents with year and noInisiatif filters.
     * Returns ORIGINAL data as submitted (for "Semua PKSI" page).
     */
    Page<PksiDocumentResponse> searchDocuments(String search, String status, Integer year, boolean noInisiatif, Pageable pageable, String userDepartment, boolean canSeeAll);
    
    /**
     * Search documents for monitoring with year, noInisiatif, and timeline filters.
     * Returns data with EFFECTIVE methods for nested PKSI (for "Monitoring PKSI" page).
     * Nested PKSI will show parent's monitoring data.
     * 
     * @param timelineStage filter by timeline stage (USREQ, SIT, UAT, etc.)
     * @param timelineFromMonth filter by month range start (1-12)
     * @param timelineToMonth filter by month range end (1-12)
     * @param timelineYear filter by specific year for timeline date
     */
    Page<PksiDocumentResponse> searchDocumentsForMonitoring(String search, String status, Integer year, boolean noInisiatif, 
            String timelineStage, Integer timelineFromMonth, Integer timelineToMonth, Integer timelineYear,
            Pageable pageable, String userDepartment, boolean canSeeAll);
    
    /**
     * Count documents by status, optional year filter, noInisiatif filter, and timeline filters
     */
    long countDocuments(String status, Integer year, boolean noInisiatif, 
            String timelineStage, Integer timelineFromMonth, Integer timelineToMonth, Integer timelineYear);
    
    PksiDocumentResponse updateDocument(UUID id, PksiDocumentRequest request, UUID userId);
    
    PksiDocumentResponse updateStatus(UUID id, UpdateStatusRequest request, UUID userId);
    
    PksiDocumentResponse updateApprovalFields(UUID id, UpdateApprovalRequest request);
    
    void deleteDocument(UUID id);

    // ==================== NESTED PKSI METHODS ====================

    /**
     * Get available parent PKSI candidates for nesting (optimized).
     * Returns only id, nama_pksi, and nama_aplikasi for dropdown performance.
     * Filtering is done on frontend for better UX.
     * 
     * @param year the year to filter by (based on timeline target_date), can be null
     * @param excludeId the PKSI id to exclude (the document being edited), can be null
     * @return list of available parent PKSI summaries
     */
    List<ParentPksiSummary> getAvailableParentPksi(Integer year, UUID excludeId);

    /**
     * Get child PKSI documents for a given parent PKSI.
     *
     * @param parentId the parent PKSI id
     * @return list of child PKSI documents
     */
    List<PksiDocumentResponse> getChildPksi(UUID parentId);
}
