package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.domain.dto.ApiResponse;
import com.pcs8.orientasi.domain.dto.rbsi.*;
import com.pcs8.orientasi.service.RbsiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/rbsi")
@RequiredArgsConstructor
@Slf4j
public class RbsiController {

    private final RbsiService rbsiService;

    /**
     * Create new RBSI
     * POST /api/v1/rbsi
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RbsiResponse>> createRbsi(
            @Valid @RequestBody RbsiCreateRequest request) {
        log.info("POST /api/v1/rbsi - Creating RBSI with periode: {}", request.getPeriode());

        RbsiResponse response = rbsiService.createRbsi(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<RbsiResponse>builder()
                        .status("success")
                        .message("RBSI berhasil dibuat")
                        .data(response)
                        .build());
    }

    /**
     * Get all RBSI list
     * GET /api/v1/rbsi/list
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<RbsiListResponse>> getAllRbsi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/rbsi/list - page: {}, size: {}", page, size);

        RbsiListResponse response = rbsiService.getAllRbsi(page, size);

        return ResponseEntity.ok(ApiResponse.<RbsiListResponse>builder()
                .status("success")
                .message("Data RBSI berhasil diambil")
                .data(response)
                .build());
    }

    /**
     * Get RBSI by ID
     * GET /api/v1/rbsi/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RbsiResponse>> getRbsiById(@PathVariable UUID id) {
        log.info("GET /api/v1/rbsi/{}", id);

        RbsiResponse response = rbsiService.getRbsiById(id);

        return ResponseEntity.ok(ApiResponse.<RbsiResponse>builder()
                .status("success")
                .message("Data RBSI berhasil diambil")
                .data(response)
                .build());
    }

    /**
     * Create new Program with initiatives
     * POST /api/v1/program
     */
    @PostMapping("/program")
    public ResponseEntity<ApiResponse<ProgramResponse>> createProgram(
            @Valid @RequestBody ProgramCreateRequest request) {
        log.info("POST /api/v1/program - Creating program: {}", request.getName());

        ProgramResponse response = rbsiService.createProgram(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ProgramResponse>builder()
                        .status("success")
                        .message("Program berhasil dibuat")
                        .data(response)
                        .build());
    }

    /**
     * Get programs list by year and RBSI
     * GET /api/v1/program/list?rbsi_id={}&year={}&page={}&size={}
     */
    @GetMapping("/program/list")
    public ResponseEntity<ApiResponse<ProgramListResponse>> getProgramList(
            @RequestParam("rbsi_id") UUID rbsiId,
            @RequestParam("year") Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/program/list - rbsiId: {}, year: {}", rbsiId, year);

        ProgramListResponse response = rbsiService.getProgramsByYearAndRbsi(rbsiId, year, page, size);

        return ResponseEntity.ok(ApiResponse.<ProgramListResponse>builder()
                .status("success")
                .message("Data program berhasil diambil")
                .data(response)
                .build());
    }

    /**
     * Add initiative to existing program
     * POST /api/v1/program/{programId}/initiative
     */
    @PostMapping("/program/{programId}/initiative")
    public ResponseEntity<ApiResponse<InitiativeResponse>> addInitiative(
            @PathVariable UUID programId,
            @RequestParam("year_version") Integer yearVersion,
            @Valid @RequestBody InitiativeCreateRequest request) {
        log.info("POST /api/v1/program/{}/initiative - Adding initiative: {}", programId, request.getName());

        InitiativeResponse response = rbsiService.addInitiativeToProgram(programId, request, yearVersion);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<InitiativeResponse>builder()
                        .status("success")
                        .message("Inisiatif berhasil ditambahkan")
                        .data(response)
                        .build());
    }
}
