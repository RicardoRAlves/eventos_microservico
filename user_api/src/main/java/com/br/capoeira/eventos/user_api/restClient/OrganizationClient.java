package com.br.capoeira.eventos.user_api.restClient;

import com.br.capoeira.eventos.user_api.config.exception.ValidationException;
import com.br.capoeira.eventos.user_api.dto.OrganizationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class OrganizationClient {

    @Value("${organization.api.endpoint}")
    private String endpoint;

    private final RestClient restClient;

    public OrganizationResponseDto getByJoinCode(String joinCode) {
        try {
            return restClient.get()
                    .uri(endpoint + "{joinCode}", joinCode)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new ValidationException("Invalid join code");
                            }
                    )
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            (request, response) -> {
                                throw new ValidationException("Error validating join code");
                            }
                    )
                    .body(OrganizationResponseDto.class);
        } catch (ResourceAccessException ex) {
            throw new ValidationException("Organization service is unavailable");
        }
    }
}