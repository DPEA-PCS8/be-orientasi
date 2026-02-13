package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.domain.dto.request.RbsiInisiatifRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiProgramRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiHistoryResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiInisiatifResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiProgramResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiResponse;
import com.pcs8.orientasi.service.RbsiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rbsi")
@RequiredArgsConstructor
public class RbsiController {

    private final RbsiService rbsiService;

    @PostMapping
    public ResponseEntity<BaseResponse> createRbsi(@Valid @RequestBody RbsiRequest request) {
        RbsiResponse response = rbsiService.createRbsi(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "RBSI created", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse> getAllRbsi() {
        List<RbsiResponse> responses = rbsiService.getAllRbsi();
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getRbsi(@PathVariable UUID id, @RequestParam(required = false) Integer tahun) {
        RbsiResponse response = rbsiService.getRbsi(id, tahun);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> updateRbsi(@PathVariable UUID id, @Valid @RequestBody RbsiRequest request) {
        RbsiResponse response = rbsiService.updateRbsi(id, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "RBSI updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> deleteRbsi(@PathVariable UUID id) {
        rbsiService.deleteRbsi(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "RBSI deleted", null));
    }

    @PostMapping("/programs")
    public ResponseEntity<BaseResponse> createOrUpdateProgram(@Valid @RequestBody RbsiProgramRequest request) {
        RbsiProgramResponse response = rbsiService.createOrUpdateProgram(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Program saved", response));
    }

    @PutMapping("/programs/{id}")
    public ResponseEntity<BaseResponse> updateProgram(@PathVariable UUID id, @Valid @RequestBody RbsiProgramRequest request) {
        RbsiProgramResponse response = rbsiService.updateProgram(id, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Program updated", response));
    }

    @DeleteMapping("/programs/{id}")
    public ResponseEntity<BaseResponse> deleteProgram(@PathVariable UUID id) {
        rbsiService.deleteProgram(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Program deleted", null));
    }

    @GetMapping("/{rbsiId}/programs")
    public ResponseEntity<BaseResponse> getProgramsByRbsi(@PathVariable UUID rbsiId, @RequestParam(required = false) Integer tahun) {
        RbsiResponse rbsi = rbsiService.getRbsi(rbsiId, tahun);
        List<RbsiProgramResponse> programs = rbsi.getPrograms();
        if (programs == null) {
            programs = Collections.emptyList();
        }
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", programs));
    }

    @PostMapping("/inisiatifs")
    public ResponseEntity<BaseResponse> createOrUpdateInisiatif(@Valid @RequestBody RbsiInisiatifRequest request) {
        RbsiInisiatifResponse response = rbsiService.createOrUpdateInisiatif(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Inisiatif saved", response));
    }

    @PutMapping("/inisiatifs/{id}")
    public ResponseEntity<BaseResponse> updateInisiatif(@PathVariable UUID id, @Valid @RequestBody RbsiInisiatifRequest request) {
        RbsiInisiatifResponse response = rbsiService.updateInisiatif(id, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Inisiatif updated", response));
    }

    @DeleteMapping("/inisiatifs/{id}")
    public ResponseEntity<BaseResponse> deleteInisiatif(@PathVariable UUID id) {
        rbsiService.deleteInisiatif(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Inisiatif deleted", null));
    }

    @GetMapping("/{rbsiId}/history")
    public ResponseEntity<BaseResponse> getHistory(@PathVariable UUID rbsiId) {
        List<RbsiHistoryResponse> history = rbsiService.getHistory(rbsiId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", history));
    }

    @GetMapping("/{rbsiId}/history/{tahun}")
    public ResponseEntity<BaseResponse> getHistoryByTahun(@PathVariable UUID rbsiId, @PathVariable Integer tahun) {
        RbsiHistoryResponse history = rbsiService.getHistoryByTahun(rbsiId, tahun);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", history));
    }

    @PostMapping("/{rbsiId}/copy-programs")
    public ResponseEntity<BaseResponse> copyProgramsFromYear(
            @PathVariable UUID rbsiId,
            @RequestParam Integer fromTahun,
            @RequestParam Integer toTahun) {
        List<RbsiProgramResponse> copiedPrograms = rbsiService.copyProgramsFromYear(rbsiId, fromTahun, toTahun);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Programs copied successfully", copiedPrograms));
    }

    @PostMapping("/programs/{programId}/copy")
    public ResponseEntity<BaseResponse> copyProgram(
            @PathVariable UUID programId,
            @RequestParam Integer toTahun,
            @RequestParam(required = false) String newNomorProgram) {
        RbsiProgramResponse copiedProgram = rbsiService.copyProgram(programId, toTahun, newNomorProgram);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Program copied successfully", copiedProgram));
    }

    @PostMapping("/inisiatifs/{inisiatifId}/copy")
    public ResponseEntity<BaseResponse> copyInisiatif(
            @PathVariable UUID inisiatifId,
            @RequestParam UUID toProgramId,
            @RequestParam(required = false) String newNomorInisiatif) {
        RbsiInisiatifResponse copiedInisiatif = rbsiService.copyInisiatif(inisiatifId, toProgramId, newNomorInisiatif);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Inisiatif copied successfully", copiedInisiatif));
    }
}
