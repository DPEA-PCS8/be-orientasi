package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.SkpaRequest;
import com.pcs8.orientasi.domain.dto.response.SkpaResponse;

import java.util.List;
import java.util.UUID;

public interface SkpaService {

    SkpaResponse create(SkpaRequest request);

    SkpaResponse getById(UUID id);

    SkpaResponse getByKode(String kode);

    List<SkpaResponse> getAll();

    SkpaResponse update(UUID id, SkpaRequest request);

    void delete(UUID id);
}
