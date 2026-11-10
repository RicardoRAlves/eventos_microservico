package unit.com.br.capoeira.eventos.event_api.seed;

import com.br.capoeira.eventos.event_api.seed.CategorySeedRunner;
import com.br.capoeira.eventos.event_api.seed.CategorySeedService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CategorySeedRunnerTest {

    @Test
    void initializesSeedSynchronouslyDuringApplicationRunner() throws Exception {
        CategorySeedService service = mock(CategorySeedService.class);
        CategorySeedRunner runner = new CategorySeedRunner(service);

        runner.run(new DefaultApplicationArguments(new String[0]));

        verify(service).initialize();
    }
}
