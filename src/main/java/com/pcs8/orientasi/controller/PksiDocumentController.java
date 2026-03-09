package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.domain.dto.request.PksiDocumentRequest;
import com.pcs8.orientasi.domain.dto.request.UpdateStatusRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.PksiDocumentResponse;
import com.pcs8.orientasi.service.PksiDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.util.UUID;

@RestController
@RequestMapping("/pksi")
@RequiredArgsConstructor
@RequiresRole({"Admin", "Pengembang", "Satker"})
public class PksiDocumentController {

    private final PksiDocumentService pksiDocumentService;

    @PostMapping
    public ResponseEntity<BaseResponse> createDocument(
            @Valid @RequestBody PksiDocumentRequest request) {
        
        // Get user ID from request body (sent from frontend)
        UUID userId = UUID.fromString(request.getUserId());
        
        PksiDocumentResponse response = pksiDocumentService.createDocument(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "PKSI document created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getDocument(@PathVariable UUID id) {
        PksiDocumentResponse response = pksiDocumentService.getDocumentById(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse> getAllDocuments() {
        List<PksiDocumentResponse> responses = pksiDocumentService.getAllDocuments();
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", responses));
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponse> searchDocuments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PksiDocumentResponse> pageResult = pksiDocumentService.searchDocuments(search, status, pageable);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("content", pageResult.getContent());
        responseData.put("total_elements", pageResult.getTotalElements());
        responseData.put("total_pages", pageResult.getTotalPages());
        responseData.put("page", pageResult.getNumber());
        responseData.put("size", pageResult.getSize());
        responseData.put("has_next", pageResult.hasNext());
        responseData.put("has_previous", pageResult.hasPrevious());
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", responseData));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<BaseResponse> getDocumentsByUser(@PathVariable UUID userId) {
        List<PksiDocumentResponse> responses = pksiDocumentService.getDocumentsByUser(userId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> updateDocument(
            @PathVariable UUID id,
            @Valid @RequestBody PksiDocumentRequest request) {
        
        PksiDocumentResponse response = pksiDocumentService.updateDocument(id, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "PKSI document updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BaseResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        
        PksiDocumentResponse response = pksiDocumentService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "PKSI document status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> deleteDocument(@PathVariable UUID id) {
        pksiDocumentService.deleteDocument(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "PKSI document deleted successfully", null));
    }
}
