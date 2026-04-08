package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.ChangelogRequest;
import com.pcs8.orientasi.domain.dto.request.UpdateSnapshotRequest;
import com.pcs8.orientasi.domain.dto.response.AplikasiHistorisListResponse;
import com.pcs8.orientasi.domain.dto.response.AplikasiSnapshotResponse;
import com.pcs8.orientasi.domain.dto.response.AplikasiStatistikResponse;
import com.pcs8.orientasi.domain.dto.response.ChangelogInfo;
import com.pcs8.orientasi.domain.entity.MstAplikasi;

import java.util.List;
import java.util.UUID;

public interface AplikasiHistorisService {

    /**
     * Create or update snapshot for an aplikasi for specific year
     */
    AplikasiSnapshotResponse createOrUpdateSnapshot(UUID aplikasiId, Integer tahun, String snapshotType);

    /**
     * Update existing snapshot
     */
    AplikasiSnapshotResponse updateSnapshot(UUID snapshotId, UpdateSnapshotRequest request);

    /**
     * Create snapshot from existing aplikasi entity
     */
    AplikasiSnapshotResponse createSnapshotFromAplikasi(MstAplikasi aplikasi, Integer tahun, String snapshotType);

    /**
     * Generate snapshots for all active applications for a specific year
     */
    List<AplikasiSnapshotResponse> generateSnapshotsForYear(Integer tahun);

    /**
     * Get snapshot by ID
     */
    AplikasiSnapshotResponse getSnapshotById(UUID id);

    /**
     * Get snapshot by aplikasi ID and year
     */
    AplikasiSnapshotResponse getSnapshotByAplikasiAndTahun(UUID aplikasiId, Integer tahun);

    /**
     * Get list of snapshots for period view
     */
    List<AplikasiHistorisListResponse> getHistorisByPeriode(Integer startYear, Integer endYear);

    /**
     * Get list of snapshots for a specific year
     */
    List<AplikasiHistorisListResponse> getHistorisByTahun(Integer tahun);

    /**
     * Get full snapshot details for a specific year (for export)
     */
    List<AplikasiSnapshotResponse> getFullSnapshotsByTahun(Integer tahun);

    /**
     * Get all available years that have snapshots
     */
    List<Integer> getAvailableYears();

    /**
     * Get statistics for a specific year
     */
    AplikasiStatistikResponse getStatistikByTahun(Integer tahun);

    /**
     * Get statistics for a period
     */
    List<AplikasiStatistikResponse> getStatistikByPeriode(Integer startYear, Integer endYear);

    /**
     * Add changelog entry to a snapshot
     */
    ChangelogInfo addChangelog(UUID snapshotId, ChangelogRequest request);

    /**
     * Get changelogs for a snapshot
     */
    List<ChangelogInfo> getChangelogsBySnapshotId(UUID snapshotId);

    /**
     * Delete a changelog entry
     */
    void deleteChangelog(UUID changelogId);

    /**
     * Delete a snapshot
     */
    void deleteSnapshot(UUID id);

    /**
     * Get all snapshots for an aplikasi
     */
    List<AplikasiSnapshotResponse> getSnapshotsByAplikasiId(UUID aplikasiId);

    /**
     * Triggers snapshot update when aplikasi is updated
     * Called internally by AplikasiService
     */
    void onAplikasiUpdated(MstAplikasi aplikasi, String keterangan);
}
