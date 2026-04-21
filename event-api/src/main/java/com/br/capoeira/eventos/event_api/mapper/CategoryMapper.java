package com.br.capoeira.eventos.event_api.mapper;

import com.br.capoeira.eventos.event_api.dto.CategoryCreateRequestDto;
import com.br.capoeira.eventos.event_api.dto.CategoryResponseDto;
import com.br.capoeira.eventos.event_api.dto.CategoryUpdateRequestDto;
import com.br.capoeira.eventos.event_api.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Mapper(componentModel = "spring")
@Component
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name", qualifiedByName = "toUpper")
    @Mapping(target = "active", constant = "true")
    Category createRequestDtoToCategory(CategoryCreateRequestDto input);

    @Mapping(target = "name", source = "name", qualifiedByName = "toUpper")
    Category updateRequestDto(CategoryUpdateRequestDto input);

    @Mapping(target = "name", source = "name", qualifiedByName = "toUpper")
    CategoryResponseDto categoryToResponseDto(Category input);

    @Named("toUpper")
    default String toUpper(String value) {
        return value != null ? value.toUpperCase(Locale.ROOT) : null;
    }
}
