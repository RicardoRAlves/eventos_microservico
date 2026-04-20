package com.br.capoeira.eventos.processor_api.mapper;

import com.br.capoeira.eventos.processor_api.dto.EventRequestDto;
import com.br.capoeira.eventos.processor_api.dto.EventResponseDto;
import com.br.capoeira.eventos.processor_api.entities.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Event requestDtoToEvent(EventRequestDto dto);

    @Mapping(target = "categoryName", source = "category.name")
    EventResponseDto eventToResponseDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEventFromDto(EventRequestDto dto, @MappingTarget Event event);
}
