package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.constant.ConstantVariable;
import com.pcs8.orientasi.domain.dto.request.ChangelogRequest;
import com.pcs8.orientasi.domain.dto.request.GenerateSnapshotRequest;
import com.pcs8.orientasi.domain.dto.request.UpdateSnapshotRequest;
import com.pcs8.orientasi.domain.dto.response.*;
import com.pcs8.orientasi.service.AplikasiHistorisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/arsitektur/historis-aplikasi")
@RequiredArgsConstructor
@RequiresRole({"admin", "pengembang"})
public class HistorisAplikasiController {

    private final AplikasiHistorisService historisService;

    /**
     * Get list of snapshots for a specific year
     */
    @GetMapping("/tahun/{tahun}")
    public ResponseEntity<BaseResponse> getByTahun(@PathVariable Integer tahun) {
        List<AplikasiHistorisListResponse> responses = historisService.getHistorisByTahun(tahun);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, responses));
    }

    /**
     * Get list of snapshots for a period
     */
    @GetMapping("/periode")
    public ResponseEntity<BaseResponse> getByPeriode(
            @RequestParam(name = "start_year") Integer startYear,
            @RequestParam(name = "end_year") Integer endYear
    ) {
        List<AplikasiHistorisListResponse> responses = historisService.getHistorisByPeriode(startYear, endYear);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, responses));
    }

    /**
     * Get all available years that have snapshots
     */
    @GetMapping("/years")
    public ResponseEntity<BaseResponse> getAvailableYears() {
        List<Integer> years = historisService.getAvailableYears();
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, years));
    }

    /**
     * Get snapshot detail by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getById(@PathVariable UUID id) {
        AplikasiSnapshotResponse response = historisService.getSnapshotById(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, response));
    }

    /**
     * Get snapshot by aplikasi ID and year
     */
    @GetMapping("/aplikasi/{aplikasiId}/tahun/{tahun}")
    public ResponseEntity<BaseResponse> getByAplikasiAndTahun(
            @PathVariable UUID aplikasiId,
            @PathVariable Integer tahun
    ) {
        AplikasiSnapshotResponse response = historisService.getSnapshotByAplikasiAndTahun(aplikasiId, tahun);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, response));
    }

    /**
     * Get all snapshots for an aplikasi
     */
    @GetMapping("/aplikasi/{aplikasiId}")
    public ResponseEntity<BaseResponse> getByAplikasiId(@PathVariable UUID aplikasiId) {
        List<AplikasiSnapshotResponse> responses = historisService.getSnapshotsByAplikasiId(aplikasiId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, responses));
    }

    /**
     * Get statistics for a specific year
     */
    @GetMapping("/statistik/tahun/{tahun}")
    public ResponseEntity<BaseResponse> getStatistikByTahun(@PathVariable Integer tahun) {
        AplikasiStatistikResponse response = historisService.getStatistikByTahun(tahun);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, response));
    }

    /**
     * Get statistics for a period
     */
    @GetMapping("/statistik/periode")
    public ResponseEntity<BaseResponse> getStatistikByPeriode(
            @RequestParam(name = "start_year") Integer startYear,
            @RequestParam(name = "end_year") Integer endYear
    ) {
        List<AplikasiStatistikResponse> responses = historisService.getStatistikByPeriode(startYear, endYear);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, responses));
    }

    /**
     * Generate snapshots for all applications for a specific year
     */
    @PostMapping("/generate")
    public ResponseEntity<BaseResponse> generateSnapshots(@Valid @RequestBody GenerateSnapshotRequest request) {
        List<AplikasiSnapshotResponse> responses = historisService.generateSnapshotsForYear(request.getTahun());
        
        Map<String, Object> result = new HashMap<>();
        result.put("tahun", request.getTahun());
        result.put("total_generated", responses.size());
        result.put("snapshots", responses);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), 
                        "Berhasil generate " + responses.size() + " snapshot untuk tahun " + request.getTahun(), 
                        result));
    }

    /**
     * Create or update snapshot for a specific aplikasi and year
     */
    @PostMapping("/aplikasi/{aplikasiId}/tahun/{tahun}")
    public ResponseEntity<BaseResponse> createOrUpdateSnapshot(
            @PathVariable UUID aplikasiId,
            @PathVariable Integer tahun
    ) {
        AplikasiSnapshotResponse response = historisService.createOrUpdateSnapshot(aplikasiId, tahun, "MANUAL");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Snapshot berhasil dibuat/diupdate", response));
    }

    /**
     * Update existing snapshot
     */
    @PutMapping("/{snapshotId}")
    public ResponseEntity<BaseResponse> updateSnapshot(
            @PathVariable UUID snapshotId,
            @Valid @RequestBody UpdateSnapshotRequest request
    ) {
        AplikasiSnapshotResponse response = historisService.updateSnapshot(snapshotId, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Snapshot berhasil diupdate", response));
    }

    /**
     * Add changelog to a snapshot
     */
    @PostMapping("/{snapshotId}/changelog")
    public ResponseEntity<BaseResponse> addChangelog(
            @PathVariable UUID snapshotId,
            @Valid @RequestBody ChangelogRequest request
    ) {
        ChangelogInfo response = historisService.addChangelog(snapshotId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Changelog berhasil ditambahkan", response));
    }

    /**
     * Get changelogs for a snapshot
     */
    @GetMapping("/{snapshotId}/changelog")
    public ResponseEntity<BaseResponse> getChangelogs(@PathVariable UUID snapshotId) {
        List<ChangelogInfo> responses = historisService.getChangelogsBySnapshotId(snapshotId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, responses));
    }

    /**
     * Delete a changelog entry
     */
    @DeleteMapping("/changelog/{changelogId}")
    public ResponseEntity<BaseResponse> deleteChangelog(@PathVariable UUID changelogId) {
        historisService.deleteChangelog(changelogId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Changelog berhasil dihapus", null));
    }

    /**
     * Delete a snapshot
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> deleteSnapshot(@PathVariable UUID id) {
        historisService.deleteSnapshot(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Snapshot berhasil dihapus", null));
    }
}
