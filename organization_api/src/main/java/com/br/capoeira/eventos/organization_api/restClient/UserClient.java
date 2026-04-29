package com.br.capoeira.eventos.organization_api.restClient;

import com.br.capoeira.eventos.organization_api.config.exception.ServiceUnavailableException;
import com.br.capoeira.eventos.organization_api.config.exception.ValidationException;
import com.br.capoeira.eventos.organization_api.dto.PromoteToSuperAdminDtoRequest;
import com.br.capoeira.eventos.organization_api.dto.UserResponseDto;
import com.br.capoeira.eventos.organization_api.service.InternalServiceTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class UserClient {

    @Value("${user.api.endpoint.change-role}")
    private String changeRoleEndpoint;

    private final RestClient restClient;
    private final InternalServiceTokenProvider internalServiceTokenProvider;

    public UserResponseDto promoteToSuperAdmin(PromoteToSuperAdminDtoRequest requestDto) {
        try {
            String serviceToken = internalServiceTokenProvider.generateServiceToken();

            return restClient.patch()
                    .uri(changeRoleEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(serviceToken))
                    .body(requestDto)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new ValidationException("User not found");
                            }
                    )
                    .onStatus(
                            status -> status.value() == 401 || status.value() == 403,
                            (request, response) -> {
                                throw new ValidationException("Unauthorized internal user role change");
                            }
                    )
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (request, response) -> {
                                throw new ValidationException(
                                        "Error promoting user to super admin. HTTP status: "
                                                + response.getStatusCode().value()
                                );
                            }
                    )
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (request, response) -> {
                                throw new ServiceUnavailableException("User service is unavailable");
                            }
                    )
                    .body(UserResponseDto.class);

        } catch (ResourceAccessException ex) {
            throw new ServiceUnavailableException("User service is unavailable");
        }
    }
}