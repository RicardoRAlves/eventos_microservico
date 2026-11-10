package com.culturaconecta.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class AiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAgentApplication.class, args);
    }

    @Configuration
    public static class AppConfig {

        @Bean
        @ConfigurationProperties(prefix = "ai")
        public OllamaModelProperties ollamaModelProperties(Environment env) {
            return new OllamaModelProperties(env);
        }
    }
}
