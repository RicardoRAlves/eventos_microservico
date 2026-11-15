package com.br.capoeira.eventos.organization_api.controller;

import com.br.capoeira.eventos.organization_api.config.exception.OrganizationKeyConflictException;
import com.br.capoeira.eventos.organization_api.dto.PromoteToSuperAdminDtoRequest;
import com.br.capoeira.eventos.organization_api.dto.UserResponseDto;
import com.br.capoeira.eventos.organization_api.repository.OrganizationRepository;
import com.br.capoeira.eventos.organization_api.repository.OrganizationUnitRepository;
import com.br.capoeira.eventos.organization_api.restClient.UserClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static utils.MockUtils.getMockOrganizationCreateRequestDto;

@SpringBootTest(properties = {
        "security.jwt.secret=bG9jYWwtdGVzdC1qd3Qtc2VjcmV0LWxvY2FsLXRlc3Qtand0LXNlY3JldC0xMjM0NTY3ODkw",
        "aws.access_key_id=test",
        "aws.secret_access_key=test",
        "s3.bucket=test",
        "s3.region=us-east-1",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "logging.file.name=target/organization-key-concurrency.log"
})
@AutoConfigureMockMvc
@Testcontainers
class OrganizationKeyConcurrencyIntegrationTest {

    private static final String EXPECTED_KEY = "grupo-raizes";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("organization_concurrency_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationUnitRepository organizationUnitRepository;

    @MockitoBean
    private UserClient userClient;

    @AfterEach
    void cleanDatabase() {
        organizationUnitRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void shouldReturnConflictForOneOfTwoConcurrentEquivalentNames() throws Exception {
        when(userClient.promoteToSuperAdmin(any(PromoteToSuperAdminDtoRequest.class)))
                .thenAnswer(invocation -> {
                    var input = invocation.getArgument(0, PromoteToSuperAdminDtoRequest.class);
                    var response = new UserResponseDto();
                    response.setId(input.userId());
                    response.setOrganizationId(input.organizationId());
                    response.setOrganizationUnitId(input.organizationUnitId());
                    return response;
                });

        var firstRequest = getMockOrganizationCreateRequestDto();
        firstRequest.setName("Grupo Raízes");
        var secondRequest = getMockOrganizationCreateRequestDto();
        secondRequest.setName("  GRUPO   RAIZES  ");

        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);

        try {
            Callable<MvcResult> firstCall = () -> performCreate(firstRequest, start);
            Callable<MvcResult> secondCall = () -> performCreate(secondRequest, start);
            var firstFuture = executor.submit(firstCall);
            var secondFuture = executor.submit(secondCall);
            start.countDown();

            var firstResult = firstFuture.get(30, TimeUnit.SECONDS);
            var secondResult = secondFuture.get(30, TimeUnit.SECONDS);
            var results = List.of(firstResult, secondResult);

            assertThat(results)
                    .extracting(result -> result.getResponse().getStatus())
                    .containsExactlyInAnyOrder(201, 409);

            var conflictResult = results.stream()
                    .filter(result -> result.getResponse().getStatus() == 409)
                    .findFirst()
                    .orElseThrow();
            var conflictBody = objectMapper.readTree(conflictResult.getResponse().getContentAsString());
            assertThat(conflictBody.get("error").asText())
                    .isEqualTo(OrganizationKeyConflictException.ERROR_CODE);

            assertThat(organizationRepository.countByOrganizationKey(EXPECTED_KEY)).isEqualTo(1);
            assertThat(organizationRepository.count()).isEqualTo(1);
            verify(userClient, times(1)).promoteToSuperAdmin(any(PromoteToSuperAdminDtoRequest.class));
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private MvcResult performCreate(
            com.br.capoeira.eventos.organization_api.dto.OrganizationCreateRequestDto request,
            CountDownLatch start) throws Exception {
        if (!start.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Concurrent requests did not start in time");
        }

        return mockMvc.perform(post("/api/v1/organizacao")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();
    }
}
