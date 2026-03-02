package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.SubKategoriRequest;
import com.pcs8.orientasi.domain.dto.response.SubKategoriResponse;

import java.util.List;
import java.util.UUID;

public interface SubKategoriService {

    SubKategoriResponse create(SubKategoriRequest request);

    SubKategoriResponse getById(UUID id);

    List<SubKategoriResponse> getByCategoryCode(String categoryCode);

    List<SubKategoriResponse> getAll();

    List<String> getAllCategoryCodes();

    SubKategoriResponse update(UUID id, SubKategoriRequest request);

    void delete(UUID id);
}
