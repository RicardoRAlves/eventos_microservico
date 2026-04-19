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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
        var ex = assertThrows(ValidationException.class, () -> service.findById(null));

        assertEquals("Id must be informed", ex.getMessage());
        verify(repository, never()).findById(any());
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenFindByIdAndUserDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        var ex = assertThrows(UsernameNotFoundException.class, () -> service.findById(1L));

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void shouldFindAllUsersByOrganizationId() {
        var user1 = getMockUser(1L, Role.CLIENT, true);
        var user2 = getMockUser(2L, Role.CLIENT, true);

        var response1 = new UserResponseDto();
        var response2 = new UserResponseDto();

        when(repository.findAllByOrganizationIdOrderByIdAsc(10L)).thenReturn(List.of(user1, user2));
        when(mapper.userToResponseDto(user1)).thenReturn(response1);
        when(mapper.userToResponseDto(user2)).thenReturn(response2);

        var result = service.findAllByOrganizationId(10L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository).findAllByOrganizationIdOrderByIdAsc(10L);
    }

    @Test
    void shouldThrowValidationExceptionWhenOrganizationIdIsNull() {
        var ex = assertThrows(ValidationException.class, () -> service.findAllByOrganizationId(null));

        assertEquals("Organization Id must be informed", ex.getMessage());
        verify(repository, never()).findAllByOrganizationIdOrderByIdAsc(any());
    }

    @Test
    void shouldFindAllUsersByOrganizationUnitId() {
        var user = getMockUser(1L, Role.CLIENT, true);
        var response = new UserResponseDto();

        when(repository.findAllByOrganizationUnitIdOrderByIdAsc(20L)).thenReturn(List.of(user));
        when(mapper.userToResponseDto(user)).thenReturn(response);

        var result = service.findAllByOrganizationUnitId(20L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository).findAllByOrganizationUnitIdOrderByIdAsc(20L);
    }

    @Test
    void shouldThrowValidationExceptionWhenOrganizationUnitIdIsNull() {
        var ex = assertThrows(ValidationException.class, () -> service.findAllByOrganizationUnitId(null));

        assertEquals("Organization Unit Id must be informed", ex.getMessage());
        verify(repository, never()).findAllByOrganizationUnitIdOrderByIdAsc(any());
    }

    @Test
    void shouldCreateUserSuccessfullyWithoutJoinCode() {
        var dto = new UserCreateRequestDto();
        dto.setEmail("user@test.com");
        dto.setPassword("123456");
        dto.setJoinCode(null);

        var user = getMockUser(null, Role.CLIENT, false);
        var savedUser = getMockUser(1L, Role.CLIENT, true);
        var response = new UserResponseDto();

        when(repository.existsByEmail("user@test.com")).thenReturn(false);
        when(mapper.createRequestDtoToUser(dto)).thenReturn(user);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(repository.save(user)).thenReturn(savedUser);
        when(mapper.userToResponseDto(savedUser)).thenReturn(response);

        var result = service.create(dto);

        assertNotNull(result);
        assertEquals("encoded-password", user.getPassword());
        assertTrue(user.getActive());
        verify(organizationClient, never()).getByJoinCode(anyString());
        verify(repository).save(user);
    }

    @Test
    void shouldCreateUserSuccessfullyWithJoinCode() {
        var dto = new UserCreateRequestDto();
        dto.setEmail("user@test.com");
        dto.setPassword("123456");
        dto.setJoinCode("JOIN123");

        var user = getMockUser(null, Role.CLIENT, false);
        var savedUser = getMockUser(1L, Role.CLIENT, true);
        var response = new UserResponseDto();

        var orgResponse = new OrganizationResponseDto();
        orgResponse.setOrganizationId(100L);
        orgResponse.setOrganizationUnitId(200L);

        when(repository.existsByEmail("user@test.com")).thenReturn(false);
        when(mapper.createRequestDtoToUser(dto)).thenReturn(user);
        when(organizationClient.getByJoinCode("JOIN123")).thenReturn(orgResponse);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(repository.save(user)).thenReturn(savedUser);
        when(mapper.userToResponseDto(savedUser)).thenReturn(response);

        var result = service.create(dto);

        assertNotNull(result);
        assertEquals("encoded-password", user.getPassword());
        assertTrue(user.getActive());
        assertEquals(100L, user.getOrganizationId());
        assertEquals(200L, user.getOrganizationUnitId());
        verify(organizationClient).getByJoinCode("JOIN123");
        verify(repository).save(user);
    }

    @Test
    void shouldNotApplyJoinCodeWhenCreatingUserAndOrganizationAlreadyExists() {
        var dto = new UserCreateRequestDto();
        dto.setEmail("user@test.com");
        dto.setPassword("123456");
        dto.setJoinCode("JOIN123");

        var user = getMockUser(null, Role.CLIENT, false);
        user.setOrganizationId(10L);
        user.setOrganizationUnitId(20L);

        var savedUser = getMockUser(1L, Role.CLIENT, true);
        var response = new UserResponseDto();

        when(repository.existsByEmail("user@test.com")).thenReturn(false);
        when(mapper.createRequestDtoToUser(dto)).thenReturn(user);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(repository.save(user)).thenReturn(savedUser);
        when(mapper.userToResponseDto(savedUser)).thenReturn(response);

        var result = service.create(dto);

        assertNotNull(result);
        assertEquals(10L, user.getOrganizationId());
        assertEquals(20L, user.getOrganizationUnitId());
        verify(organizationClient, never()).getByJoinCode(anyString());
        verify(repository).save(user);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateRequestIsNull() {
        var ex = assertThrows(ValidationException.class, () -> service.create(null));

        assertEquals("Request must be informed", ex.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenEmailAlreadyExists() {
        var dto = new UserCreateRequestDto();
        dto.setEmail("user@test.com");

        when(repository.existsByEmail("user@test.com")).thenReturn(true);

        var ex = assertThrows(ValidationException.class, () -> service.create(dto));

        assertEquals("Email already registered", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        var dto = new UserUpdateRequestDto();
        dto.setId(1L);
        dto.setName("Updated Name");
        dto.setAvatarUrl("new-avatar.png");
        dto.setJoinCode(null);

        var savedUser = getMockUser(1L, Role.CLIENT, true);
        var response = new UserResponseDto();

        when(repository.findById(1L)).thenReturn(Optional.of(savedUser));
        when(repository.save(savedUser)).thenReturn(savedUser);
        when(mapper.userToResponseDto(savedUser)).thenReturn(response);

        var result = service.update(dto);

        assertNotNull(result);
        assertEquals("Updated Name", savedUser.getName());
        assertEquals("new-avatar.png", savedUser.getAvatarUrl());
        verify(repository).save(savedUser);
        verify(organizationClient, never()).getByJoinCode(anyString());
    }

    @Test
    void shouldUpdateUserAndApplyJoinCodeWhenNecessary() {
        var dto = new UserUpdateRequestDto();
        dto.setId(1L);
        dto.setName("Updated Name");
        dto.setAvatarUrl("new-avatar.png");
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

        var result = service.update(dto);

        assertNotNull(result);
        assertEquals(100L, savedUser.getOrganizationId());
        assertEquals(200L, savedUser.getOrganizationUnitId());
        verify(organizationClient).getByJoinCode("JOIN123");
        verify(repository).save(savedUser);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateRequestIsNull() {
        var ex = assertThrows(ValidationException.class, () -> service.update(null));

        assertEquals("Request must be informed", ex.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateIdIsNull() {
        var dto = new UserUpdateRequestDto();
        dto.setId(null);

        var ex = assertThrows(ValidationException.class, () -> service.update(dto));

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
    }

    @Test
    void shouldThrowValidationExceptionWhenChangePasswordRequestIsNull() {
        var ex = assertThrows(ValidationException.class, () -> service.changePassword(null));

        assertEquals("Request must be informed", ex.getMessage());
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
    }

    @Test
    void shouldThrowValidationExceptionWhenChangeRoleRequestIsNull() {
        var ex = assertThrows(ValidationException.class, () -> service.changeRole(null));

        assertEquals("Request must be informed", ex.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenRoleIsNullInChangeRole() {
        mockAuthenticatedUser(99L, Role.ADMIN);

        var dto = new UserChangeRoleRequestDto();
        dto.setId(1L);
        dto.setRole(null);

        var ex = assertThrows(ValidationException.class, () -> service.changeRole(dto));

        assertEquals("Role must be informed", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenUserTriesToChangeOwnRole() {
        mockAuthenticatedUser(1L, Role.ADMIN);

        var dto = new UserChangeRoleRequestDto();
        dto.setId(1L);
        dto.setRole(Role.CLIENT);

        var ex = assertThrows(ValidationException.class, () -> service.changeRole(dto));

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
    }

    @Test
    void shouldThrowValidationExceptionWhenUserTriesToDeactivateSelf() {
        mockAuthenticatedUser(1L, Role.ADMIN);

        var savedUser = getMockUser(1L, Role.ADMIN, true);
        when(repository.findById(1L)).thenReturn(Optional.of(savedUser));

        var ex = assertThrows(ValidationException.class, () -> service.deactivateUser(1L));

        assertEquals("Users cannot deactivate themselves", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldReactivateUserSuccessfully() {
        var savedUser = getMockUser(1L, Role.CLIENT, false);
        var response = new UserResponseDto();

        when(repository.findByEmail("user@test.com")).thenReturn(Optional.of(savedUser));
        when(repository.save(savedUser)).thenReturn(savedUser);
        when(mapper.userToResponseDto(savedUser)).thenReturn(response);

        var result = service.reactivateUser("user@test.com");

        assertNotNull(result);
        assertTrue(savedUser.getActive());
        verify(repository).save(savedUser);
    }

    @Test
    void shouldThrowValidationExceptionWhenReactivateEmailIsEmpty() {
        var ex = assertThrows(ValidationException.class, () -> service.reactivateUser(""));

        assertEquals("Email must be informed", ex.getMessage());
        verify(repository, never()).findByEmail(anyString());
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenReactivateUserDoesNotExist() {
        when(repository.findByEmail("user@test.com")).thenReturn(Optional.empty());

        var ex = assertThrows(UsernameNotFoundException.class, () -> service.reactivateUser("user@test.com"));

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void shouldUploadPhotoSuccessfully() {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(s3Service.uploadFile(file)).thenReturn(URI.create("https://s3.amazonaws.com/file.png"));

        var result = service.updatePhoto(file);

        assertEquals("https://s3.amazonaws.com/file.png", result);
        verify(s3Service).uploadFile(file);
    }

    @Test
    void shouldThrowValidationExceptionWhenPhotoIsNull() {
        var ex = assertThrows(ValidationException.class, () -> service.updatePhoto(null));

        assertEquals("Image must be informed", ex.getMessage());
        verify(s3Service, never()).uploadFile(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenPhotoIsEmpty() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        var ex = assertThrows(ValidationException.class, () -> service.updatePhoto(file));

        assertEquals("Image must be informed", ex.getMessage());
        verify(s3Service, never()).uploadFile(any());
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
        User user = new User();
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