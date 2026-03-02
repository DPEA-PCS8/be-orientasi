package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.domain.dto.request.AplikasiRequest;
import com.pcs8.orientasi.domain.dto.response.AplikasiResponse;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.service.AplikasiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/arsitektur/aplikasi")
@RequiredArgsConstructor
@RequiresRole({"admin", "pengembang"})
public class AplikasiController {

    private final AplikasiService aplikasiService;

    @PostMapping
    public ResponseEntity<BaseResponse> create(@Valid @RequestBody AplikasiRequest request) {
        AplikasiResponse response = aplikasiService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Aplikasi berhasil dibuat", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse> getAll() {
        List<AplikasiResponse> responses = aplikasiService.getAll();
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getById(@PathVariable UUID id) {
        AplikasiResponse response = aplikasiService.getById(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", response));
    }

    @GetMapping("/kode/{kode}")
    public ResponseEntity<BaseResponse> getByKode(@PathVariable String kode) {
        AplikasiResponse response = aplikasiService.getByKode(kode);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> update(@PathVariable UUID id, @Valid @RequestBody AplikasiRequest request) {
        AplikasiResponse response = aplikasiService.update(id, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Aplikasi berhasil diupdate", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> delete(@PathVariable UUID id) {
        aplikasiService.delete(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Aplikasi berhasil dihapus", null));
    }
}
