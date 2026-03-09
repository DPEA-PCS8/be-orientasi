package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.PksiDocumentRequest;
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
    
    PksiDocumentResponse updateDocument(UUID id, PksiDocumentRequest request);
    
    PksiDocumentResponse updateStatus(UUID id, String status);
    
    void deleteDocument(UUID id);
}
