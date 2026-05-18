package com.br.capoeira.eventos.user_api.mapper;

import com.br.capoeira.eventos.user_api.dto.UserReservationEventResponseDto;
import com.br.capoeira.eventos.user_api.dto.UserReservationEventValidatedMessageDto;
import com.br.capoeira.eventos.user_api.model.UserReservationEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserReservationEventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    UserReservationEvent validatedMessageDtoToEntity(
            UserReservationEventValidatedMessageDto input
    );

    UserReservationEventResponseDto entityToResponseDto(
            UserReservationEvent input
    );
}
