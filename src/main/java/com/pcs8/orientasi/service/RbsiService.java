package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.rbsi.*;

import java.util.UUID;

public interface RbsiService {

    RbsiResponse createRbsi(RbsiCreateRequest request);

    RbsiListResponse getAllRbsi(int page, int size);

    RbsiResponse getRbsiById(UUID id);

    ProgramResponse createProgram(ProgramCreateRequest request);

    ProgramListResponse getProgramsByYearAndRbsi(UUID rbsiId, Integer year, int page, int size);

    InitiativeResponse addInitiativeToProgram(UUID programId, InitiativeCreateRequest request, Integer yearVersion);
}
