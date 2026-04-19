package com.br.capoeira.eventos.user_api.mapper;

import com.br.capoeira.eventos.user_api.dto.UserCreateRequestDto;
import com.br.capoeira.eventos.user_api.dto.UserResponseDto;
import com.br.capoeira.eventos.user_api.dto.UserUpdateRequestDto;
import com.br.capoeira.eventos.user_api.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "organizationUnitId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User createRequestDtoToUser(UserCreateRequestDto dto);

    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User updateRequestDtoToUser(UserUpdateRequestDto dto);

    UserResponseDto userToResponseDto(User user);
}
