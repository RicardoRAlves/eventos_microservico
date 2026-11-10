package com.br.capoeira.eventos.event_api.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class CategorySeedRunner implements ApplicationRunner {

    private final CategorySeedService categorySeedService;

    @Override
    public void run(ApplicationArguments args) {
        categorySeedService.initialize();
    }
}
