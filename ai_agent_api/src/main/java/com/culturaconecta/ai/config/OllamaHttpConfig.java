package com.culturaconecta.ai.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class OllamaHttpConfig {

    @Bean
    public RestClientCustomizer ollamaRestClientCustomizer() {

        return builder -> {
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .build();

            JdkClientHttpRequestFactory factory =
                    new JdkClientHttpRequestFactory(httpClient);

            factory.setReadTimeout(Duration.ofSeconds(60));

            builder.requestFactory(factory);
        };
    }
}

