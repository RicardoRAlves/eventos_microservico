package com.br.capoeira.eventos.organization_api.controller;

import com.br.capoeira.eventos.organization_api.dto.OrganizationDto;
import com.br.capoeira.eventos.organization_api.dto.OrganizationUnitDto;
import com.br.capoeira.eventos.organization_api.dto.OrganizationUnitResponseDto;
import com.br.capoeira.eventos.organization_api.model.Organization;
import com.br.capoeira.eventos.organization_api.model.OrganizationUnit;
import com.br.capoeira.eventos.organization_api.service.OrganizationService;
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
    public ResponseEntity<Organization> findOrganizationById(@PathVariable("id") Long id) {
        var optOrganization = service.findById(id);

        if (optOrganization.isPresent()) {
            log.info("Finding Organization by id {}", optOrganization.get());
            return ResponseEntity.ok(optOrganization.get());
        } else {
            log.info("Organization not found for id {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Organization());
        }
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
        var organizationUnits = service.findAllByOrganizationId(organizationId);
        return ResponseEntity.ok(organizationUnits);
    }

    @PostMapping
    public ResponseEntity<Organization> createOrganization(@RequestBody OrganizationDto dto) {
        log.info("New Organization created, {}", dto);
        var organization = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(organization);
    }

    @PostMapping("/unit")
    public ResponseEntity<OrganizationUnitResponseDto> createOrganization(@RequestBody OrganizationUnitDto dto) {
        log.info("New OrganizationUnit created, {}", dto);
        var response = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<Organization> updateOrganization(@RequestBody Organization organization) {
        log.info("Organization updated, {}", organization);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.update(organization));
    }

    @PutMapping("/unit")
    public ResponseEntity<OrganizationUnitResponseDto> updateOrganization(@RequestBody OrganizationUnit organizationUnit) {
        log.info("OrganizationUnit updated, {}", organizationUnit);
        var response = service.update(organizationUnit);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
