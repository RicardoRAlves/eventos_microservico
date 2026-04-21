package com.br.capoeira.eventos.user_api.restClient;

import com.br.capoeira.eventos.user_api.config.exception.ValidationException;
import com.br.capoeira.eventos.user_api.dto.OrganizationResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class OrganizationClient {

    private final RestClient restClient;
    private final HttpServletRequest request;

    @Value("${organization.api.endpoint}")
    private String endpoint;

    public OrganizationResponseDto getByJoinCode(String joinCode) {
        try {
            return restClient.get()
                    .uri(endpoint + "{joinCode}", joinCode)
                    .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader())
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (req, response) -> {
                                throw new ValidationException("Invalid join code");
                            }
                    )
                    .onStatus(
                            status -> status.value() == 401,
                            (req, response) -> {
                                throw new ValidationException("Unauthorized to access organization service");
                            }
                    )
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            (req, response) -> {
                                throw new ValidationException(
                                        "Error validating join code. HTTP status: " + response.getStatusCode().value()
                                );
                            }
                    )
                    .body(OrganizationResponseDto.class);
        } catch (ResourceAccessException ex) {
            throw new ValidationException("Organization service is unavailable");
        }
    }

    private String getAuthorizationHeader() {
        var authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || authorization.isBlank()) {
            throw new ValidationException("Authorization header not found");
        }

        return authorization;
    }
}