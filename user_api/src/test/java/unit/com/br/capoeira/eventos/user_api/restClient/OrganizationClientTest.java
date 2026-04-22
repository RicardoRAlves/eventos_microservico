package unit.com.br.capoeira.eventos.user_api.restClient;

import com.br.capoeira.eventos.user_api.config.exception.ValidationException;
import com.br.capoeira.eventos.user_api.restClient.OrganizationClient;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OrganizationClientTest {

    private OrganizationClient client;
    private MockRestServiceServer server;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.build();
        request = Mockito.mock(HttpServletRequest.class);

        client = new OrganizationClient(restClient, request);

        ReflectionTestUtils.setField(
                client,
                "endpoint",
                "http://localhost/api/v1/organizations/join-code/"
        );

        Mockito.when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer test-token");
    }

    @Test
    void shouldReturnOrganizationWhenJoinCodeIsValid() {
        var json = """
            {
              "organizationId": 10,
              "id": 20
            }
            """;

        server.expect(requestTo("http://localhost/api/v1/organizations/join-code/JOIN123"))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andRespond(withSuccess(json, APPLICATION_JSON));

        var response = client.getByJoinCode("JOIN123");

        assertNotNull(response);
        assertEquals(10L, response.getOrganizationId());
        assertEquals(20L, response.getOrganizationUnitId());
    }

    @Test
    void shouldThrowValidationExceptionWhenJoinCodeIsInvalid() {
        server.expect(requestTo("http://localhost/api/v1/organizations/join-code/INVALID"))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andRespond(withStatus(NOT_FOUND)
                        .contentType(APPLICATION_JSON)
                        .body("{}"));

        var exception = assertThrows(
                ValidationException.class,
                () -> client.getByJoinCode("INVALID")
        );

        assertEquals("Invalid join code", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenApiReturnsServerError() {
        server.expect(requestTo("http://localhost/api/v1/organizations/join-code/JOIN123"))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andRespond(withStatus(INTERNAL_SERVER_ERROR)
                        .contentType(APPLICATION_JSON)
                        .body("{}"));

        var exception = assertThrows(
                ValidationException.class,
                () -> client.getByJoinCode("JOIN123")
        );

        assertEquals(
                "Error validating join code. HTTP status: 500",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowValidationExceptionWhenAuthorizationHeaderIsMissing() {
        Mockito.when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn(null);

        var exception = assertThrows(
                ValidationException.class,
                () -> client.getByJoinCode("JOIN123")
        );

        assertEquals("Authorization header not found", exception.getMessage());
    }
}