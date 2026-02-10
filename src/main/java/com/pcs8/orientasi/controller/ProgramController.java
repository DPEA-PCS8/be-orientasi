package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.request.ProgramRequest;
import com.pcs8.orientasi.domain.dto.response.ProgramListResponse;
import com.pcs8.orientasi.domain.dto.response.ProgramResponse;
import com.pcs8.orientasi.service.ProgramService;
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
@RequestMapping("/program")
@RequiredArgsConstructor
public class ProgramController {

    private static final Logger log = LoggerFactory.getLogger(ProgramController.class);

    private final ProgramService programService;

    @PostMapping
    public ResponseEntity<BaseResponse> create(@Valid @RequestBody ProgramRequest request) {
        log.info("POST /program - Creating new Program");
        try {
            ProgramResponse response = programService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.CREATED.value())
                            .message("Program created successfully")
                            .data(response)
                            .build());
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create Program: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error creating Program: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to create Program")
                            .build());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<BaseResponse> findByRbsiAndYear(
            @RequestParam("rbsi_id") UUID rbsiId,
            @RequestParam("year") Integer yearVersion) {
        log.info("GET /program/list - Fetching programs for RBSI: {} and year: {}", rbsiId, yearVersion);
        try {
            ProgramListResponse response = programService.findByRbsiAndYear(rbsiId, yearVersion);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("Program list fetched successfully")
                    .data(response)
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("Failed to fetch programs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error fetching programs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to fetch program list")
                            .build());
        }
    }

    @GetMapping("/years")
    public ResponseEntity<BaseResponse> getAvailableYears(@RequestParam("rbsi_id") UUID rbsiId) {
        log.info("GET /program/years - Fetching available years for RBSI: {}", rbsiId);
        try {
            List<Integer> years = programService.getAvailableYears(rbsiId);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("Available years fetched successfully")
                    .data(years)
                    .build());
        } catch (Exception e) {
            log.error("Error fetching available years: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to fetch available years")
                            .build());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> findById(@PathVariable UUID id) {
        log.info("GET /program/{} - Fetching Program by id", id);
        try {
            ProgramResponse response = programService.findById(id);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("Program fetched successfully")
                    .data(response)
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("Program not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error fetching Program: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to fetch Program")
                            .build());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> update(@PathVariable UUID id,
                                               @Valid @RequestBody ProgramRequest request) {
        log.info("PUT /program/{} - Updating Program", id);
        try {
            ProgramResponse response = programService.update(id, request);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("Program updated successfully")
                    .data(response)
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update Program: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error updating Program: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to update Program")
                            .build());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> delete(@PathVariable UUID id,
                                               @RequestParam("year") Integer yearVersion) {
        log.info("DELETE /program/{} - Deleting Program for year: {}", id, yearVersion);
        try {
            programService.delete(id, yearVersion);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("Program deleted successfully")
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete Program: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error deleting Program: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to delete Program")
                            .build());
        }
    }
}
