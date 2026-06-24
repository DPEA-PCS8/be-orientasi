package com.pcs8.orientasi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SsoConfig {

    /**
     * RestTemplate used for all SSO calls (catalog client_credentials + OIDC login flow).
     *
     * <p>T10 (dev TLS trust): the configured {@code sso.base-url} default is
     * {@code http://auth-web.sso-engine-dev.svc.cluster.local} — plain HTTP, no TLS handshake.
     * Therefore NO trust-all/self-signed override is needed and none is added (avoids weakening
     * prod). If the SSO is later moved to HTTPS with a self-signed cert (e.g.
     * {@code https://localhost:7283}), add a dev-profile-only trust-all variant of this bean
     * guarded by {@code @Profile("dev")} — never enable trust-all in prod.
     */
    @Bean("ssoRestTemplate")
    public RestTemplate ssoRestTemplate() {
        return new RestTemplate();
    }
}
