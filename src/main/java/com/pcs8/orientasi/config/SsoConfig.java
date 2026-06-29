package com.pcs8.orientasi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Configuration
public class SsoConfig {

    /**
     * DEV-ONLY toggle. The internal SSO (https://auth-web-sso-engine-dev...) presents a cert
     * issued by an internal CA not present in the JVM truststore, causing
     * "PKIX path building failed" on /connect/token. Enable trust-all locally via
     * sso.trust-all-certs=true (env SSO_TRUST_ALL_CERTS=true).
     *
     * <p>NEVER set true in prod. Proper prod fix: import the SSO CA cert into the JVM truststore.
     */
    @Value("${sso.trust-all-certs:false}")
    private boolean trustAllCerts;

    /**
     * RestTemplate used for all SSO calls (catalog client_credentials + OIDC login flow).
     */
    @Bean("ssoRestTemplate")
    public RestTemplate ssoRestTemplate() throws Exception {
        if (!trustAllCerts) {
            return new RestTemplate(new JdkClientHttpRequestFactory());
        }
        return new RestTemplate(trustAllRequestFactory());
    }

    // Sonar S4830 / S5527: server cert validation is intentionally disabled here, but ONLY when
    // sso.trust-all-certs=true (default false → this code path never runs in prod). It exists solely
    // so local dev can reach the internal-CA SSO without importing its cert. Prod must keep the flag
    // false and import the SSO CA into the JVM truststore instead.
    @SuppressWarnings({"java:S4830", "java:S5527"})
    private JdkClientHttpRequestFactory trustAllRequestFactory() throws Exception {
        TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        // dev: accept any client cert
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        // dev: accept any server cert
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAll, new SecureRandom());

        SSLParameters sslParameters = new SSLParameters();
        sslParameters.setEndpointIdentificationAlgorithm("");

        HttpClient httpClient = HttpClient.newBuilder()
                .sslContext(sslContext)
                .sslParameters(sslParameters)
                .build();

        return new JdkClientHttpRequestFactory(httpClient);
    }
}
