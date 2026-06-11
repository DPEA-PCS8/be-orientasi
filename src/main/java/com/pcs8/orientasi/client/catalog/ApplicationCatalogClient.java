package com.pcs8.orientasi.client.catalog;

import com.pcs8.orientasi.client.catalog.dto.aplikasi.CatalogAplikasiRequest;
import com.pcs8.orientasi.client.catalog.dto.aplikasi.CatalogAplikasiResponse;
import com.pcs8.orientasi.client.catalog.dto.aplikasi.CatalogStatusRequest;
import com.pcs8.orientasi.client.catalog.dto.common.CatalogBaseResponse;
import com.pcs8.orientasi.client.catalog.dto.common.CatalogPageResponse;
import com.pcs8.orientasi.client.catalog.dto.snapshot.CatalogSnapshotResponse;
import com.pcs8.orientasi.client.sso.SsoTokenClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class ApplicationCatalogClient extends AbstractCatalogClient {

    public ApplicationCatalogClient(SsoTokenClient ssoTokenClient,
                                    @Qualifier("ssoRestTemplate") RestTemplate restTemplate) {
        super(ssoTokenClient, restTemplate);
    }

    @Value("${catalog.base-url}")
    private String baseUrl;

    private static final String APLIKASI_PATH = "/api/aplikasi";

    // ── READ ─────────────────────────────────────────────────────────────────

    public CatalogPageResponse<CatalogAplikasiResponse> search(
            String namaAplikasi,
            String statusAplikasi,
            String skpa,
            String bidang,
            String kategori,
            LocalDate tanggalImplementasi,
            int page,
            int size) {

        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(baseUrl + APLIKASI_PATH)
                .queryParam("page", page)
                .queryParam("size", size);

        if (namaAplikasi != null) uri.queryParam("nama_aplikasi", namaAplikasi);
        if (statusAplikasi != null) uri.queryParam("status_aplikasi", statusAplikasi);
        if (skpa != null) uri.queryParam("skpa", skpa);
        if (bidang != null) uri.queryParam("bidang", bidang);
        if (kategori != null) uri.queryParam("kategori", kategori);
        if (tanggalImplementasi != null) uri.queryParam("tanggal_implementasi", tanggalImplementasi);

        ResponseEntity<CatalogBaseResponse<CatalogPageResponse<CatalogAplikasiResponse>>> response =
                restTemplate.exchange(
                        uri.toUriString(),
                        HttpMethod.GET,
                        new HttpEntity<>(buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    public CatalogAplikasiResponse getById(UUID id) {
        ResponseEntity<CatalogBaseResponse<CatalogAplikasiResponse>> response =
                restTemplate.exchange(
                        baseUrl + APLIKASI_PATH + "/" + id,
                        HttpMethod.GET,
                        new HttpEntity<>(buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    public List<CatalogSnapshotResponse> getSnapshots(UUID id) {
        ResponseEntity<CatalogBaseResponse<List<CatalogSnapshotResponse>>> response =
                restTemplate.exchange(
                        baseUrl + APLIKASI_PATH + "/" + id + "/snapshots",
                        HttpMethod.GET,
                        new HttpEntity<>(buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    // ── WRITE ────────────────────────────────────────────────────────────────

    public CatalogAplikasiResponse create(CatalogAplikasiRequest request) {
        ResponseEntity<CatalogBaseResponse<CatalogAplikasiResponse>> response =
                restTemplate.exchange(
                        baseUrl + APLIKASI_PATH,
                        HttpMethod.POST,
                        new HttpEntity<>(request, buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    public CatalogAplikasiResponse update(UUID id, CatalogAplikasiRequest request) {
        ResponseEntity<CatalogBaseResponse<CatalogAplikasiResponse>> response =
                restTemplate.exchange(
                        baseUrl + APLIKASI_PATH + "/" + id,
                        HttpMethod.PUT,
                        new HttpEntity<>(request, buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    public CatalogAplikasiResponse updateStatus(UUID id, CatalogStatusRequest request) {
        ResponseEntity<CatalogBaseResponse<CatalogAplikasiResponse>> response =
                restTemplate.exchange(
                        baseUrl + APLIKASI_PATH + "/" + id + "/status",
                        HttpMethod.PATCH,
                        new HttpEntity<>(request, buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    public void delete(UUID id) {
        restTemplate.exchange(
                baseUrl + APLIKASI_PATH + "/" + id,
                HttpMethod.DELETE,
                new HttpEntity<>(buildAuthHeaders()),
                Void.class);
    }
}
