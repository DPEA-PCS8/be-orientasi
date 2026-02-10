package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.ProgramRequest;
import com.pcs8.orientasi.domain.dto.response.ProgramListResponse;
import com.pcs8.orientasi.domain.dto.response.ProgramResponse;

import java.util.List;
import java.util.UUID;

public interface ProgramService {

    ProgramResponse create(ProgramRequest request);

    ProgramListResponse findByRbsiAndYear(UUID rbsiId, Integer yearVersion);

    ProgramResponse findById(UUID id);

    ProgramResponse update(UUID id, ProgramRequest request);

    void delete(UUID id, Integer yearVersion);

    List<Integer> getAvailableYears(UUID rbsiId);
}
