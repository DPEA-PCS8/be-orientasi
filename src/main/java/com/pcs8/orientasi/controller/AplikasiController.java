package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.constant.ConstantVariable;
import com.pcs8.orientasi.domain.dto.request.AplikasiRequest;
import com.pcs8.orientasi.domain.dto.request.AplikasiStatusRequest;
import com.pcs8.orientasi.domain.dto.response.AplikasiResponse;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.service.AplikasiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, responses));
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(name = "bidang_id", required = false) UUID bidangId,
            @RequestParam(name = "skpa_id", required = false) UUID skpaId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AplikasiResponse> pageResult = aplikasiService.search(search, bidangId, skpaId, status, pageable);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("content", pageResult.getContent());
        responseData.put("total_elements", pageResult.getTotalElements());
        responseData.put("total_pages", pageResult.getTotalPages());
        responseData.put("page", pageResult.getNumber());
        responseData.put("size", pageResult.getSize());
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, responseData));
    }

    @GetMapping("/list")
    public ResponseEntity<BaseResponse> searchList(
            @RequestParam(required = false) String search,
            @RequestParam(name = "bidang_id", required = false) UUID bidangId,
            @RequestParam(name = "skpa_id", required = false) UUID skpaId,
            @RequestParam(required = false) String status
    ) {
        List<AplikasiResponse> responses = aplikasiService.searchList(search, bidangId, skpaId, status);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getById(@PathVariable UUID id) {
        AplikasiResponse response = aplikasiService.getById(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, response));
    }

    @GetMapping("/kode/{kode}")
    public ResponseEntity<BaseResponse> getByKode(@PathVariable String kode) {
        AplikasiResponse response = aplikasiService.getByKode(kode);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> update(@PathVariable UUID id, @Valid @RequestBody AplikasiRequest request) {
        AplikasiResponse response = aplikasiService.update(id, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Aplikasi berhasil diupdate", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BaseResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam(required = false) String status,
            @RequestBody(required = false) AplikasiStatusRequest request
    ) {
        AplikasiResponse response;
        if (request != null && request.getStatus() != null) {
            // Use request body with idle details
            response = aplikasiService.updateStatusWithDetails(id, request);
        } else if (status != null) {
            // Use query param for simple status update
            response = aplikasiService.updateStatus(id, status);
        } else {
            throw new IllegalArgumentException("Status is required");
        }
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Status aplikasi berhasil diupdate", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> delete(@PathVariable UUID id) {
        aplikasiService.delete(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Aplikasi berhasil dihapus", null));
    }
}
