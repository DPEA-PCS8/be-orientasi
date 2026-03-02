package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.domain.dto.request.PksiDocumentRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.PksiDocumentResponse;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.service.PksiDocumentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pksi")
@RequiredArgsConstructor
public class PksiDocumentController {

    private final PksiDocumentService pksiDocumentService;

    @PostMapping
    public ResponseEntity<BaseResponse> createDocument(
            @Valid @RequestBody PksiDocumentRequest request,
            HttpServletRequest httpServletRequest) {
        
        // Get user UUID from authentication context
        Object userUuid = httpServletRequest.getAttribute("user_uuid");
        if (userUuid == null) {
            throw new BadRequestException("User UUID not found in request context");
        }
        UUID userId = UUID.fromString(userUuid.toString());
        
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

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> deleteDocument(@PathVariable UUID id) {
        pksiDocumentService.deleteDocument(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "PKSI document deleted successfully", null));
    }
}
