package com.br.capoeira.eventos.user_api.controller;

import com.br.capoeira.eventos.user_api.dto.InitialAdminSeedRequestDto;
import com.br.capoeira.eventos.user_api.dto.UserResponseDto;
import com.br.capoeira.eventos.user_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Profile("!prod & !production & (local | homolog | homologacao)")
@ConditionalOnProperty(
        prefix = "app.initial-data-seed",
        name = "enabled",
        havingValue = "true"
)
public class InitialAdminSeedController {

    private final UserService userService;

    @PostMapping("/initial-admin")
    @PreAuthorize("hasAuthority('ROLE_SERVICE')")
    public ResponseEntity<UserResponseDto> ensureInitialAdmin(
            @RequestBody @Valid InitialAdminSeedRequestDto request
    ) {
        return ResponseEntity.ok(userService.ensureInitialAdmin(request));
    }
}
