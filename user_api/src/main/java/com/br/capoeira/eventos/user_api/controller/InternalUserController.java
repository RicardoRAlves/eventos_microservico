package com.br.capoeira.eventos.user_api.controller;

import com.br.capoeira.eventos.user_api.dto.PromoteToSuperAdminDtoRequest;
import com.br.capoeira.eventos.user_api.dto.UserResponseDto;
import com.br.capoeira.eventos.user_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService service;

    @PatchMapping("/promote-to-super-admin")
    @PreAuthorize("hasAuthority('ROLE_SERVICE')")
    public ResponseEntity<UserResponseDto> promoteToSuperAdmin(
            @RequestBody @Valid PromoteToSuperAdminDtoRequest request
    ) {
        UserResponseDto response = service.promoteToSuperAdmin(request);
        return ResponseEntity.ok(response);
    }
}