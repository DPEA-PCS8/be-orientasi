package com.pcs8.orientasi.client.catalog;

import com.pcs8.orientasi.client.catalog.dto.changelog.CatalogChangelogRequest;
import com.pcs8.orientasi.client.catalog.dto.changelog.CatalogChangelogResponse;
import com.pcs8.orientasi.client.catalog.dto.common.CatalogBaseResponse;
import com.pcs8.orientasi.client.catalog.dto.snapshot.CatalogBatchGenerateRequest;
import com.pcs8.orientasi.client.catalog.dto.snapshot.CatalogBatchGenerateResponse;
import com.pcs8.orientasi.client.catalog.dto.snapshot.CatalogSnapshotStatistikResponse;
import com.pcs8.orientasi.client.catalog.dto.snapshot.CatalogSnapshotUpdateRequest;
import com.pcs8.orientasi.client.catalog.dto.snapshot.CatalogYearSnapshotListItem;
import com.pcs8.orientasi.client.catalog.dto.snapshot.CatalogYearSnapshotRequest;
import com.pcs8.orientasi.client.catalog.dto.snapshot.CatalogYearSnapshotResponse;
import com.pcs8.orientasi.client.sso.SsoTokenClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class CatalogSnapshotClient {

    private final SsoTokenClient ssoTokenClient;
    private final RestTemplate restTemplate;

    public CatalogSnapshotClient(SsoTokenClient ssoTokenClient,
                                 @Qualifier("ssoRestTemplate") RestTemplate restTemplate) {
        this.ssoTokenClient = ssoTokenClient;
        this.restTemplate = restTemplate;
    }

    @Value("${catalog.base-url}")
    private String baseUrl;

    private static final String APLIKASI_PATH = "/api/aplikasi";
    private static final String SNAPSHOTS_PATH = "/api/aplikasi/snapshots";

    // ── YEAR SNAPSHOT CRUD ───────────────────────────────────────────────────

    public CatalogYearSnapshotResponse createOrUpdateYearSnapshot(UUID aplikasiId, CatalogYearSnapshotRequest request) {
        ResponseEntity<CatalogBaseResponse<CatalogYearSnapshotResponse>> response =
                restTemplate.exchange(
                        baseUrl + APLIKASI_PATH + "/" + aplikasiId + "/snapshots/year",
                        HttpMethod.POST,
                        new HttpEntity<>(request, buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    public CatalogBatchGenerateResponse batchGenerate(CatalogBatchGenerateRequest request) {
        ResponseEntity<CatalogBaseResponse<CatalogBatchGenerateResponse>> response =
                restTemplate.exchange(
                        baseUrl + SNAPSHOTS_PATH + "/year/generate",
                        HttpMethod.POST,
                        new HttpEntity<>(request, buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    public CatalogYearSnapshotResponse getByAplikasiAndTahun(UUID aplikasiId, Integer tahun) {
        ResponseEntity<CatalogBaseResponse<CatalogYearSnapshotResponse>> response =
                restTemplate.exchange(
                        baseUrl + APLIKASI_PATH + "/" + aplikasiId + "/snapshots/year/" + tahun,
                        HttpMethod.GET,
                        new HttpEntity<>(buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    public CatalogYearSnapshotResponse getById(UUID snapshotId) {
        ResponseEntity<CatalogBaseResponse<CatalogYearSnapshotResponse>> response =
                restTemplate.exchange(
                        baseUrl + SNAPSHOTS_PATH + "/" + snapshotId,
                        HttpMethod.GET,
                        new HttpEntity<>(buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    public List<CatalogYearSnapshotListItem> listByTahun(Integer tahun) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + SNAPSHOTS_PATH + "/year")
                .queryParam("tahun", tahun)
                .toUriString();

        ResponseEntity<CatalogBaseResponse<List<CatalogYearSnapshotListItem>>> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    public List<CatalogYearSnapshotListItem> listByAplikasi(UUID aplikasiId) {
        ResponseEntity<CatalogBaseResponse<List<CatalogYearSnapshotListItem>>> response =
                restTemplate.exchange(
                        baseUrl + APLIKASI_PATH + "/" + aplikasiId + "/snapshots/year",
                        HttpMethod.GET,
                        new HttpEntity<>(buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    public List<Integer> getAvailableYears() {
        ResponseEntity<CatalogBaseResponse<List<Integer>>> response =
                restTemplate.exchange(
                        baseUrl + SNAPSHOTS_PATH + "/year/available-years",
                        HttpMethod.GET,
                        new HttpEntity<>(buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    public CatalogSnapshotStatistikResponse getStatistik(Integer tahun) {
        ResponseEntity<CatalogBaseResponse<CatalogSnapshotStatistikResponse>> response =
                restTemplate.exchange(
                        baseUrl + SNAPSHOTS_PATH + "/year/" + tahun + "/statistik",
                        HttpMethod.GET,
                        new HttpEntity<>(buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    public CatalogYearSnapshotResponse update(UUID snapshotId, CatalogSnapshotUpdateRequest request) {
        ResponseEntity<CatalogBaseResponse<CatalogYearSnapshotResponse>> response =
                restTemplate.exchange(
                        baseUrl + SNAPSHOTS_PATH + "/" + snapshotId,
                        HttpMethod.PUT,
                        new HttpEntity<>(request, buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    public void delete(UUID snapshotId) {
        restTemplate.exchange(
                baseUrl + SNAPSHOTS_PATH + "/" + snapshotId,
                HttpMethod.DELETE,
                new HttpEntity<>(buildAuthHeaders()),
                Void.class);
    }

    // ── CHANGELOG ────────────────────────────────────────────────────────────

    public CatalogChangelogResponse addChangelog(UUID snapshotId, CatalogChangelogRequest request) {
        ResponseEntity<CatalogBaseResponse<CatalogChangelogResponse>> response =
                restTemplate.exchange(
                        baseUrl + SNAPSHOTS_PATH + "/" + snapshotId + "/changelog",
                        HttpMethod.POST,
                        new HttpEntity<>(request, buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    public List<CatalogChangelogResponse> getChangelogs(UUID snapshotId) {
        ResponseEntity<CatalogBaseResponse<List<CatalogChangelogResponse>>> response =
                restTemplate.exchange(
                        baseUrl + SNAPSHOTS_PATH + "/" + snapshotId + "/changelogs",
                        HttpMethod.GET,
                        new HttpEntity<>(buildAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        return extractData(response);
    }

    public void deleteChangelog(UUID changelogId) {
        restTemplate.exchange(
                baseUrl + SNAPSHOTS_PATH + "/changelogs/" + changelogId,
                HttpMethod.DELETE,
                new HttpEntity<>(buildAuthHeaders()),
                Void.class);
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private HttpHeaders buildAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(ssoTokenClient.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private <T> T extractData(ResponseEntity<CatalogBaseResponse<T>> response) {
        CatalogBaseResponse<T> body = response.getBody();
        if (body == null || body.getData() == null) {
            throw new IllegalStateException("Empty response from catalog snapshot service");
        }
        return body.getData();
    }
}
