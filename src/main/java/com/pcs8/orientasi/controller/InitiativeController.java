package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.request.InitiativeCreateRequest;
import com.pcs8.orientasi.domain.dto.response.InitiativeResponse;
import com.pcs8.orientasi.service.InitiativeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/initiative")
@RequiredArgsConstructor
public class InitiativeController {

    private static final Logger log = LoggerFactory.getLogger(InitiativeController.class);

    private final InitiativeService initiativeService;

    @PostMapping
    public ResponseEntity<BaseResponse> create(@Valid @RequestBody InitiativeCreateRequest request) {
        log.info("POST /initiative - Creating new Initiative");
        try {
            InitiativeResponse response = initiativeService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.CREATED.value())
                            .message("Initiative created successfully")
                            .data(response)
                            .build());
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create Initiative: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error creating Initiative: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to create Initiative")
                            .build());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<BaseResponse> findByProgramAndYear(
            @RequestParam("program_id") UUID programId,
            @RequestParam("year") Integer yearVersion) {
        log.info("GET /initiative/list - Fetching initiatives for Program: {} and year: {}", 
                programId, yearVersion);
        try {
            List<InitiativeResponse> responses = initiativeService.findByProgramAndYear(programId, yearVersion);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("Initiative list fetched successfully")
                    .data(responses)
                    .build());
        } catch (Exception e) {
            log.error("Error fetching initiatives: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to fetch initiative list")
                            .build());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> findById(@PathVariable UUID id) {
        log.info("GET /initiative/{} - Fetching Initiative by id", id);
        try {
            InitiativeResponse response = initiativeService.findById(id);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("Initiative fetched successfully")
                    .data(response)
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("Initiative not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error fetching Initiative: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to fetch Initiative")
                            .build());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> update(@PathVariable UUID id,
                                               @Valid @RequestBody InitiativeCreateRequest request) {
        log.info("PUT /initiative/{} - Updating Initiative", id);
        try {
            InitiativeResponse response = initiativeService.update(id, request);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("Initiative updated successfully")
                    .data(response)
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update Initiative: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error updating Initiative: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to update Initiative")
                            .build());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BaseResponse> updateStatus(@PathVariable UUID id,
                                                     @RequestBody Map<String, String> request) {
        log.info("PATCH /initiative/{}/status - Updating Initiative status", id);
        try {
            String status = request.get("status");
            if (status == null || status.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Status is required")
                                .build());
            }
            InitiativeResponse response = initiativeService.updateStatus(id, status);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("Initiative status updated successfully")
                    .data(response)
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update Initiative status: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error updating Initiative status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to update Initiative status")
                            .build());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> delete(@PathVariable UUID id) {
        log.info("DELETE /initiative/{} - Deleting Initiative", id);
        try {
            initiativeService.delete(id);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("Initiative deleted successfully")
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete Initiative: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error deleting Initiative: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to delete Initiative")
                            .build());
        }
    }
}
