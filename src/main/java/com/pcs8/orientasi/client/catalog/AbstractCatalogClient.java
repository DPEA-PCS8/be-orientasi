package com.pcs8.orientasi.client.catalog;

import com.pcs8.orientasi.client.catalog.dto.common.CatalogBaseResponse;
import com.pcs8.orientasi.client.sso.SsoTokenClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

abstract class AbstractCatalogClient {

    protected final SsoTokenClient ssoTokenClient;
    protected final RestTemplate restTemplate;

    protected AbstractCatalogClient(SsoTokenClient ssoTokenClient, RestTemplate restTemplate) {
        this.ssoTokenClient = ssoTokenClient;
        this.restTemplate = restTemplate;
    }

    protected HttpHeaders buildAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(ssoTokenClient.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected <T> T extractData(ResponseEntity<CatalogBaseResponse<T>> response) {
        CatalogBaseResponse<T> body = response.getBody();
        if (body == null || body.getData() == null) {
            throw new IllegalStateException("Empty response from catalog service");
        }
        return body.getData();
    }
}
