package com.br.capoeira.eventos.organization_api.controller;

import com.br.capoeira.eventos.organization_api.dto.*;
import com.br.capoeira.eventos.organization_api.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/organizacao")
public class OrganizationController {

    private final OrganizationService service;

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponseDto> findOrganizationById(@PathVariable("id") Long id) {
        log.info("Finding Organization by id {}", id);
        var response = service.findOrganizationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unit/{id}")
    public ResponseEntity<OrganizationUnitResponseDto> findOrganizationUnitById(@PathVariable Long id) {
        log.info("Finding Organization Unit by id {}", id);
        var response = service.findUnitById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unit/all/{organizationId}")
    public ResponseEntity<List<OrganizationUnitResponseDto>> findAllOrganizationUnitByOrganizationId(
            @PathVariable("organizationId") Long organizationId) {
        log.info("Finding all Organization Units by organization id {}", organizationId);
        var response = service.findAllByOrganizationId(organizationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unit/code/{code}")
    public ResponseEntity<OrganizationUnitResponseDto> findUnitByJoinCode(
            @PathVariable String code) {

        log.info("Finding Organization Unit by joinCode {}", code);

        var response = service.findByJoinCode(code);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<OrganizationResponseDto> createOrganization(
            @Valid @RequestBody OrganizationCreateRequestDto dto) {
        log.info("Creating organization with main unit {}", dto);
        var response = service.createWithMainUnit(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/unit")
    public ResponseEntity<OrganizationUnitResponseDto> createOrganizationUnit(
            @Valid @RequestBody OrganizationUnitDto dto) {
        log.info("Creating organization unit {}", dto);
        var response = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<OrganizationResponseDto> updateOrganization(
            @Valid @RequestBody OrganizationUpdateDto dto) {
        log.info("Updating organization {}", dto);
        var response = service.update(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/unit")
    public ResponseEntity<OrganizationUnitResponseDto> updateOrganizationUnit(
            @Valid @RequestBody OrganizationUnitUpdateDto dto) {
        log.info("Updating organization unit {}", dto);
        var response = service.update(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
