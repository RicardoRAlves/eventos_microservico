package com.br.capoeira.eventos.event_api.service;

import com.br.capoeira.eventos.event_api.config.exception.ValidationException;
import com.br.capoeira.eventos.event_api.dto.CategoryCreateRequestDto;
import com.br.capoeira.eventos.event_api.dto.CategoryResponseDto;
import com.br.capoeira.eventos.event_api.dto.CategoryUpdateRequestDto;
import com.br.capoeira.eventos.event_api.mapper.CategoryMapper;
import com.br.capoeira.eventos.event_api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    public List<CategoryResponseDto> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::categoryToResponseDto)
                .toList();
    }

    public CategoryResponseDto findById(Long id) {
        if (id == null) {
            throw new ValidationException("Category id must be informed");
        }

        return repository.findById(id)
                .map(mapper::categoryToResponseDto)
                .orElseThrow(() -> new ValidationException("Category not found"));
    }

    public CategoryResponseDto findByName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Category name must be informed");
        }

        return repository.findByName(name)
                .map(mapper::categoryToResponseDto)
                .orElseThrow(() -> new ValidationException("Category not found"));
    }

    public CategoryResponseDto create(CategoryCreateRequestDto dto) {
        validateCreate(dto);

        var category = mapper.createRequestDtoToCategory(dto);

        repository.findByName(category.getName())
                .ifPresent(cat -> {
                    throw new ValidationException("Category already exists");
                });



        var nextId = repository.findTopByOrderByIdDesc()
                .map(savedCategory -> savedCategory.getId() + 1)
                .orElse(1L);

        category.setId(nextId);
        category.setActive(true);

        var savedCategory = repository.save(category);

        log.info("Category created successfully. id={}, name={}", savedCategory.getId(), savedCategory.getName());

        return mapper.categoryToResponseDto(savedCategory);
    }

    public CategoryResponseDto update(CategoryUpdateRequestDto dto) {
        validateUpdate(dto);

        var category = mapper.updateRequestDto(dto);

        var savedCategory = repository.findById(category.getId())
                .orElseThrow(() -> new ValidationException("Category not found"));

        var validateCategory = repository.findByName(category.getName());

        if ((validateCategory.isPresent()) && (!Objects.equals(validateCategory.get().getId(), category.getId())) ){
            throw new ValidationException("Category already exists");
        }

        var updatedCategory = repository.save(savedCategory);

        return mapper.categoryToResponseDto(updatedCategory);
    }

    public CategoryResponseDto deactivateCategory(Long id) {
        if (id == null) {
            throw new ValidationException("Category id must be informed");
        }

        var savedCategory = repository.findById(id)
                .orElseThrow(() -> new ValidationException("Category not found"));

        if (Boolean.FALSE.equals(savedCategory.getActive())) {
            throw new ValidationException("Category is already inactive");
        }

        savedCategory.setActive(false);

        var updatedCategory = repository.save(savedCategory);

        log.info("Category deactivated successfully. id={}, name={}", updatedCategory.getId(), updatedCategory.getName());

        return mapper.categoryToResponseDto(updatedCategory);
    }

    public CategoryResponseDto reactivateCategory(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Category name must be informed");
        }

        var savedCategory = repository.findByName(name)
                .orElseThrow(() -> new ValidationException("Category not found"));

        if (Boolean.TRUE.equals(savedCategory.getActive())) {
            throw new ValidationException("Category is already active");
        }

        savedCategory.setActive(true);

        var updatedCategory = repository.save(savedCategory);

        log.info("Category reactivated successfully. id={}, name={}", updatedCategory.getId(), updatedCategory.getName());

        return mapper.categoryToResponseDto(updatedCategory);
    }

    private void validateCreate(CategoryCreateRequestDto dto) {
        if (dto == null) {
            throw new ValidationException("Category data must be informed");
        }

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ValidationException("Category name must be informed");
        }
    }

    private void validateUpdate(CategoryUpdateRequestDto dto) {
        if (dto == null) {
            throw new ValidationException("Category data must be informed");
        }

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ValidationException("Category name must be informed");
        }

        if (dto.getId() == null) {
            throw new ValidationException("Category id must be informed");
        }
    }
}
