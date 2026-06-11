package com.pcs8.orientasi.client.sso;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Slf4j
@Component
public class SsoTokenClient {

    private final RestTemplate restTemplate;

    @Value("${sso.base-url}")
    private String baseUrl;

    private static final String TOKEN_PATH = "/connect/token";

    @Value("${sso.client-id}")
    private String clientId;

    @Value("${sso.client-secret}")
    private String clientSecret;

    @Value("${sso.token-buffer-seconds}")
    private int tokenBufferSeconds;

    private volatile String cachedToken;
    private volatile Instant tokenExpiresAt = Instant.MIN;

    public SsoTokenClient(@Qualifier("ssoRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public synchronized String getToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiresAt)) {
            return cachedToken;
        }
        return fetchNewToken();
    }

    private String fetchNewToken() {
        String tokenUrl = baseUrl + TOKEN_PATH;
        log.info("Fetching new SSO token from {}", tokenUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<SsoTokenResponse> response = restTemplate.postForEntity(
                tokenUrl, request, SsoTokenResponse.class);

        SsoTokenResponse tokenResponse = response.getBody();
        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new IllegalStateException("SSO token response is empty");
        }

        cachedToken = tokenResponse.getAccessToken();
        tokenExpiresAt = Instant.now().plusSeconds(tokenResponse.getExpiresIn() - tokenBufferSeconds);

        log.info("SSO token fetched, expires in {}s (buffered {}s)", tokenResponse.getExpiresIn(), tokenBufferSeconds);
        return cachedToken;
    }
}
