package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.request.RbsiRequest;
import com.pcs8.orientasi.domain.dto.response.RbsiResponse;
import com.pcs8.orientasi.service.RbsiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rbsi")
@RequiredArgsConstructor
public class RbsiController {

    private static final Logger log = LoggerFactory.getLogger(RbsiController.class);

    private final RbsiService rbsiService;

    @PostMapping
    public ResponseEntity<BaseResponse> create(@Valid @RequestBody RbsiRequest request) {
        log.info("POST /rbsi - Creating new RBSI");
        try {
            RbsiResponse response = rbsiService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.CREATED.value())
                            .message("RBSI created successfully")
                            .data(response)
                            .build());
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create RBSI: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error creating RBSI: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to create RBSI")
                            .build());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<BaseResponse> findAll() {
        log.info("GET /rbsi/list - Fetching all RBSI");
        try {
            List<RbsiResponse> responses = rbsiService.findAll();
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("RBSI list fetched successfully")
                    .data(responses)
                    .build());
        } catch (Exception e) {
            log.error("Error fetching RBSI list: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to fetch RBSI list")
                            .build());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> findById(@PathVariable UUID id) {
        log.info("GET /rbsi/{} - Fetching RBSI by id", id);
        try {
            RbsiResponse response = rbsiService.findById(id);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("RBSI fetched successfully")
                    .data(response)
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("RBSI not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error fetching RBSI: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to fetch RBSI")
                            .build());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> update(@PathVariable UUID id, 
                                               @Valid @RequestBody RbsiRequest request) {
        log.info("PUT /rbsi/{} - Updating RBSI", id);
        try {
            RbsiResponse response = rbsiService.update(id, request);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("RBSI updated successfully")
                    .data(response)
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update RBSI: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error updating RBSI: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to update RBSI")
                            .build());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> delete(@PathVariable UUID id) {
        log.info("DELETE /rbsi/{} - Deleting RBSI", id);
        try {
            rbsiService.delete(id);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("RBSI deleted successfully")
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete RBSI: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error deleting RBSI: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to delete RBSI")
                            .build());
        }
    }
}
