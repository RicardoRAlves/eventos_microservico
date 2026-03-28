package com.br.capoeira.eventos.evento_api.mapper;

import com.br.capoeira.eventos.evento_api.dto.EventDto;
import com.br.capoeira.eventos.evento_api.model.Event;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface EventoMapper {

    Event eventoDtoToEvento(EventDto input);
}
