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
 * <p>NOTE: {@code clientId}/{@code clientSecret} here are the LOGIN (confidential) client
 * used for the authorization-code exchange. The pre-existing {@code SsoTokenClient}
 * (catalog client_credentials) is a different concern and keeps reading
 * {@code sso.client-id}/{@code sso.client-secret} directly via @Value. To avoid breaking it,
 * the login client is bound from dedicated keys {@code sso.login-client-id} /
 * {@code sso.login-client-secret} (see {@code application.yaml}).
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "sso")
public class SsoOAuthProperties {

    /** SSO base URL, e.g. http://auth-web.sso-engine-dev.svc.cluster.local */
    private String baseUrl;

    /** Login (confidential) client id used for the authorization-code flow. */
    private String loginClientId;

    /** Login (confidential) client secret used for the authorization-code flow. */
    private String loginClientSecret;

    /** Redirect URI registered with the SSO; this is the FE callback. Must match across authorize/token/registration. */
    private String redirectUri;

    /** Frontend callback URL (same as redirectUri in this BFF setup). */
    private String frontendCallbackUrl;

    /** Space-delimited OIDC scopes, e.g. "openid email profile roles". */
    private String scopes;

    /** Token buffer in seconds (shared with catalog client config). */
    private int tokenBufferSeconds;
}
