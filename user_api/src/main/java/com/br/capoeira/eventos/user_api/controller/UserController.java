package com.br.capoeira.eventos.user_api.controller;

import com.br.capoeira.eventos.user_api.dto.*;
import com.br.capoeira.eventos.user_api.enums.Role;
import com.br.capoeira.eventos.user_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> findUserById(@PathVariable Long id) {
        log.info("Finding user by id {}", id);
        var user = service.findById(id);
        return ResponseEntity.ok(user);
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDto> findUserByEmail(@PathVariable String email) {
        log.info("Finding user by email {}", email);
        var user = service.findByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/organization/{organizationId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PageResponseDto<UserResponseDto>> findAllByOrganizationId(
            @PathVariable Long organizationId,
            @RequestParam(required = false) Long organizationUnitId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var response = service.findAllByOrganizationId(
                organizationId,
                organizationUnitId,
                active,
                role,
                sortBy,
                direction,
                page,
                size
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/organization-unit/{organizationUnitId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PageResponseDto<UserResponseDto>> findAllByOrganizationUnitId(
            @PathVariable Long organizationUnitId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var response = service.findAllByOrganizationUnitId(
                organizationUnitId,
                active,
                role,
                sortBy,
                direction,
                page,
                size
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserCreateRequestDto dto) {
        log.info("Creating new user with email {}", dto.getEmail());
        var responseDto = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload user image")
    public ResponseEntity<UploadImageResponseDto> uploadImage(@ModelAttribute @Valid UploadImageRequest request) {
        log.info("Uploading user image");
        var photoPath = service.updatePhoto(request.getImage());
        return ResponseEntity.ok(new UploadImageResponseDto(photoPath));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PutMapping
    public ResponseEntity<UserResponseDto> updateUser(@RequestBody @Valid UserUpdateRequestDto dto) {
        log.info("Updating user with id {}", dto.getId());
        var responseDto = service.update(dto);
        return ResponseEntity.ok(responseDto);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/joincode")
    public ResponseEntity<UserResponseDto> joinCode(@RequestBody @Valid UserJoinCodeRequestDto dto) {
        log.info("Join code from Organization for user id {}", dto.getId());
        var responseDto = service.joinCode(dto);
        return ResponseEntity.ok(responseDto);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/changePassword")
    public ResponseEntity<UserResponseDto> changePassword(@RequestBody @Valid UserChangePasswordRequestDto dto) {
        log.info("Changing password for user id {}", dto.getId());
        var responseDto = service.changePassword(dto);
        return ResponseEntity.ok(responseDto);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PatchMapping("/changeRole")
    public ResponseEntity<UserResponseDto> changeRole(@RequestBody @Valid UserChangeRoleRequestDto dto) {
        log.info("Changing role for user id {}", dto.getId());
        var responseDto = service.changeRole(dto);

        return ResponseEntity.ok(responseDto);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponseDto> deleteUser(@PathVariable Long id) {
        log.info("Deactivating user {}", id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authorities: " + auth.getAuthorities());
        System.out.println("Principal: " + auth.getPrincipal());
        var responseDto = service.deactivateUser(id);
        return ResponseEntity.ok(responseDto);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PatchMapping("/reactivate")
    public ResponseEntity<UserResponseDto> reactivate(@RequestBody @Valid ReactivateUserRequestDto dto) {
        log.info("Reactivating user by id {}", dto.getId());
        var responseDto = service.reactivateUser(dto.getId());
        return ResponseEntity.ok(responseDto);
    }
}
