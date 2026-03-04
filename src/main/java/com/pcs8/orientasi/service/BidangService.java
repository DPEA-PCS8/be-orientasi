package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.BidangRequest;
import com.pcs8.orientasi.domain.dto.response.BidangResponse;

import java.util.List;
import java.util.UUID;

public interface BidangService {

    BidangResponse create(BidangRequest request);

    BidangResponse getById(UUID id);

    BidangResponse getByKode(String kode);

    List<BidangResponse> getAll();

    BidangResponse update(UUID id, BidangRequest request);

    void delete(UUID id);
}
