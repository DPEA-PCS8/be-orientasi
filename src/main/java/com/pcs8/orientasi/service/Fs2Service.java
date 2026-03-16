package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.Fs2DocumentRequest;
import com.pcs8.orientasi.domain.dto.response.Fs2DocumentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface Fs2Service {

    Fs2DocumentResponse create(Fs2DocumentRequest request);

    Fs2DocumentResponse getById(UUID id);

    List<Fs2DocumentResponse> getAll();

    Page<Fs2DocumentResponse> search(String search, UUID bidangId, UUID skpaId, String status, Pageable pageable, String userDepartment, boolean canSeeAll);

    List<Fs2DocumentResponse> searchList(String search, UUID bidangId, UUID skpaId, String status, String userDepartment, boolean canSeeAll);

    Page<Fs2DocumentResponse> searchApproved(
            com.pcs8.orientasi.domain.dto.request.Fs2ApprovedSearchFilter filter,
            Pageable pageable,
            String userDepartment,
            boolean canSeeAll
    );

    Fs2DocumentResponse update(UUID id, Fs2DocumentRequest request);

    Fs2DocumentResponse updateStatus(UUID id, String status);

    void delete(UUID id);
}
