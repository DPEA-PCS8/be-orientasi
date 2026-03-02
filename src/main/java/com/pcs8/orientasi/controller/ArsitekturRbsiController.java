package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.domain.dto.request.ArsitekturRbsiRequest;
import com.pcs8.orientasi.domain.dto.response.ArsitekturRbsiResponse;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.service.ArsitekturRbsiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/arsitektur/rbsi")
@RequiredArgsConstructor
@RequiresRole({"admin", "pengembang"})
public class ArsitekturRbsiController {

    private final ArsitekturRbsiService arsitekturRbsiService;

    @PostMapping
    public ResponseEntity<BaseResponse> create(@Valid @RequestBody ArsitekturRbsiRequest request) {
        ArsitekturRbsiResponse response = arsitekturRbsiService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Arsitektur RBSI berhasil dibuat", response));
    }

    @PostMapping("/bulk")
    public ResponseEntity<BaseResponse> bulkCreate(@Valid @RequestBody List<ArsitekturRbsiRequest> requests) {
        List<ArsitekturRbsiResponse> responses = arsitekturRbsiService.bulkCreate(requests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Arsitektur RBSI berhasil dibuat (bulk)", responses));
    }

    @GetMapping
    public ResponseEntity<BaseResponse> getByRbsiId(@RequestParam UUID rbsiId) {
        List<ArsitekturRbsiResponse> responses = arsitekturRbsiService.getByRbsiId(rbsiId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getById(@PathVariable UUID id) {
        ArsitekturRbsiResponse response = arsitekturRbsiService.getById(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> update(@PathVariable UUID id, @Valid @RequestBody ArsitekturRbsiRequest request) {
        ArsitekturRbsiResponse response = arsitekturRbsiService.update(id, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Arsitektur RBSI berhasil diupdate", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> delete(@PathVariable UUID id) {
        arsitekturRbsiService.delete(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Arsitektur RBSI berhasil dihapus", null));
    }

    @DeleteMapping("/by-rbsi/{rbsiId}")
    public ResponseEntity<BaseResponse> deleteByRbsiId(@PathVariable UUID rbsiId) {
        arsitekturRbsiService.deleteByRbsiId(rbsiId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Arsitektur RBSI berhasil dihapus untuk RBSI ini", null));
    }
}
