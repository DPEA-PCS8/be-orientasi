package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.PksiDocumentRequest;
import com.pcs8.orientasi.domain.dto.request.UpdateApprovalRequest;
import com.pcs8.orientasi.domain.dto.request.UpdateStatusRequest;
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
     * Search documents with year and noInisiatif filters
     */
    Page<PksiDocumentResponse> searchDocuments(String search, String status, Integer year, boolean noInisiatif, Pageable pageable, String userDepartment, boolean canSeeAll);
    
    /**
     * Count documents by status, optional year filter, and noInisiatif filter
     */
    long countDocuments(String status, Integer year, boolean noInisiatif);
    
    PksiDocumentResponse updateDocument(UUID id, PksiDocumentRequest request);
    
    PksiDocumentResponse updateStatus(UUID id, UpdateStatusRequest request);
    
    PksiDocumentResponse updateApprovalFields(UUID id, UpdateApprovalRequest request);
    
    void deleteDocument(UUID id);
}
