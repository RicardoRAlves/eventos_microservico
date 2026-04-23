package com.br.capoeira.eventos.event_api.restClient;

import com.br.capoeira.eventos.event_api.config.exception.ServiceUnavailableException;
import com.br.capoeira.eventos.event_api.config.exception.ValidationException;
import com.br.capoeira.eventos.event_api.dto.OrganizationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;


@Component
@RequiredArgsConstructor
public class OrganizationClient {

    @Value("${organization.api.endpoint}")
    private String endpoint;

    private final RestClient restClient;

    public OrganizationResponseDto findUnitById(Long organizationUnitId) {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();

            final String finalToken = (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)
                    ? jwtAuthenticationToken.getToken().getTokenValue()
                    : null;

            return restClient.get()
                    .uri(endpoint + "{organizationUnitId}", organizationUnitId)
                    .headers(headers -> {
                        if (finalToken != null) {
                            headers.setBearerAuth(finalToken);
                        }
                    })
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new ValidationException("Invalid Organization Unit Id");
                            }
                    )
                    .onStatus(
                            status -> status.value() == 401,
                            (request, response) -> {
                                throw new ValidationException("Unauthorized to validate Organization Unit Id");
                            }
                    )
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (request, response) -> {
                                throw new ValidationException(
                                        "Error validating Organization Unit Id. HTTP status: "
                                                + response.getStatusCode().value()
                                );
                            }
                    )
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (request, response) -> {
                                throw new ServiceUnavailableException("Organization service is unavailable");
                            }
                    )
                    .body(OrganizationResponseDto.class);

        } catch (ResourceAccessException ex) {
            throw new ServiceUnavailableException("Organization service is unavailable");
        }
    }
}