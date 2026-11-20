package com.br.capoeira.eventos.organization_api.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
@Profile("!prod & !production & (local | homolog | homologacao)")
@ConditionalOnProperty(
        prefix = "app.initial-data-seed",
        name = "enabled",
        havingValue = "true"
)
public class BonfimSeedRunner implements ApplicationRunner {

    private final BonfimSeedService seedService;

    @Override
    public void run(ApplicationArguments args) {
        BonfimSeedResult result = seedService.seed();
        log.info(
                "Bonfim seed completed. organizationCreated={}, unitCreated={}, userLinkUpdated={}",
                result.organizationCreated(),
                result.organizationUnitCreated(),
                result.userLinkUpdated()
        );
    }
}
