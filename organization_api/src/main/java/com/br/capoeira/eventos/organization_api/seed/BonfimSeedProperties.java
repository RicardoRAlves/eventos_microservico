package com.br.capoeira.eventos.organization_api.seed;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.initial-data-seed")
public class BonfimSeedProperties {

    private boolean enabled;
    private String resource;
    private String adminName;
    private String adminEmail;
    private String adminPassword;
    private String adminAvatarUrl;
    private String organizationName;
    private String organizationLogoUrl;
    private String organizationUnitName;
    private String organizationUnitContactPhone;
}
