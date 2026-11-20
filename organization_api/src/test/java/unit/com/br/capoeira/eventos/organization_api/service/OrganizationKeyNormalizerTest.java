package unit.com.br.capoeira.eventos.organization_api.service;

import com.br.capoeira.eventos.organization_api.service.OrganizationKeyNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrganizationKeyNormalizerTest {

    private OrganizationKeyNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new OrganizationKeyNormalizer();
    }

    @ParameterizedTest
    @MethodSource("validNames")
    void shouldNormalizeOrganizationName(String name, String expectedKey) {
        assertThat(normalizer.normalize(name)).isEqualTo(expectedKey);
    }

    private static Stream<Arguments> validNames() {
        return Stream.of(
                Arguments.of(
                        "Associação de Capoeira Nosso Senhor do Bonfim",
                        "associacao-de-capoeira-nosso-senhor-do-bonfim"
                ),
                Arguments.of("Bônfim Capoeira", "bonfim-capoeira"),
                Arguments.of("BONFIM CAPOEIRA", "bonfim-capoeira"),
                Arguments.of("Grupo Raízes", "grupo-raizes"),
                Arguments.of("Ação & Cultura", "acao-cultura"),
                Arguments.of("  Grupo   Raízes  ", "grupo-raizes"),
                Arguments.of("Cultura...Arte___Brasil", "cultura-arte-brasil"),
                Arguments.of("Ａｃ̧ａ̃ｏ Cultural", "acao-cultural"),
                Arguments.of("Ac\u0327a\u0303o Cultural", "acao-cultural")
        );
    }

    @Test
    void shouldRejectNullName() {
        assertThatThrownBy(() -> normalizer.normalize(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or blank");
    }

    @ParameterizedTest
    @MethodSource("blankNames")
    void shouldRejectBlankName(String name) {
        assertThatThrownBy(() -> normalizer.normalize(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or blank");
    }

    private static Stream<String> blankNames() {
        return Stream.of("", " ", "\t\n");
    }

    @Test
    void shouldRejectNameThatNormalizesToEmpty() {
        assertThatThrownBy(() -> normalizer.normalize("— & !!!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ASCII letter or digit");
    }

    @Test
    void shouldAcceptKeyAtMaximumLength() {
        String name = "a".repeat(OrganizationKeyNormalizer.MAX_KEY_LENGTH);

        assertThat(normalizer.normalize(name)).hasSize(OrganizationKeyNormalizer.MAX_KEY_LENGTH);
    }

    @Test
    void shouldRejectKeyOverMaximumLengthWithoutTruncatingIt() {
        String name = "a".repeat(OrganizationKeyNormalizer.MAX_KEY_LENGTH + 1);

        assertThatThrownBy(() -> normalizer.normalize(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(OrganizationKeyNormalizer.MAX_KEY_LENGTH));
    }
}
