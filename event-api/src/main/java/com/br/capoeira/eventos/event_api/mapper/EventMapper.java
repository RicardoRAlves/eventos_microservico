package com.br.capoeira.eventos.event_api.mapper;

import com.br.capoeira.eventos.event_api.dto.EventCreateRequestDto;
import com.br.capoeira.eventos.event_api.dto.EventResponseDto;
import com.br.capoeira.eventos.event_api.dto.EventUpdateRequestDto;
import com.br.capoeira.eventos.event_api.model.Category;
import com.br.capoeira.eventos.event_api.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Mapper(componentModel = "spring")
@Component
public interface EventMapper {

    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "categoryName", source = "categoryName", qualifiedByName = "toUpper")
    @Mapping(target = "active", constant = "true")
    Event createRequestDtoToEvent(EventCreateRequestDto input);

    @Mapping(target = "categoryName", source = "categoryName", qualifiedByName = "toUpper")
    Event updateRequestDtoToEvent(EventUpdateRequestDto input);

    @Mapping(target = "categoryName", source = "category.name", qualifiedByName = "toUpper")
    @Mapping(target = "active", source = "event.active")
    EventResponseDto eventToResponseDto(Event event, Category category);

    @Named("toUpper")
    default String toUpper(String value) {
        return value != null ? value.toUpperCase(Locale.ROOT) : null;
    }
}
