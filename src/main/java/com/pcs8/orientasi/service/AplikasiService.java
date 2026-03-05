package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.AplikasiRequest;
import com.pcs8.orientasi.domain.dto.request.AplikasiStatusRequest;
import com.pcs8.orientasi.domain.dto.response.AplikasiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AplikasiService {

    AplikasiResponse create(AplikasiRequest request);

    AplikasiResponse getById(UUID id);

    AplikasiResponse getByKode(String kode);

    List<AplikasiResponse> getAll();

    Page<AplikasiResponse> search(String search, UUID bidangId, UUID skpaId, String status, Pageable pageable);

    List<AplikasiResponse> searchList(String search, UUID bidangId, UUID skpaId, String status);

    AplikasiResponse update(UUID id, AplikasiRequest request);

    AplikasiResponse updateStatus(UUID id, String status);

    AplikasiResponse updateStatusWithDetails(UUID id, AplikasiStatusRequest request);

    void delete(UUID id);
}
