package unit.com.br.capoeira.eventos.event_api.service;

import com.br.capoeira.eventos.event_api.config.exception.ValidationException;
import com.br.capoeira.eventos.event_api.dto.CategoryCreateRequestDto;
import com.br.capoeira.eventos.event_api.dto.CategoryResponseDto;
import com.br.capoeira.eventos.event_api.dto.CategoryUpdateRequestDto;
import com.br.capoeira.eventos.event_api.mapper.CategoryMapper;
import com.br.capoeira.eventos.event_api.model.Category;
import com.br.capoeira.eventos.event_api.repository.CategoryRepository;
import com.br.capoeira.eventos.event_api.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository repository;

    @Mock
    private CategoryMapper mapper;

    @InjectMocks
    private CategoryService service;

    @Test
    void shouldFindAllCategoriesSuccessfully() {
        var category = getMockCategory(1L, "Capoeira", true);
        var responseDto = getMockCategoryResponseDto(1L, "Capoeira", true);

        var categoryPage = new PageImpl<>(
                List.of(category),
                PageRequest.of(0, 10, Sort.by("id").ascending()),
                1
        );

        when(repository.findAll(any(Pageable.class))).thenReturn(categoryPage);
        when(mapper.categoryToResponseDto(category)).thenReturn(responseDto);

        var response = service.findAll(0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(responseDto.getId(), response.getContent().get(0).getId());
        assertEquals(responseDto.getName(), response.getContent().get(0).getName());
        assertEquals(responseDto.getActive(), response.getContent().get(0).getActive());
        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getTotalPages());

        verify(repository).findAll(any(Pageable.class));
        verify(mapper).categoryToResponseDto(category);
    }

    @Test
    void shouldFindCategoryByIdSuccessfully() {
        var category = getMockCategory(1L, "Capoeira", true);
        var responseDto = getMockCategoryResponseDto(1L, "Capoeira", true);

        when(repository.findById(1L)).thenReturn(Optional.of(category));
        when(mapper.categoryToResponseDto(category)).thenReturn(responseDto);

        var response = service.findById(1L);

        assertNotNull(response);
        assertEquals(responseDto.getId(), response.getId());
        assertEquals(responseDto.getName(), response.getName());
        assertEquals(responseDto.getActive(), response.getActive());

        verify(repository).findById(1L);
        verify(mapper).categoryToResponseDto(category);
    }

    @Test
    void shouldThrowValidationExceptionWhenFindCategoryByIdAndIdIsNull() {
        var exception = assertThrows(ValidationException.class, () -> service.findById(null));

        assertEquals("Category id must be informed", exception.getMessage());

        verify(repository, never()).findById(any());
        verify(mapper, never()).categoryToResponseDto(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenFindCategoryByIdAndCategoryDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(ValidationException.class, () -> service.findById(1L));

        assertEquals("Category not found", exception.getMessage());

        verify(repository).findById(1L);
        verify(mapper, never()).categoryToResponseDto(any());
    }

    @Test
    void shouldFindCategoryByNameSuccessfully() {
        var category = getMockCategory(1L, "Capoeira", true);
        var responseDto = getMockCategoryResponseDto(1L, "Capoeira", true);

        when(repository.findByName("Capoeira")).thenReturn(Optional.of(category));
        when(mapper.categoryToResponseDto(category)).thenReturn(responseDto);

        var response = service.findByName("Capoeira");

        assertNotNull(response);
        assertEquals(responseDto.getId(), response.getId());
        assertEquals(responseDto.getName(), response.getName());
        assertEquals(responseDto.getActive(), response.getActive());

        verify(repository).findByName("Capoeira");
        verify(mapper).categoryToResponseDto(category);
    }

    @Test
    void shouldThrowValidationExceptionWhenFindCategoryByNameAndNameIsBlank() {
        var exception = assertThrows(ValidationException.class, () -> service.findByName(""));

        assertEquals("Category name must be informed", exception.getMessage());

        verify(repository, never()).findByName(any());
        verify(mapper, never()).categoryToResponseDto(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenFindCategoryByNameAndCategoryDoesNotExist() {
        when(repository.findByName("Capoeira")).thenReturn(Optional.empty());

        var exception = assertThrows(ValidationException.class, () -> service.findByName("Capoeira"));

        assertEquals("Category not found", exception.getMessage());

        verify(repository).findByName("Capoeira");
        verify(mapper, never()).categoryToResponseDto(any());
    }

    @Test
    void shouldCreateCategorySuccessfully() {
        var requestDto = getMockCategoryCreateRequestDto("Capoeira");
        var category = getMockCategory(null, "Capoeira", true);
        var savedCategory = getMockCategory(1L, "Capoeira", true);
        var responseDto = getMockCategoryResponseDto(1L, "Capoeira", true);

        when(repository.findByName("Capoeira")).thenReturn(Optional.empty());
        when(repository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        when(mapper.createRequestDtoToCategory(requestDto)).thenReturn(category);
        when(repository.save(any(Category.class))).thenReturn(savedCategory);
        when(mapper.categoryToResponseDto(savedCategory)).thenReturn(responseDto);

        var response = service.create(requestDto);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Capoeira", response.getName());
        assertTrue(response.getActive());

        verify(repository).findByName("Capoeira");
        verify(repository).findTopByOrderByIdDesc();
        verify(mapper).createRequestDtoToCategory(requestDto);
        verify(repository).save(any(Category.class));
        verify(mapper).categoryToResponseDto(savedCategory);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateCategoryAndDtoIsNull() {
        var exception = assertThrows(ValidationException.class, () -> service.create(null));

        assertEquals("Category data must be informed", exception.getMessage());

        verify(repository, never()).findByName(any());
        verify(repository, never()).save(any());
        verify(mapper, never()).createRequestDtoToCategory(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateCategoryAndNameIsBlank() {
        var requestDto = getMockCategoryCreateRequestDto("");

        var exception = assertThrows(ValidationException.class, () -> service.create(requestDto));

        assertEquals("Category name must be informed", exception.getMessage());

        verify(repository, never()).findByName(any());
        verify(repository, never()).save(any());
        verify(mapper, never()).createRequestDtoToCategory(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateCategoryAndNameAlreadyExists() {
        var requestDto = getMockCategoryCreateRequestDto("Capoeira");
        var existingCategory = getMockCategory(1L, "Capoeira", true);
        var category = getMockCategory(null, "Capoeira", true);

        when(mapper.createRequestDtoToCategory(requestDto)).thenReturn(category);
        when(repository.findByName(any())).thenReturn(Optional.of(existingCategory));

        var exception = assertThrows(ValidationException.class, () -> service.create(requestDto));

        assertEquals("Category already exists", exception.getMessage());

        verify(repository).findByName(any());
        verify(repository, never()).save(any());
        verify(mapper).createRequestDtoToCategory(any());
    }

    @Test
    void shouldUpdateCategorySuccessfully() {
        var requestDto = new CategoryUpdateRequestDto(1L, "Capoeira");
        var existingCategory = getMockCategory(1L, "Capoeira", true);
        var updatedCategory = getMockCategory(1L, "Capoeira", false);
        var responseDto = getMockCategoryResponseDto(1L, "Capoeira", false);

        when(repository.findById(any())).thenReturn(Optional.of(existingCategory));
        when(repository.findByName(any())).thenReturn(Optional.of(existingCategory));
        when(repository.save(existingCategory)).thenReturn(updatedCategory);
        when(mapper.updateRequestDto(requestDto)).thenReturn(existingCategory);
        when(mapper.categoryToResponseDto(updatedCategory)).thenReturn(responseDto);

        var response = service.update(requestDto);

        assertNotNull(response);
        assertEquals("Capoeira", response.getName());
        assertFalse(response.getActive());

        verify(repository).findById(any());
        verify(repository).findByName(any());
        verify(repository).save(existingCategory);
        verify(mapper).categoryToResponseDto(updatedCategory);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateCategoryAndDtoIsNull() {
        var exception = assertThrows(ValidationException.class, () -> service.update(null));
        //var existingCategory = getMockCategory(1L, "Capoeira", true);

        assertEquals("Category data must be informed", exception.getMessage());

        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateCategoryAndNameIsBlank() {
        var requestDto = getMockCategoryUpdateRequestDto(1L, "");

        var exception = assertThrows(ValidationException.class, () -> service.update(requestDto));

        assertEquals("Category name must be informed", exception.getMessage());

        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateCategoryAndCategoryDoesNotExist() {
        var requestDto = getMockCategoryUpdateRequestDto(1L, "Capoeira");
        var existingCategory = getMockCategory(1L, "Capoeira", true);

        when(repository.findById(any())).thenReturn(Optional.empty());
        when(mapper.updateRequestDto(requestDto)).thenReturn(existingCategory);

        var exception = assertThrows(ValidationException.class, () -> service.update(requestDto));

        assertEquals("Category not found", exception.getMessage());

        verify(repository).findById(any());
        verify(repository, never()).findByName(any());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldDeactivateCategorySuccessfully() {
        var category = getMockCategory(1L, "Capoeira", true);
        var updatedCategory = getMockCategory(1L, "Capoeira", false);
        var responseDto = getMockCategoryResponseDto(1L, "Capoeira", false);

        when(repository.findById(1L)).thenReturn(Optional.of(category));
        when(repository.save(category)).thenReturn(updatedCategory);
        when(mapper.categoryToResponseDto(updatedCategory)).thenReturn(responseDto);

        var response = service.deactivateCategory(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Capoeira", response.getName());
        assertFalse(response.getActive());

        verify(repository).findById(1L);
        verify(repository).save(category);
        verify(mapper).categoryToResponseDto(updatedCategory);
    }

    @Test
    void shouldThrowValidationExceptionWhenDeactivateCategoryAndIdIsNull() {
        var exception = assertThrows(ValidationException.class, () -> service.deactivateCategory(null));

        assertEquals("Category id must be informed", exception.getMessage());

        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenDeactivateCategoryAndCategoryDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(ValidationException.class, () -> service.deactivateCategory(1L));

        assertEquals("Category not found", exception.getMessage());

        verify(repository).findById(1L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenDeactivateCategoryAndCategoryIsAlreadyInactive() {
        var category = getMockCategory(1L, "Capoeira", false);

        when(repository.findById(1L)).thenReturn(Optional.of(category));

        var exception = assertThrows(ValidationException.class, () -> service.deactivateCategory(1L));

        assertEquals("Category is already inactive", exception.getMessage());

        verify(repository).findById(1L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldReactivateCategorySuccessfully() {
        var category = getMockCategory(1L, "Capoeira", false);
        var updatedCategory = getMockCategory(1L, "Capoeira", true);
        var responseDto = getMockCategoryResponseDto(1L, "Capoeira", true);

        when(repository.findByName("Capoeira")).thenReturn(Optional.of(category));
        when(repository.save(category)).thenReturn(updatedCategory);
        when(mapper.categoryToResponseDto(updatedCategory)).thenReturn(responseDto);

        var response = service.reactivateCategory("Capoeira");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Capoeira", response.getName());
        assertTrue(response.getActive());

        verify(repository).findByName("Capoeira");
        verify(repository).save(category);
        verify(mapper).categoryToResponseDto(updatedCategory);
    }

    @Test
    void shouldThrowValidationExceptionWhenReactivateCategoryAndNameIsBlank() {
        var exception = assertThrows(ValidationException.class, () -> service.reactivateCategory(""));

        assertEquals("Category name must be informed", exception.getMessage());

        verify(repository, never()).findByName(any());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenReactivateCategoryAndCategoryDoesNotExist() {
        when(repository.findByName("Capoeira")).thenReturn(Optional.empty());

        var exception = assertThrows(ValidationException.class, () -> service.reactivateCategory("Capoeira"));

        assertEquals("Category not found", exception.getMessage());

        verify(repository).findByName("Capoeira");
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenReactivateCategoryAndCategoryIsAlreadyActive() {
        var category = getMockCategory(1L, "Capoeira", true);

        when(repository.findByName("Capoeira")).thenReturn(Optional.of(category));

        var exception = assertThrows(ValidationException.class, () -> service.reactivateCategory("Capoeira"));

        assertEquals("Category is already active", exception.getMessage());

        verify(repository).findByName("Capoeira");
        verify(repository, never()).save(any());
    }

    private Category getMockCategory(Long id, String name, Boolean active) {
        return Category.builder()
                .id(id)
                .name(name)
                .active(active)
                .build();
    }

    private CategoryResponseDto getMockCategoryResponseDto(Long id, String name, Boolean active) {
        return CategoryResponseDto.builder()
                .id(id)
                .name(name)
                .active(active)
                .build();
    }

    private CategoryCreateRequestDto getMockCategoryCreateRequestDto(String name) {
        return new CategoryCreateRequestDto(name);
    }

    private CategoryUpdateRequestDto getMockCategoryUpdateRequestDto(Long id, String name) {
        return new CategoryUpdateRequestDto(id, name);
    }
}
