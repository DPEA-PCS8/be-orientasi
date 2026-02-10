package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.RbsiRequest;
import com.pcs8.orientasi.domain.dto.response.RbsiResponse;

import java.util.List;
import java.util.UUID;

public interface RbsiService {

    RbsiResponse create(RbsiRequest request);

    List<RbsiResponse> findAll();

    RbsiResponse findById(UUID id);

    RbsiResponse update(UUID id, RbsiRequest request);

    void delete(UUID id);
}
