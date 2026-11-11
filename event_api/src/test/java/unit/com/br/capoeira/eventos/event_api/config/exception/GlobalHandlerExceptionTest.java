package unit.com.br.capoeira.eventos.event_api.config.exception;

import com.br.capoeira.eventos.event_api.config.exception.GlobalHandlerException;
import com.br.capoeira.eventos.event_api.config.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalHandlerExceptionTest {

    @Test
    void categoryNotFoundKeepsHttp404Contract() {
        GlobalHandlerException handler = new GlobalHandlerException();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/event");

        var response = handler.handleValidationException(
                new ValidationException("Category not found"),
                request
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
    }
}
