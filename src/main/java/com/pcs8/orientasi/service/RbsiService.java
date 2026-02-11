package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.RbsiInisiatifRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiProgramRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiRequest;
import com.pcs8.orientasi.domain.dto.response.RbsiHistoryResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiInisiatifResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiProgramResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiResponse;

import java.util.List;
import java.util.UUID;

public interface RbsiService {

    RbsiResponse createRbsi(RbsiRequest request);

    RbsiResponse getRbsi(UUID id, Integer tahun);

    List<RbsiResponse> getAllRbsi();

    RbsiResponse updateRbsi(UUID id, RbsiRequest request);

    void deleteRbsi(UUID id);

    RbsiProgramResponse createOrUpdateProgram(RbsiProgramRequest request);

    RbsiProgramResponse updateProgram(UUID programId, RbsiProgramRequest request);

    void deleteProgram(UUID programId);

    RbsiInisiatifResponse createOrUpdateInisiatif(RbsiInisiatifRequest request);

    RbsiInisiatifResponse updateInisiatif(UUID inisiatifId, RbsiInisiatifRequest request);

    void deleteInisiatif(UUID inisiatifId);

    List<RbsiProgramResponse> getProgramsByRbsiAndTahun(UUID rbsiId, Integer tahun);

    List<RbsiHistoryResponse> getHistory(UUID rbsiId);

    RbsiHistoryResponse getHistoryByTahun(UUID rbsiId, Integer tahun);
}
