package com.br.capoeira.eventos.user_api.service;


import com.br.capoeira.eventos.user_api.config.exception.ValidationException;
import com.br.capoeira.eventos.user_api.dto.*;
import com.br.capoeira.eventos.user_api.enums.Role;
import com.br.capoeira.eventos.user_api.mapper.UserMapper;
import com.br.capoeira.eventos.user_api.model.User;
import com.br.capoeira.eventos.user_api.repository.UserRepository;
import com.br.capoeira.eventos.user_api.restClient.OrganizationClient;
import com.br.capoeira.eventos.user_api.service.aws.S3Service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.util.StringUtils.hasText;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final OrganizationClient organizationClient;
    private final S3Service s3Service;

    public UserResponseDto findById(Long id) {
        return mapper.userToResponseDto(getUserByIdOrThrow(id));
    }

    public PageResponseDto<UserResponseDto> findAllByOrganizationId(
            Long organizationId,
            int page,
            int size) {
        validateId(organizationId, "Organization Id must be informed");
        var pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        var userPage = repository.findAllByOrganizationIdOrderByIdAsc(organizationId, pageable);

        var content = userPage
                .stream()
                .map(mapper::userToResponseDto)
                .toList();

        return new PageResponseDto<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast()
        );
    }

    public PageResponseDto<UserResponseDto> findAllByOrganizationUnitId(
            Long organizationUnitId,
            int page,
            int size
    ) {
        validateId(organizationUnitId, "Organization Unit Id must be informed");
        var pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        var userPage =  repository.findAllByOrganizationUnitIdOrderByIdAsc(organizationUnitId, pageable);

        var content = userPage
                .stream()
                .map(mapper::userToResponseDto)
                .toList();

        return new PageResponseDto<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast()
        );
    }

    @Transactional
    public UserResponseDto create(UserCreateRequestDto dto) {
        validateCreateRequest(dto);
        validateEmailAlreadyExists(dto.getEmail());

        User user = mapper.createRequestDtoToUser(dto);
        user.setRole(Role.CLIENT);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setActive(true);

        User savedUser = repository.save(user);
        return mapper.userToResponseDto(savedUser);
    }

    @Transactional
    public UserResponseDto update(UserUpdateRequestDto dto) {
        validateUpdateRequest(dto);

        User savedUser = getUserByIdOrThrow(dto.getId());

        updateUserFields(savedUser, dto);
        applyJoinCodeIfNecessary(savedUser, dto.getJoinCode());

        User updatedUser = repository.save(savedUser);
        return mapper.userToResponseDto(updatedUser);
    }

    @Transactional
    public UserResponseDto changePassword(UserChangePasswordRequestDto dto) {
        if (dto == null) {
            throw new ValidationException("Request must be informed");
        }

        User savedUser = getUserByIdOrThrow(dto.getId());
        savedUser.setPassword(passwordEncoder.encode(dto.getPassword()));

        User updatedUser = repository.save(savedUser);
        return mapper.userToResponseDto(updatedUser);
    }

    @Transactional
    public UserResponseDto changeRole(UserChangeRoleRequestDto dto) {
        if (dto == null) {
            throw new ValidationException("Request must be informed");
        }

        if (dto.getRole() == null) {
            throw new ValidationException("Role must be informed");
        }

        validateCurrentUser(dto.getId(), "Users cannot change their own role");

        User savedUser = getUserByIdOrThrow(dto.getId());
        savedUser.setRole(dto.getRole());

        User updatedUser = repository.save(savedUser);
        return mapper.userToResponseDto(updatedUser);
    }

    private static void validateCurrentUser(Long id, String message) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User loggedUser)) {
            throw new ValidationException("Authenticated user not found");
        }

        if (loggedUser.getId().equals(id)) {
            throw new ValidationException(message);
        }
    }

    @Transactional
    public UserResponseDto deactivateUser(Long id) {
        User savedUser = getUserByIdOrThrow(id);
        validateCurrentUser(id, "Users cannot deactivate themselves");
        savedUser.setActive(false);

        User updatedUser = repository.save(savedUser);
        return mapper.userToResponseDto(updatedUser);
    }

    @Transactional
    public UserResponseDto reactivateUser(String email) {
        if (!hasText(email)) {
            throw new ValidationException("Email must be informed");
        }

        User savedUser = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        savedUser.setActive(true);

        User updatedUser = repository.save(savedUser);
        return mapper.userToResponseDto(updatedUser);
    }

    public String updatePhoto(MultipartFile file) {
        validatePhoto(file);

        log.info("Uploading photo to S3");
        return s3Service.uploadFile(file).toString();
    }

    @Transactional
    public UserResponseDto promoteToSuperAdmin(PromoteToSuperAdminDtoRequest request) {
        User user = repository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setRole(Role.SUPER_ADMIN);
        user.setOrganizationId(request.organizationId());
        user.setOrganizationUnitId(request.organizationUnitId());

        User savedUser = repository.save(user);

        return mapper.userToResponseDto(savedUser);
    }

    private User getUserByIdOrThrow(Long id) {
        validateId(id, "Id must be informed");

        return repository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private void validateId(Long id, String message) {
        if (id == null) {
            throw new ValidationException(message);
        }
    }

    private void validateEmailAlreadyExists(String email) {
        if (repository.existsByEmail(email)) {
            throw new ValidationException("Email already registered");
        }
    }

    private void validateCreateRequest(UserCreateRequestDto dto) {
        if (dto == null) {
            throw new ValidationException("Request must be informed");
        }
    }

    private void validateUpdateRequest(UserUpdateRequestDto dto) {
        if (dto == null) {
            throw new ValidationException("Request must be informed");
        }

        validateId(dto.getId(), "Id must be informed");
    }

    private void validatePhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Image must be informed");
        }
    }

    private void applyJoinCodeIfNecessary(User user, String joinCode) {
        if (user.getOrganizationId() == null && hasText(joinCode)) {
            var org = organizationClient.getByJoinCode(joinCode);

            user.setOrganizationId(org.getOrganizationId());
            user.setOrganizationUnitId(org.getOrganizationUnitId());
        }
    }

    private void updateUserFields(User user, UserUpdateRequestDto dto) {
        user.setName(dto.getName());
        user.setAvatarUrl(dto.getAvatarUrl());
    }
}
