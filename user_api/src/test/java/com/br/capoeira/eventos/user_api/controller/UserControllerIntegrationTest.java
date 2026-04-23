package com.br.capoeira.eventos.user_api.controller;

import com.br.capoeira.eventos.user_api.config.RestClientConfig;
import com.br.capoeira.eventos.user_api.config.filter.JwtAuthenticationFilter;
import com.br.capoeira.eventos.user_api.dto.*;
import com.br.capoeira.eventos.user_api.enums.Role;
import com.br.capoeira.eventos.user_api.restClient.OrganizationClient;
import com.br.capoeira.eventos.user_api.service.AuthenticationService;
import com.br.capoeira.eventos.user_api.service.CustomUserDetailsService;
import com.br.capoeira.eventos.user_api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService service;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private OrganizationClient organizationClient;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private RestClientConfig clientConfig;

    @Test
    void shouldFindUserByIdSuccessfully() throws Exception {
        var response = getMockUserResponseDto();

        when(service.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.email").value(response.getEmail()));

        verify(service).findById(1L);
    }

    @Test
    void shouldFindAllByOrganizationIdSuccessfully() throws Exception {
        var userResponse = getMockUserResponseDto();

        var response = new PageResponseDto<>(
                List.of(userResponse),
                0,
                10,
                1L,
                1,
                true
        );

        when(service.findAllByOrganizationId(1L, 0, 10)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/organization/1")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(userResponse.getId()))
                .andExpect(jsonPath("$.content[0].email").value(userResponse.getEmail()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        verify(service).findAllByOrganizationId(1L, 0, 10);
    }

    @Test
    void shouldFindAllByOrganizationUnitIdSuccessfully() throws Exception {
        var userResponse = getMockUserResponseDto();

        var response = new PageResponseDto<>(
                List.of(userResponse),
                0,
                10,
                1L,
                1,
                true
        );

        when(service.findAllByOrganizationUnitId(1L, 0, 10)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/organization-unit/1")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(userResponse.getId()))
                .andExpect(jsonPath("$.content[0].email").value(userResponse.getEmail()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        verify(service).findAllByOrganizationUnitId(1L, 0, 10);
    }

    @Test
    void shouldCreateUserSuccessfully() throws Exception {
        var request = getMockUserCreateRequestDto();
        var response = getMockUserResponseDto();

        when(service.create(any(UserCreateRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.email").value(response.getEmail()));

        verify(service).create(any(UserCreateRequestDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreateUserRequestIsInvalid() throws Exception {
        var request = new UserCreateRequestDto();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUploadImageSuccessfully() throws Exception {
        when(service.updatePhoto(any(MultipartFile.class))).thenReturn("photo/path/image.png");

        var multipartFile = new MockMultipartFile(
                "image",
                "user.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/users/upload")
                        .file(multipartFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").value("photo/path/image.png"));

        verify(service).updatePhoto(any(MultipartFile.class));
    }

    @Test
    void shouldUpdateUserSuccessfully() throws Exception {
        var request = getMockUserUpdateRequestDto();
        var response = getMockUserResponseDto();

        when(service.update(any(UserUpdateRequestDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.email").value(response.getEmail()));

        verify(service).update(any(UserUpdateRequestDto.class));
    }

    @Test
    void shouldChangePasswordSuccessfully() throws Exception {
        var request = getMockUserChangePasswordRequestDto();
        var response = getMockUserResponseDto();

        when(service.changePassword(any(UserChangePasswordRequestDto.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/users/changePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.email").value(response.getEmail()));

        verify(service).changePassword(any(UserChangePasswordRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldChangeRoleSuccessfully() throws Exception {
        var request = getMockUserChangeRoleRequestDto();
        var response = getMockUserResponseDto();

        when(service.changeRole(any(UserChangeRoleRequestDto.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/users/changeRole")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.email").value(response.getEmail()));

        verify(service).changeRole(any(UserChangeRoleRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUserSuccessfully() throws Exception {
        var response = getMockUserResponseDto();

        when(service.deactivateUser(1L)).thenReturn(response);

        mockMvc.perform(delete("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.email").value(response.getEmail()));

        verify(service).deactivateUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReactivateUserSuccessfully() throws Exception {
        var request = getMockReactivateUserRequestDto();
        var response = getMockUserResponseDto();

        when(service.reactivateUser(request.getEmail())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/users/reactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.email").value(response.getEmail()));

        verify(service).reactivateUser(request.getEmail());
    }

    private UserResponseDto getMockUserResponseDto() {
        var dto = new UserResponseDto();
        dto.setId(1L);
        dto.setEmail("user@test.com");
        return dto;
    }

    private UserCreateRequestDto getMockUserCreateRequestDto() {
        var dto = new UserCreateRequestDto();
        dto.setName("Ricardo");
        dto.setEmail("user@test.com");
        dto.setPassword("123456");
        dto.setRole(Role.CLIENT);
        dto.setAvatarUrl("https://image.com/avatar.png");
        dto.setJoinCode("JOIN123");
        return dto;
    }

    private UserUpdateRequestDto getMockUserUpdateRequestDto() {
        var dto = new UserUpdateRequestDto();
        dto.setId(1L);
        dto.setName("Ricardo");
        dto.setJoinCode("joinCode");
        dto.setAvatarUrl("http://image.jpg");
        return dto;
    }

    private UserChangePasswordRequestDto getMockUserChangePasswordRequestDto() {
        var dto = new UserChangePasswordRequestDto();
        dto.setId(1L);
        dto.setPassword("123456");
        return dto;
    }

    private UserChangeRoleRequestDto getMockUserChangeRoleRequestDto() {
        var dto = new UserChangeRoleRequestDto();
        dto.setId(1L);
        dto.setRole(Role.ADMIN);
        return dto;
    }

    private ReactivateUserRequestDto getMockReactivateUserRequestDto() {
        var dto = new ReactivateUserRequestDto();
        dto.setEmail("user@test.com");
        return dto;
    }
}
