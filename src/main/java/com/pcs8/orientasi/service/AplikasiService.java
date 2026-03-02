package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.AplikasiRequest;
import com.pcs8.orientasi.domain.dto.response.AplikasiResponse;

import java.util.List;
import java.util.UUID;

public interface AplikasiService {

    AplikasiResponse create(AplikasiRequest request);

    AplikasiResponse getById(UUID id);

    AplikasiResponse getByKode(String kode);

    List<AplikasiResponse> getAll();

    AplikasiResponse update(UUID id, AplikasiRequest request);

    void delete(UUID id);
}
