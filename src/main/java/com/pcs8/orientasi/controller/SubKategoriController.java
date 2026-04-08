package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.domain.dto.request.SubKategoriRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.SubKategoriResponse;
import com.pcs8.orientasi.domain.dto.response.SubKategoriSnapshotResponse;
import com.pcs8.orientasi.service.SubKategoriService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/arsitektur/sub-kategori")
@RequiredArgsConstructor
@RequiresRole({"admin", "pengembang"})
public class SubKategoriController {

    private final SubKategoriService subKategoriService;

    @PostMapping
    public ResponseEntity<BaseResponse> create(@Valid @RequestBody SubKategoriRequest request) {
        SubKategoriResponse response = subKategoriService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Sub Kategori berhasil dibuat", response));
    }

    @PostMapping("/bulk")
    public ResponseEntity<BaseResponse> bulkCreate(@Valid @RequestBody List<SubKategoriRequest> requests) {
        List<SubKategoriResponse> responses = subKategoriService.bulkCreate(requests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), 
                    responses.size() + " Sub Kategori berhasil dibuat", responses));
    }

    @GetMapping
    public ResponseEntity<BaseResponse> getAll(@RequestParam(required = false) String categoryCode) {
        List<SubKategoriResponse> responses;
        if (categoryCode != null) {
            responses = subKategoriService.getByCategoryCode(categoryCode);
        } else {
            responses = subKategoriService.getAll();
        }
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", responses));
    }

    @GetMapping("/category-codes")
    public ResponseEntity<BaseResponse> getAllCategoryCodes() {
        List<String> codes = subKategoriService.getAllCategoryCodes();
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", codes));
    }

    @GetMapping("/category-codes-with-names")
    public ResponseEntity<BaseResponse> getAllCategoryCodesWithNames() {
        Map<String, String> codesWithNames = subKategoriService.getAllCategoryCodesWithNames();
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", codesWithNames));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getById(@PathVariable UUID id) {
        SubKategoriResponse response = subKategoriService.getById(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> update(@PathVariable UUID id, @Valid @RequestBody SubKategoriRequest request) {
        SubKategoriResponse response = subKategoriService.update(id, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Sub Kategori berhasil diupdate", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> delete(@PathVariable UUID id) {
        subKategoriService.delete(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Sub Kategori berhasil dihapus", null));
    }

    // Snapshot endpoints
    @PostMapping("/snapshot/{year}")
    public ResponseEntity<BaseResponse> createYearlySnapshot(@PathVariable Integer year) {
        subKategoriService.createYearlySnapshot(year);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Snapshot tahun " + year + " berhasil dibuat", null));
    }

    @GetMapping("/snapshot/{year}")
    public ResponseEntity<BaseResponse> getSnapshotsByYear(@PathVariable Integer year) {
        List<SubKategoriSnapshotResponse> snapshots = subKategoriService.getSnapshotsByYear(year);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", snapshots));
    }

    @GetMapping("/snapshot/years")
    public ResponseEntity<BaseResponse> getDistinctSnapshotYears() {
        List<Integer> years = subKategoriService.getDistinctSnapshotYears();
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", years));
    }

    @GetMapping("/snapshot/history/{subKategoriId}")
    public ResponseEntity<BaseResponse> getSnapshotHistory(@PathVariable UUID subKategoriId) {
        List<SubKategoriSnapshotResponse> history = subKategoriService.getSnapshotHistoryBySubKategoriId(subKategoriId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", history));
    }
}
