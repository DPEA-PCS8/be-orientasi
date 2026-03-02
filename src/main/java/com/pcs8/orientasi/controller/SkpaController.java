package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.domain.dto.request.SkpaRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.SkpaResponse;
import com.pcs8.orientasi.service.SkpaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/arsitektur/skpa")
@RequiredArgsConstructor
@RequiresRole({"admin", "pengembang"})
public class SkpaController {

    private final SkpaService skpaService;

    @PostMapping
    public ResponseEntity<BaseResponse> create(@Valid @RequestBody SkpaRequest request) {
        SkpaResponse response = skpaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "SKPA berhasil dibuat", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse> getAll() {
        List<SkpaResponse> responses = skpaService.getAll();
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getById(@PathVariable UUID id) {
        SkpaResponse response = skpaService.getById(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", response));
    }

    @GetMapping("/kode/{kode}")
    public ResponseEntity<BaseResponse> getByKode(@PathVariable String kode) {
        SkpaResponse response = skpaService.getByKode(kode);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> update(@PathVariable UUID id, @Valid @RequestBody SkpaRequest request) {
        SkpaResponse response = skpaService.update(id, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "SKPA berhasil diupdate", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> delete(@PathVariable UUID id) {
        skpaService.delete(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "SKPA berhasil dihapus", null));
    }
}
