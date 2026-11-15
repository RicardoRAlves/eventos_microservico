package unit.com.br.capoeira.eventos.organization_api.seed;

import com.br.capoeira.eventos.organization_api.seed.BonfimSeedRunner;
import com.br.capoeira.eventos.organization_api.seed.BonfimSeedService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BonfimSeedProfileTest {

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
                .withBean(BonfimSeedService.class, () -> mock(BonfimSeedService.class))
                .withUserConfiguration(BonfimSeedRunner.class);

        runner.run(context -> {
            if (expected) {
                assertThat(context).hasSingleBean(BonfimSeedRunner.class);
            } else {
                assertThat(context).doesNotHaveBean(BonfimSeedRunner.class);
            }
        });
    }
}
