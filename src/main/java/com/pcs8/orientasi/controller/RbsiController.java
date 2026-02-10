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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/rbsi")
public class RbsiController {

    private final RbsiService rbsiService;

    public RbsiController(RbsiService rbsiService) {
        this.rbsiService = rbsiService;
    }

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
    public ResponseEntity<BaseResponse> getRbsi(@PathVariable Long id, @RequestParam(required = false) Integer tahun) {
        RbsiResponse response = rbsiService.getRbsi(id, tahun);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> updateRbsi(@PathVariable Long id, @Valid @RequestBody RbsiRequest request) {
        RbsiResponse response = rbsiService.updateRbsi(id, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "RBSI updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> deleteRbsi(@PathVariable Long id) {
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
    public ResponseEntity<BaseResponse> updateProgram(@PathVariable Long id, @Valid @RequestBody RbsiProgramRequest request) {
        RbsiProgramResponse response = rbsiService.updateProgram(id, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Program updated", response));
    }

    @DeleteMapping("/programs/{id}")
    public ResponseEntity<BaseResponse> deleteProgram(@PathVariable Long id) {
        rbsiService.deleteProgram(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Program deleted", null));
    }

    @GetMapping("/{rbsiId}/programs")
    public ResponseEntity<BaseResponse> getProgramsByRbsi(@PathVariable Long rbsiId, @RequestParam(required = false) Integer tahun) {
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
    public ResponseEntity<BaseResponse> updateInisiatif(@PathVariable Long id, @Valid @RequestBody RbsiInisiatifRequest request) {
        RbsiInisiatifResponse response = rbsiService.updateInisiatif(id, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Inisiatif updated", response));
    }

    @DeleteMapping("/inisiatifs/{id}")
    public ResponseEntity<BaseResponse> deleteInisiatif(@PathVariable Long id) {
        rbsiService.deleteInisiatif(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Inisiatif deleted", null));
    }

    @GetMapping("/{rbsiId}/history")
    public ResponseEntity<BaseResponse> getHistory(@PathVariable Long rbsiId) {
        List<RbsiHistoryResponse> history = rbsiService.getHistory(rbsiId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", history));
    }

    @GetMapping("/{rbsiId}/history/{tahun}")
    public ResponseEntity<BaseResponse> getHistoryByTahun(@PathVariable Long rbsiId, @PathVariable Integer tahun) {
        RbsiHistoryResponse history = rbsiService.getHistoryByTahun(rbsiId, tahun);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", history));
    }
}
