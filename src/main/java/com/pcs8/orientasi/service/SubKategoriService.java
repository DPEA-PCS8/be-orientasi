package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.SubKategoriRequest;
import com.pcs8.orientasi.domain.dto.response.SubKategoriResponse;
import com.pcs8.orientasi.domain.dto.response.SubKategoriSnapshotResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SubKategoriService {

    SubKategoriResponse create(SubKategoriRequest request);

    SubKategoriResponse getById(UUID id);

    List<SubKategoriResponse> getByCategoryCode(String categoryCode);

    List<SubKategoriResponse> getAll();

    List<String> getAllCategoryCodes();

    /**
     * Get all distinct category codes with their full names
     * Returns Map where key is code (e.g., "CS") and value is name (e.g., "Core System")
     */
    Map<String, String> getAllCategoryCodesWithNames();

    SubKategoriResponse update(UUID id, SubKategoriRequest request);

    void delete(UUID id);

    // Snapshot methods
    /**
     * Create snapshot for all sub kategori in the given year
     */
    void createYearlySnapshot(Integer year);

    /**
     * Get snapshots by year
     */
    List<SubKategoriSnapshotResponse> getSnapshotsByYear(Integer year);

    /**
     * Get all distinct snapshot years
     */
    List<Integer> getDistinctSnapshotYears();

    /**
     * Get snapshot history for a specific sub kategori
     */
    List<SubKategoriSnapshotResponse> getSnapshotHistoryBySubKategoriId(UUID subKategoriId);
}
