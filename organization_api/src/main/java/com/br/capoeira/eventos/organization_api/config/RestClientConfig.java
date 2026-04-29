package com.br.capoeira.eventos.organization_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${user.api.url.base}")
    private String organizationApiUrl;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(organizationApiUrl)
                .build();
    }
}