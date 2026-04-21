package com.br.capoeira.eventos.event_api.restClient;

import com.br.capoeira.eventos.event_api.config.exception.ValidationException;
import com.br.capoeira.eventos.event_api.dto.OrganizationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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

            String token = null;
            if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
                token = jwtAuthenticationToken.getToken().getTokenValue();
            }

            return restClient.get()
                    .uri(endpoint + "{organizationUnitId}", organizationUnitId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            (request, response) -> {
                                throw new ValidationException(
                                        "Error validating Organization Unit Id. HTTP status: "
                                                + response.getStatusCode().value()
                                );
                            }
                    )
                    .body(OrganizationResponseDto.class);
        } catch (ResourceAccessException ex) {
            throw new ValidationException("Organization service is unavailable");
        }
    }
}