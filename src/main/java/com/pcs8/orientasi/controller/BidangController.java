package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.domain.dto.request.BidangRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.BidangResponse;
import com.pcs8.orientasi.service.BidangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/arsitektur/bidang")
@RequiredArgsConstructor
@RequiresRole({"admin", "pengembang"})
public class BidangController {

    private final BidangService bidangService;

    @PostMapping
    public ResponseEntity<BaseResponse> create(@Valid @RequestBody BidangRequest request) {
        BidangResponse response = bidangService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Bidang berhasil dibuat", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse> getAll() {
        List<BidangResponse> responses = bidangService.getAll();
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getById(@PathVariable UUID id) {
        BidangResponse response = bidangService.getById(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", response));
    }

    @GetMapping("/kode/{kode}")
    public ResponseEntity<BaseResponse> getByKode(@PathVariable String kode) {
        BidangResponse response = bidangService.getByKode(kode);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> update(@PathVariable UUID id, @Valid @RequestBody BidangRequest request) {
        BidangResponse response = bidangService.update(id, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Bidang berhasil diupdate", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> delete(@PathVariable UUID id) {
        bidangService.delete(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Bidang berhasil dihapus", null));
    }
}
