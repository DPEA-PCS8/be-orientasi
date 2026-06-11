package com.pcs8.orientasi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SsoConfig {

    @Bean("ssoRestTemplate")
    public RestTemplate ssoRestTemplate() {
        return new RestTemplate();
    }
}
