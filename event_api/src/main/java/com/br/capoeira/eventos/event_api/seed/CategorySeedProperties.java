package com.br.capoeira.eventos.event_api.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app.category-seed")
public class CategorySeedProperties {

    private boolean enabled = true;
    private String resource = "classpath:seed/categories.json";
    private Duration lockDuration = Duration.ofSeconds(30);
    private Duration lockWaitTimeout = Duration.ofSeconds(60);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Duration getLockDuration() {
        return lockDuration;
    }

    public void setLockDuration(Duration lockDuration) {
        this.lockDuration = lockDuration;
    }

    public Duration getLockWaitTimeout() {
        return lockWaitTimeout;
    }

    public void setLockWaitTimeout(Duration lockWaitTimeout) {
        this.lockWaitTimeout = lockWaitTimeout;
    }
}
