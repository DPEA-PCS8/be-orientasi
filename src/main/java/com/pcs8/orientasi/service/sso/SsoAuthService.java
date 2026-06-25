package com.pcs8.orientasi.service.sso;

import com.pcs8.orientasi.client.sso.SsoTokenResponse;
import com.pcs8.orientasi.domain.dto.response.LoginResponse.UserInfo;

/**
 * SSO OIDC authorization-code (BFF) flow operations.
 */
public interface SsoAuthService {

    /**
     * Build the {@code /connect/authorize} redirect URL for the given state and S256 code challenge.
     */
    String buildAuthorizeUrl(String state, String codeChallenge);

    /**
     * Exchange an authorization code (with the matching PKCE verifier) for tokens at {@code /connect/token}.
     */
    SsoTokenResponse exchangeCode(String code, String codeVerifier);

    /**
     * Fetch user info from {@code /connect/userinfo} using the access token and map it to the app UserInfo.
     */
    UserInfo fetchUserInfo(String accessToken);

    /**
     * Build the {@code /connect/logout} (end_session) redirect URL that terminates the SSO session
     * and returns the browser to the configured post-logout redirect URI.
     */
    String buildLogoutUrl();
}
