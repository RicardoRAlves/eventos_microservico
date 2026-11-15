package com.br.capoeira.eventos.user_api.controller;

import com.br.capoeira.eventos.user_api.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class InitialAdminSeedProfileTest {
    @Test
    void shouldEnableOnlyExplicitlyAllowedProfilesWhenFlagIsTrue() {
        assertRegistered(true, true, "local");
        assertRegistered(true, true, "homolog");
        assertRegistered(true, true, "homologacao");
    }

    @Test
    void shouldRejectDefaultProductionUnknownAndMixedProductionProfiles() {
        assertRegistered(false, true);
        assertRegistered(false, true, "default");
        assertRegistered(false, true, "prod");
        assertRegistered(false, true, "production");
        assertRegistered(false, true, "staging");
        assertRegistered(false, true, "local", "prod");
    }

    @Test
    void shouldRejectAllowedProfileWhenFlagIsFalse() {
        assertRegistered(false, false, "local");
    }

    private void assertRegistered(boolean expected, boolean enabled, String... profiles) {
        var runner = new ApplicationContextRunner()
                .withInitializer(context -> context.getEnvironment().setActiveProfiles(profiles))
                .withPropertyValues("app.initial-data-seed.enabled=" + enabled)
                .withBean(UserService.class, () -> mock(UserService.class))
                .withUserConfiguration(InitialAdminSeedController.class);
        runner.run(context -> {
            if (expected) assertThat(context).hasSingleBean(InitialAdminSeedController.class);
            else assertThat(context).doesNotHaveBean(InitialAdminSeedController.class);
        });
    }
}
