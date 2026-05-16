package com.br.capoeira.eventos.user_api.mapper;

import com.br.capoeira.eventos.user_api.dto.UserFavoriteCreateRequestDto;
import com.br.capoeira.eventos.user_api.dto.UserFavoriteResponseDto;
import com.br.capoeira.eventos.user_api.model.UserFavoriteEvents;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserFavoriteEventsMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    UserFavoriteEvents createRequestDtoToUserFavoriteEvents(
            UserFavoriteCreateRequestDto dto
    );

    UserFavoriteResponseDto userFavoriteEventsToResponseDto(
            UserFavoriteEvents userFavoriteEvents
    );

    List<UserFavoriteResponseDto> userFavoriteEventsListToResponseDtoList(
            List<UserFavoriteEvents> userFavoriteEvents
    );
}
