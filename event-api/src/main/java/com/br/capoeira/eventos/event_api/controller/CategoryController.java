package com.br.capoeira.eventos.event_api.controller;

import com.br.capoeira.eventos.event_api.dto.CategoryCreateRequestDto;
import com.br.capoeira.eventos.event_api.dto.CategoryReactivateRequestDto;
import com.br.capoeira.eventos.event_api.dto.CategoryResponseDto;
import com.br.capoeira.eventos.event_api.dto.CategoryUpdateRequestDto;
import com.br.capoeira.eventos.event_api.service.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/categoria")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryResponseDto>> findAll() {
        var categories = categoryService.findAll();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> findById(@PathVariable Long id) {
        log.info("Finding Category by id {}", id);
        var category = categoryService.findById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/name/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> findByName(@PathVariable String name) {
        log.info("Finding Category by name {}", name);
        var category = categoryService.findByName(name);
        return ResponseEntity.ok(category);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> createCategory(
            @Valid @RequestBody CategoryCreateRequestDto dto) {
        log.info("Creating Category {}", dto);
        var response = categoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @Valid @RequestBody CategoryUpdateRequestDto dto) {
        log.info("Updating Category {}", dto);
        var response = categoryService.update(dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> deleteCategory(@PathVariable Long id) {
        log.info("Deactivating category {}", id);
        var responseDto = categoryService.deactivateCategory(id);
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> reactivate(
            @RequestBody @Valid CategoryReactivateRequestDto dto) {
        log.info("Reactivating category by name {}", dto.getName());
        var responseDto = categoryService.reactivateCategory(dto.getName());
        return ResponseEntity.ok(responseDto);
    }
}
