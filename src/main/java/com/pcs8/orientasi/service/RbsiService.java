package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.BatchKepProgressRequest;
import com.pcs8.orientasi.domain.dto.request.KepProgressRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiAnalyticsRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiInisiatifRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiKepRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiProgramRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiRequest;
import com.pcs8.orientasi.domain.dto.response.BatchKepProgressResponse;
import com.pcs8.orientasi.domain.dto.response.InisiatifGroupResponse;
import com.pcs8.orientasi.domain.dto.response.KepProgressFullResponse;
import com.pcs8.orientasi.domain.dto.response.KepProgressResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiAnalyticsResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiHistoryResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiInisiatifResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiKepResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiMonitoringResponse;
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

    List<InisiatifGroupResponse> getInisiatifGroups(UUID rbsiId);

    List<RbsiProgramResponse> getProgramsByRbsiAndTahun(UUID rbsiId, Integer tahun);

    List<RbsiHistoryResponse> getHistory(UUID rbsiId);

    RbsiHistoryResponse getHistoryByTahun(UUID rbsiId, Integer tahun);

    List<RbsiProgramResponse> copyProgramsFromYear(UUID rbsiId, Integer fromTahun, Integer toTahun);

    RbsiProgramResponse copyProgram(UUID programId, Integer toTahun, String newNomorProgram);

    RbsiInisiatifResponse copyInisiatif(UUID inisiatifId, UUID toProgramId, String newNomorInisiatif);

    // KEP methods
    List<RbsiKepResponse> getKepList(UUID rbsiId);

    RbsiKepResponse createKep(UUID rbsiId, RbsiKepRequest request);

    KepProgressFullResponse getKepProgress(UUID rbsiId, Integer tahun);

    BatchKepProgressResponse batchUpdateKepProgress(UUID rbsiId, BatchKepProgressRequest request);

    KepProgressResponse updateKepProgress(UUID rbsiId, UUID kepId, KepProgressRequest request);

    RbsiMonitoringResponse getMonitoringData(UUID rbsiId);

    // Analytics
    RbsiAnalyticsResponse getAnalytics(UUID rbsiId, RbsiAnalyticsRequest request);
}
