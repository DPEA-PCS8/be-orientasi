package com.pcs8.orientasi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
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
            return new RestTemplate();
        }
        return new RestTemplate(trustAllRequestFactory());
    }

    private SimpleClientHttpRequestFactory trustAllRequestFactory() throws Exception {
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
        final SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        final HostnameVerifier allowAllHosts = (hostname, session) -> true;

        return new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                if (connection instanceof HttpsURLConnection httpsConnection) {
                    httpsConnection.setSSLSocketFactory(socketFactory);
                    httpsConnection.setHostnameVerifier(allowAllHosts);
                }
                super.prepareConnection(connection, httpMethod);
            }
        };
    }
}
