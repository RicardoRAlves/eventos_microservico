package unit.com.br.capoeira.eventos.user_api.service;

import com.br.capoeira.eventos.user_api.config.exception.ValidationException;
import com.br.capoeira.eventos.user_api.dto.*;
import com.br.capoeira.eventos.user_api.enums.Role;
import com.br.capoeira.eventos.user_api.mapper.UserMapper;
import com.br.capoeira.eventos.user_api.model.User;
import com.br.capoeira.eventos.user_api.repository.UserRepository;
import com.br.capoeira.eventos.user_api.restClient.OrganizationClient;
import com.br.capoeira.eventos.user_api.service.UserService;
import com.br.capoeira.eventos.user_api.service.aws.S3Service;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository repository;

    @Mock
    private OrganizationClient organizationClient;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private UserService service;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldFindUserById() {
        var user = getMockUser(1L, Role.CLIENT, true);
        var response = new UserResponseDto();

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.userToResponseDto(user)).thenReturn(response);

        var result = service.findById(1L);

        assertNotNull(result);
        assertEquals(response, result);

        verify(repository).findById(1L);
        verify(mapper).userToResponseDto(user);
    }

    @Test
    void shouldThrowValidationExceptionWhenFindByIdWithNullId() {
        var ex = assertThrows(
                ValidationException.class,
                () -> service.findById(null)
        );

        assertEquals("Id must be informed", ex.getMessage());
        verify(repository, never()).findById(any());
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenFindByIdAndUserDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        var ex = assertThrows(
                UsernameNotFoundException.class,
                () -> service.findById(1L)
        );

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void shouldFindUserByEmail() {
        var user = getMockUser(1L, Role.CLIENT, true);
        var response = new UserResponseDto();

        when(repository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(mapper.userToResponseDto(user)).thenReturn(response);

        var result = service.findByEmail("user@test.com");

        assertNotNull(result);
        assertEquals(response, result);

        verify(repository).findByEmail("user@test.com");
        verify(mapper).userToResponseDto(user);
    }

    @Test
    void shouldThrowValidationExceptionWhenFindByEmailWithEmptyEmail() {
        var ex = assertThrows(
                ValidationException.class,
                () -> service.findByEmail("")
        );

        assertEquals("Email must be informed", ex.getMessage());
        verify(repository, never()).findByEmail(anyString());
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenFindByEmailAndUserDoesNotExist() {
        when(repository.findByEmail("user@test.com")).thenReturn(Optional.empty());

        var ex = assertThrows(
                UsernameNotFoundException.class,
                () -> service.findByEmail("user@test.com")
        );

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void shouldFindAllUsersByOrganizationId() {
        var user1 = getMockUser(1L, Role.CLIENT, true);
        var user2 = getMockUser(2L, Role.CLIENT, true);

        var response1 = new UserResponseDto();
        var response2 = new UserResponseDto();

        var userPage = new PageImpl<>(
                List.of(user1, user2),
                PageRequest.of(0, 10),
                2
        );

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(userPage);
        when(mapper.userToResponseDto(user1)).thenReturn(response1);
        when(mapper.userToResponseDto(user2)).thenReturn(response2);

        var result = service.findAllByOrganizationId(
                10L,
                null,
                true,
                Role.CLIENT,
                "name",
                "asc",
                0,
                10
        );

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(2L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isLast());

        assertEquals(response1, result.getContent().get(0));
        assertEquals(response2, result.getContent().get(1));

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
        verify(mapper).userToResponseDto(user1);
        verify(mapper).userToResponseDto(user2);
    }

    @Test
    void shouldThrowValidationExceptionWhenOrganizationIdIsNull() {
        var ex = assertThrows(
                ValidationException.class,
                () -> service.findAllByOrganizationId(
                        null,
                        null,
                        null,
                        null,
                        "name",
                        "asc",
                        0,
                        10
                )
        );

        assertEquals("Organization Id must be informed", ex.getMessage());
        verify(repository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void shouldFindAllUsersByOrganizationUnitId() {
        var user = getMockUser(1L, Role.CLIENT, true);
        var response = new UserResponseDto();

        var userPage = new PageImpl<>(
                List.of(user),
                PageRequest.of(0, 10),
                1
        );

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(userPage);
        when(mapper.userToResponseDto(user)).thenReturn(response);

        var result = service.findAllByOrganizationUnitId(
                20L,
                true,
                Role.CLIENT,
                "name",
                "asc",
                0,
                10
        );

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(response, result.getContent().get(0));

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
        verify(mapper).userToResponseDto(user);
    }

    @Test
    void shouldThrowValidationExceptionWhenOrganizationUnitIdIsNull() {
        var ex = assertThrows(
                ValidationException.class,
                () -> service.findAllByOrganizationUnitId(
                        null,
                        null,
                        null,
                        "name",
                        "asc",
                        0,
                        10
                )
        );

        assertEquals("Organization Unit Id must be informed", ex.getMessage());
        verify(repository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void shouldCreateUserSuccessfully() {
        var dto = new UserCreateRequestDto();
        dto.setEmail("user@test.com");
        dto.setPassword("123456");

        var user = getMockUser(null, Role.ADMIN, false);
        var savedUser = getMockUser(1L, Role.CLIENT, true);
        var response = new UserResponseDto();

        when(repository.existsByEmail("user@test.com")).thenReturn(false);
        when(mapper.createRequestDtoToUser(dto)).thenReturn(user);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(repository.save(user)).thenReturn(savedUser);
        when(mapper.userToResponseDto(savedUser)).thenReturn(response);

        var result = service.create(dto);

        assertNotNull(result);
        assertEquals(Role.CLIENT, user.getRole());
        assertEquals("encoded-password", user.getPassword());
        assertTrue(user.getActive());

        verify(organizationClient, never()).getByJoinCode(anyString());
        verify(repository).save(user);
        verify(mapper).userToResponseDto(savedUser);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateRequestIsNull() {
        var ex = assertThrows(
                ValidationException.class,
                () -> service.create(null)
        );

        assertEquals("Request must be informed", ex.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenEmailAlreadyExists() {
        var dto = new UserCreateRequestDto();
        dto.setEmail("user@test.com");

        when(repository.existsByEmail("user@test.com")).thenReturn(true);

        var ex = assertThrows(
                ValidationException.class,
                () -> service.create(dto)
        );

        assertEquals("Email already registered", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        var dto = new UserUpdateRequestDto();
        dto.setId(1L);
        dto.setName("Updated Name");
        dto.setAvatarUrl("new-avatar.png");

        var savedUser = getMockUser(1L, Role.CLIENT, true);
        var response = new UserResponseDto();

        when(repository.findById(1L)).thenReturn(Optional.of(savedUser));
        when(repository.save(savedUser)).thenReturn(savedUser);
        when(mapper.userToResponseDto(savedUser)).thenReturn(response);

        var result = service.update(dto);

        assertNotNull(result);
        assertEquals("Updated Name", savedUser.getName());
        assertEquals("new-avatar.png", savedUser.getAvatarUrl());

        verify(organizationClient, never()).getByJoinCode(anyString());
        verify(repository).save(savedUser);
        verify(mapper).userToResponseDto(savedUser);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateRequestIsNull() {
        var ex = assertThrows(
                ValidationException.class,
                () -> service.update(null)
        );

        assertEquals("Request must be informed", ex.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateIdIsNull() {
        var dto = new UserUpdateRequestDto();
        dto.setId(null);

        var ex = assertThrows(
                ValidationException.class,
                () -> service.update(dto)
        );

        assertEquals("Id must be informed", ex.getMessage());
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        var dto = new UserChangePasswordRequestDto();
        dto.setId(1L);
        dto.setPassword("new-password");

        var savedUser = getMockUser(1L, Role.CLIENT, true);
        var response = new UserResponseDto();

        when(repository.findById(1L)).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-password");
        when(repository.save(savedUser)).thenReturn(savedUser);
        when(mapper.userToResponseDto(savedUser)).thenReturn(response);

        var result = service.changePassword(dto);

        assertNotNull(result);
        assertEquals("encoded-password", savedUser.getPassword());

        verify(repository).save(savedUser);
        verify(mapper).userToResponseDto(savedUser);
    }

    @Test
    void shouldThrowValidationExceptionWhenChangePasswordRequestIsNull() {
        var ex = assertThrows(
                ValidationException.class,
                () -> service.changePassword(null)
        );

        assertEquals("Request must be informed", ex.getMessage());
    }

    @Test
    void shouldJoinCodeSuccessfully() {
        var dto = new UserJoinCodeRequestDto();
        dto.setId(1L);
        dto.setJoinCode("JOIN123");

        var savedUser = getMockUser(1L, Role.CLIENT, true);
        savedUser.setOrganizationId(null);
        savedUser.setOrganizationUnitId(null);

        var response = new UserResponseDto();

        var orgResponse = new OrganizationResponseDto();
        orgResponse.setOrganizationId(100L);
        orgResponse.setOrganizationUnitId(200L);

        when(repository.findById(1L)).thenReturn(Optional.of(savedUser));
        when(organizationClient.getByJoinCode("JOIN123")).thenReturn(orgResponse);
        when(repository.save(savedUser)).thenReturn(savedUser);
        when(mapper.userToResponseDto(savedUser)).thenReturn(response);

        var result = service.joinCode(dto);

        assertNotNull(result);
        assertEquals(100L, savedUser.getOrganizationId());
        assertEquals(200L, savedUser.getOrganizationUnitId());

        verify(organizationClient).getByJoinCode("JOIN123");
        verify(repository).save(savedUser);
        verify(mapper).userToResponseDto(savedUser);
    }

    @Test
    void shouldThrowValidationExceptionWhenJoinCodeRequestIsNull() {
        var ex = assertThrows(
                ValidationException.class,
                () -> service.joinCode(null)
        );

        assertEquals("Request must be informed", ex.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenUserAlreadyHasOrganizationOnJoinCode() {
        var dto = new UserJoinCodeRequestDto();
        dto.setId(1L);
        dto.setJoinCode("JOIN123");

        var savedUser = getMockUser(1L, Role.CLIENT, true);
        savedUser.setOrganizationId(100L);
        savedUser.setOrganizationUnitId(200L);

        when(repository.findById(1L)).thenReturn(Optional.of(savedUser));

        var ex = assertThrows(
                ValidationException.class,
                () -> service.joinCode(dto)
        );

        assertEquals(
                "User is already added to Organization 100",
                ex.getMessage()
        );

        verify(organizationClient, never()).getByJoinCode(anyString());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldChangeRoleSuccessfully() {
        mockAuthenticatedUser(99L, Role.ADMIN);

        var dto = new UserChangeRoleRequestDto();
        dto.setId(1L);
        dto.setRole(Role.ADMIN);

        var savedUser = getMockUser(1L, Role.CLIENT, true);
        var response = new UserResponseDto();

        when(repository.findById(1L)).thenReturn(Optional.of(savedUser));
        when(repository.save(savedUser)).thenReturn(savedUser);
        when(mapper.userToResponseDto(savedUser)).thenReturn(response);

        var result = service.changeRole(dto);

        assertNotNull(result);
        assertEquals(Role.ADMIN, savedUser.getRole());

        verify(repository).save(savedUser);
        verify(mapper).userToResponseDto(savedUser);
    }

    @Test
    void shouldThrowValidationExceptionWhenChangeRoleRequestIsNull() {
        var ex = assertThrows(
                ValidationException.class,
                () -> service.changeRole(null)
        );

        assertEquals("Request must be informed", ex.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenRoleIsNullInChangeRole() {
        mockAuthenticatedUser(99L, Role.ADMIN);

        var dto = new UserChangeRoleRequestDto();
        dto.setId(1L);
        dto.setRole(null);

        var ex = assertThrows(
                ValidationException.class,
                () -> service.changeRole(dto)
        );

        assertEquals("Role must be informed", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenAuthenticatedUserNotFoundOnChangeRole() {
        var dto = new UserChangeRoleRequestDto();
        dto.setId(1L);
        dto.setRole(Role.ADMIN);

        var ex = assertThrows(
                ValidationException.class,
                () -> service.changeRole(dto)
        );

        assertEquals("Authenticated user not found", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenUserTriesToChangeOwnRole() {
        mockAuthenticatedUser(1L, Role.ADMIN);

        var dto = new UserChangeRoleRequestDto();
        dto.setId(1L);
        dto.setRole(Role.CLIENT);

        var ex = assertThrows(
                ValidationException.class,
                () -> service.changeRole(dto)
        );

        assertEquals("Users cannot change their own role", ex.getMessage());

        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldDeactivateUserSuccessfully() {
        mockAuthenticatedUser(99L, Role.ADMIN);

        var savedUser = getMockUser(1L, Role.CLIENT, true);
        var response = new UserResponseDto();

        when(repository.findById(1L)).thenReturn(Optional.of(savedUser));
        when(repository.save(savedUser)).thenReturn(savedUser);
        when(mapper.userToResponseDto(savedUser)).thenReturn(response);

        var result = service.deactivateUser(1L);

        assertNotNull(result);
        assertFalse(savedUser.getActive());

        verify(repository).save(savedUser);
        verify(mapper).userToResponseDto(savedUser);
    }

    @Test
    void shouldThrowValidationExceptionWhenUserTriesToDeactivateSelf() {
        mockAuthenticatedUser(1L, Role.ADMIN);

        var savedUser = getMockUser(1L, Role.ADMIN, true);

        when(repository.findById(1L)).thenReturn(Optional.of(savedUser));

        var ex = assertThrows(
                ValidationException.class,
                () -> service.deactivateUser(1L)
        );

        assertEquals("Users cannot deactivate themselves", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldReactivateUserSuccessfully() {
        var savedUser = getMockUser(1L, Role.CLIENT, false);
        var response = new UserResponseDto();

        when(repository.findById(1L)).thenReturn(Optional.of(savedUser));
        when(repository.save(savedUser)).thenReturn(savedUser);
        when(mapper.userToResponseDto(savedUser)).thenReturn(response);

        var result = service.reactivateUser(1L);

        assertNotNull(result);
        assertTrue(savedUser.getActive());

        verify(repository).findById(1L);
        verify(repository).save(savedUser);
        verify(mapper).userToResponseDto(savedUser);
    }

    @Test
    void shouldThrowValidationExceptionWhenReactivateIdIsNull() {
        var ex = assertThrows(
                ValidationException.class,
                () -> service.reactivateUser(null)
        );

        assertEquals("Id must be informed", ex.getMessage());
        verify(repository, never()).findById(any());
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenReactivateUserDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        var ex = assertThrows(
                UsernameNotFoundException.class,
                () -> service.reactivateUser(1L)
        );

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void shouldUploadPhotoSuccessfully() {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(s3Service.uploadFile(file))
                .thenReturn(URI.create("https://s3.amazonaws.com/file.png"));

        var result = service.updatePhoto(file);

        assertEquals("https://s3.amazonaws.com/file.png", result);
        verify(s3Service).uploadFile(file);
    }

    @Test
    void shouldThrowValidationExceptionWhenPhotoIsNull() {
        var ex = assertThrows(
                ValidationException.class,
                () -> service.updatePhoto(null)
        );

        assertEquals("Image must be informed", ex.getMessage());
        verify(s3Service, never()).uploadFile(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenPhotoIsEmpty() {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(true);

        var ex = assertThrows(
                ValidationException.class,
                () -> service.updatePhoto(file)
        );

        assertEquals("Image must be informed", ex.getMessage());
        verify(s3Service, never()).uploadFile(any());
    }

    @Test
    void shouldPromoteToSuperAdminSuccessfully() {
        var request = new PromoteToSuperAdminDtoRequest(
                1L,
                100L,
                200L
        );

        var user = getMockUser(1L, Role.CLIENT, true);
        var response = new UserResponseDto();

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);
        when(mapper.userToResponseDto(user)).thenReturn(response);

        var result = service.promoteToSuperAdmin(request);

        assertNotNull(result);
        assertEquals(Role.SUPER_ADMIN, user.getRole());
        assertEquals(100L, user.getOrganizationId());
        assertEquals(200L, user.getOrganizationUnitId());

        verify(repository).save(user);
        verify(mapper).userToResponseDto(user);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenPromoteToSuperAdminUserDoesNotExist() {
        var request = new PromoteToSuperAdminDtoRequest(
                1L,
                100L,
                200L
        );

        when(repository.findById(1L)).thenReturn(Optional.empty());

        var ex = assertThrows(
                EntityNotFoundException.class,
                () -> service.promoteToSuperAdmin(request)
        );

        assertEquals("User not found", ex.getMessage());
        verify(repository, never()).save(any());
    }

    private void mockAuthenticatedUser(Long id, Role role) {
        var loggedUser = getMockUser(id, role, true);

        var authentication = new UsernamePasswordAuthenticationToken(
                loggedUser,
                null,
                loggedUser.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private User getMockUser(Long id, Role role, Boolean active) {
        var user = new User();

        user.setId(id);
        user.setName("Test User");
        user.setEmail("user@test.com");
        user.setPassword("password");
        user.setRole(role);
        user.setActive(active);
        user.setAvatarUrl("avatar.png");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }
}