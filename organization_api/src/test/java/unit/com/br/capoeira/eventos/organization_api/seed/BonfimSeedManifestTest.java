package unit.com.br.capoeira.eventos.organization_api.seed;

import com.br.capoeira.eventos.organization_api.seed.BonfimSeedManifestLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class BonfimSeedManifestTest {

    private final BonfimSeedManifestLoader loader = new BonfimSeedManifestLoader(
            new ObjectMapper(),
            new DefaultResourceLoader()
    );

    @Test
    void shouldContainOnlyTheApprovedBonfimAndJundiaiHistoricalData() {
        var manifest = loader.load("classpath:seed/bonfim-seed.json");

        assertThat(manifest.source()).isEqualTo("Dossie_Bonfim.pdf");
        assertThat(manifest.organization().name())
                .isEqualTo("Associação de Capoeira Nosso Senhor do Bonfim");
        assertThat(manifest.organization().expectedKey())
                .isEqualTo("associacao-de-capoeira-nosso-senhor-do-bonfim");
        assertThat(manifest.organization().description()).isEqualTo(
                "A Associação de Capoeira Nosso Senhor do Bonfim reúne uma trajetória de preservação "
                        + "e transmissão da capoeira, conectando mestres, professores e praticantes por meio "
                        + "do ensino, da musicalidade, das rodas e dos encontros culturais. Sua história está "
                        + "ligada ao trabalho de Reginaldo Santana, Grão Mestre Régis, com origem em Passos, "
                        + "Minas Gerais, e atuação documentada em diferentes localidades brasileiras. A associação "
                        + "valoriza a memória, a formação de novas gerações e a continuidade das tradições "
                        + "afro-brasileiras."
        );

        assertThat(manifest.unit().name()).isEqualTo("Jundiai - Mestre Kaue");
        assertThat(manifest.unit().description()).isEqualTo(
                "Núcleo da Associação de Capoeira Nosso Senhor do Bonfim em Jundiaí, São Paulo, sob a "
                        + "responsabilidade de Carlos Alberto da Silva, Mestre Kauê. Com atividades documentadas "
                        + "desde 2 de fevereiro de 1990, desenvolve um trabalho dedicado ao ensino da capoeira, "
                        + "à musicalidade, à formação de crianças e jovens, às rodas, aos batizados e à participação "
                        + "em eventos culturais e esportivos, contribuindo para a preservação e a transmissão dessa "
                        + "tradição entre gerações."
        );
        assertThat(manifest.unit().city()).isEqualTo("Jundiaí");
        assertThat(manifest.unit().state()).isEqualTo("SP");
        assertThat(manifest.unit().country()).isEqualTo("Brasil");
        assertThat(manifest.unit().address()).isEqualTo("Rua José Dainese, 105 — Vila Municipal");
        assertThat(manifest.unit().contactEmail()).isEqualTo("capoeirabonfimjundiai@gmail.com");
        assertThat(manifest.unit().documentedResponsible())
                .isEqualTo("Carlos Alberto da Silva — Mestre Kauê");
        assertThat(manifest.unit().contactDataRequiresCurrentConfirmation()).isTrue();
    }
}
