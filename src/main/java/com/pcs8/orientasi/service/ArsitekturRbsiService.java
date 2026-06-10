package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.ArsitekturRbsiRequest;
import com.pcs8.orientasi.domain.dto.response.ArsitekturRbsiResponse;
import com.pcs8.orientasi.domain.dto.response.SnapshotArsitekturRbsiResponse;

import java.util.List;
import java.util.UUID;

public interface ArsitekturRbsiService {

    ArsitekturRbsiResponse create(ArsitekturRbsiRequest request);

    ArsitekturRbsiResponse getById(UUID id);

    List<ArsitekturRbsiResponse> getByRbsiId(UUID rbsiId);

    ArsitekturRbsiResponse update(UUID id, ArsitekturRbsiRequest request);

    void delete(UUID id);

    void deleteByRbsiId(UUID rbsiId);

    List<ArsitekturRbsiResponse> bulkCreate(List<ArsitekturRbsiRequest> requests);

    List<ArsitekturRbsiResponse> bulkUpdate(List<ArsitekturRbsiRequest> requests);

    // Snapshot current state lalu sinkronisasi year_status tahun ini dengan status aplikasi aktual
    List<ArsitekturRbsiResponse> updateData(UUID rbsiId);

    // Ambil semua snapshot, dikelompokkan per tanggal
    List<SnapshotArsitekturRbsiResponse.SnapshotGroup> getSnapshots(UUID rbsiId);
}
