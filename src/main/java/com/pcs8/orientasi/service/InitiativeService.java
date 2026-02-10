package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.InitiativeCreateRequest;
import com.pcs8.orientasi.domain.dto.response.InitiativeResponse;

import java.util.List;
import java.util.UUID;

public interface InitiativeService {

    InitiativeResponse create(InitiativeCreateRequest request);

    List<InitiativeResponse> findByProgramAndYear(UUID programId, Integer yearVersion);

    InitiativeResponse findById(UUID id);

    InitiativeResponse update(UUID id, InitiativeCreateRequest request);

    void delete(UUID id);

    InitiativeResponse updateStatus(UUID id, String status);
}
