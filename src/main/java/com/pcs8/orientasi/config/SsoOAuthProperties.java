package com.pcs8.orientasi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the SSO OIDC login flow (BFF / Authorization Code + PKCE).
 *
 * <p>Bound from the {@code sso:} block in application.yaml.
 *
 * <p>NOTE: a single confidential client ({@code sso.client-id}/{@code sso.client-secret})
 * is used for BOTH the authorization-code login flow (here) AND the catalog
 * client_credentials flow ({@code SsoTokenClient}). The client is dual-grant on the SSO side.
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "sso")
public class SsoOAuthProperties {

    /** SSO base URL, e.g. http://auth-web.sso-engine-dev.svc.cluster.local */
    private String baseUrl;

    /** Confidential client id (dual-grant: authorization_code + client_credentials). */
    private String clientId;

    /** Confidential client secret. */
    private String clientSecret;

    /** Redirect URI registered with the SSO; this is the FE callback. Must match across authorize/token/registration. */
    private String redirectUri;

    /** Frontend callback URL (same as redirectUri in this BFF setup). */
    private String frontendCallbackUrl;

    /** Space-delimited OIDC scopes, e.g. "openid email profile roles". */
    private String scopes;

    /** Token buffer in seconds (shared with catalog client config). */
    private int tokenBufferSeconds;
}
