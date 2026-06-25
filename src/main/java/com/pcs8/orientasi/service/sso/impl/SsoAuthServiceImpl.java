package com.pcs8.orientasi.service.sso.impl;

import com.pcs8.orientasi.client.sso.SsoTokenResponse;
import com.pcs8.orientasi.client.sso.dto.SsoUserInfoResponse;
import com.pcs8.orientasi.config.SsoOAuthProperties;
import com.pcs8.orientasi.domain.dto.response.LoginResponse.UserInfo;
import com.pcs8.orientasi.service.sso.SsoAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class SsoAuthServiceImpl implements SsoAuthService {

    private static final String AUTHORIZE_PATH = "/connect/authorize";
    private static final String TOKEN_PATH = "/connect/token";
    private static final String USERINFO_PATH = "/connect/userinfo";
    private static final String LOGOUT_PATH = "/connect/logout";

    private final RestTemplate ssoRestTemplate;
    private final SsoOAuthProperties props;

    public SsoAuthServiceImpl(@Qualifier("ssoRestTemplate") RestTemplate ssoRestTemplate,
                              SsoOAuthProperties props) {
        this.ssoRestTemplate = ssoRestTemplate;
        this.props = props;
    }

    @Override
    public String buildAuthorizeUrl(String state, String codeChallenge) {
        return UriComponentsBuilder.fromHttpUrl(props.getBaseUrl())
                .path(AUTHORIZE_PATH)
                .queryParam("response_type", "code")
                .queryParam("client_id", props.getClientId())
                .queryParam("redirect_uri", props.getRedirectUri())
                .queryParam("scope", props.getScopes())
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .encode()
                .toUriString();
    }

    @Override
    public String buildLogoutUrl() {
        return UriComponentsBuilder.fromHttpUrl(props.getBaseUrl())
                .path(LOGOUT_PATH)
                .queryParam("client_id", props.getClientId())
                .queryParam("post_logout_redirect_uri", props.getPostLogoutRedirectUri())
                .encode()
                .toUriString();
    }

    @Override
    public SsoTokenResponse exchangeCode(String code, String codeVerifier) {
        String tokenUrl = props.getBaseUrl() + TOKEN_PATH;
        log.info("Exchanging authorization code at {}", tokenUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", props.getRedirectUri());
        body.add("client_id", props.getClientId());
        body.add("client_secret", props.getClientSecret());
        body.add("code_verifier", codeVerifier);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<SsoTokenResponse> response =
                ssoRestTemplate.postForEntity(tokenUrl, request, SsoTokenResponse.class);

        SsoTokenResponse tokenResponse = response.getBody();
        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new IllegalStateException("SSO token response is empty");
        }
        log.info("Authorization code exchanged successfully");
        return tokenResponse;
    }

    @Override
    public UserInfo fetchUserInfo(String accessToken) {
        String userInfoUrl = props.getBaseUrl() + USERINFO_PATH;
        log.info("Fetching userinfo from {}", userInfoUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<SsoUserInfoResponse> response = ssoRestTemplate.exchange(
                userInfoUrl, HttpMethod.GET, request, SsoUserInfoResponse.class);

        SsoUserInfoResponse info = response.getBody();
        if (info == null || info.getSub() == null) {
            throw new IllegalStateException("SSO userinfo response is empty or missing 'sub'");
        }

        return UserInfo.builder()
                .username(info.getSub())
                .fullName(info.getName())
                .displayName(info.getName())
                .email(info.getEmail())
                .title(info.getJabatan())
                .department(info.getOrganization())
                .build();
    }
}
