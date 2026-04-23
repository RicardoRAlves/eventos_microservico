package com.br.capoeira.eventos.organization_api.controller;

import com.br.capoeira.eventos.organization_api.dto.*;
import com.br.capoeira.eventos.organization_api.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizacao")
public class OrganizationController {

    private final OrganizationService service;

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrganizationResponseDto> findOrganizationById(@PathVariable("id") Long id) {
        log.info("Finding Organization by id {}", id);
        var response = service.findOrganizationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unit/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrganizationUnitResponseDto> findOrganizationUnitById(@PathVariable Long id) {
        log.info("Finding Organization Unit by id {}", id);
        var response = service.findUnitById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unit/all/{organizationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponseDto<OrganizationUnitResponseDto>> findAllOrganizationUnitByOrganizationId(
            @PathVariable("organizationId") Long organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Finding all Organization Units by organization id {}", organizationId);
        var response = service.findAllByOrganizationId(organizationId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unit/code/{code}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrganizationUnitResponseDto> findUnitByJoinCode(
            @PathVariable String code) {

        log.info("Finding Organization Unit by joinCode {}", code);

        var response = service.findByJoinCode(code);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponseDto> createOrganization(
            @Valid @RequestBody OrganizationCreateRequestDto dto) {
        log.info("Creating organization with main unit {}", dto);
        var response = service.createWithMainUnit(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/unit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationUnitResponseDto> createOrganizationUnit(
            @Valid @RequestBody OrganizationUnitDto dto) {
        log.info("Creating organization unit {}", dto);
        var response = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponseDto> updateOrganization(
            @Valid @RequestBody OrganizationUpdateDto dto) {
        log.info("Updating organization {}", dto);
        var response = service.update(dto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/unit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationUnitResponseDto> updateOrganizationUnit(
            @Valid @RequestBody OrganizationUnitUpdateDto dto) {
        log.info("Updating  a organization unit {}", dto);
        var response = service.update(dto);
        return ResponseEntity.ok(response);
    }
}
