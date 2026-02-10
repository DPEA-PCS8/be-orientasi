package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.RbsiInisiatifRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiProgramRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiRequest;
import com.pcs8.orientasi.domain.dto.response.RbsiHistoryResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiInisiatifResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiProgramResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiResponse;

import java.util.List;

public interface RbsiService {

    RbsiResponse createRbsi(RbsiRequest request);

    RbsiResponse getRbsi(Long id, Integer tahun);

    List<RbsiResponse> getAllRbsi();

    RbsiResponse updateRbsi(Long id, RbsiRequest request);

    void deleteRbsi(Long id);

    RbsiProgramResponse createOrUpdateProgram(RbsiProgramRequest request);

    RbsiProgramResponse updateProgram(Long programId, RbsiProgramRequest request);

    void deleteProgram(Long programId);

    RbsiInisiatifResponse createOrUpdateInisiatif(RbsiInisiatifRequest request);

    RbsiInisiatifResponse updateInisiatif(Long inisiatifId, RbsiInisiatifRequest request);

    void deleteInisiatif(Long inisiatifId);

    List<RbsiProgramResponse> getProgramsByRbsiAndTahun(Long rbsiId, Integer tahun);

    List<RbsiHistoryResponse> getHistory(Long rbsiId);

    RbsiHistoryResponse getHistoryByTahun(Long rbsiId, Integer tahun);
}
