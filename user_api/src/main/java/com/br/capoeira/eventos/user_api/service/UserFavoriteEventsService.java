package com.br.capoeira.eventos.user_api.service;

import com.br.capoeira.eventos.user_api.config.exception.ValidationException;
import com.br.capoeira.eventos.user_api.dto.UserFavoriteCreateRequestDto;
import com.br.capoeira.eventos.user_api.dto.UserFavoriteDeleteDto;
import com.br.capoeira.eventos.user_api.dto.UserFavoriteResponseDto;
import com.br.capoeira.eventos.user_api.mapper.UserFavoriteEventsMapper;
import com.br.capoeira.eventos.user_api.repository.UserFavoriteEventsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFavoriteEventsService {

    private final UserFavoriteEventsRepository repository;
    private final UserFavoriteEventsMapper mapper;

    @Transactional(readOnly = true)
    public List<UserFavoriteResponseDto> findAllEventsByUserId(Long userId) {
        validateUserId(userId);

        var entity = repository.findAllByUserId(userId);

        return mapper.userFavoriteEventsListToResponseDtoList(entity);
    }

    @Transactional
    public UserFavoriteResponseDto createFavorite(
            UserFavoriteCreateRequestDto dto
    ) {
        validateCreateFavorite(dto);

        if (repository.existsByUserIdAndEventId(
                dto.getUserId(),
                dto.getEventId()
        )) {
            throw new ValidationException(
                    "Event is already favorited by user"
            );
        }

        var entity = mapper.createRequestDtoToUserFavoriteEvents(dto);

        return mapper.userFavoriteEventsToResponseDto(
                repository.save(entity)
        );
    }

    @Transactional
    public void deleteFavoriteById(UserFavoriteDeleteDto dto) {
        validateDeleteFavorite(dto);

        if (repository.existsByUserIdAndEventId(
                dto.getUserId(),
                dto.getEventId()
        )) {

            repository.deleteByUserIdAndEventId(
                    dto.getUserId(),
                    dto.getEventId()
            );
        }
    }

    private void validateCreateFavorite(
            UserFavoriteCreateRequestDto dto
    ) {

        if (dto.getUserId() == null || dto.getUserId() <= 0) {
            throw new ValidationException("Invalid user id");
        }

        if (dto.getEventId() == null || dto.getEventId() <= 0) {
            throw new ValidationException("Invalid event id");
        }

        if (dto.getEventScope() == null) {
            throw new ValidationException("Event scope must be informed");
        }

        if (dto.getOrganizationId() == null
                || dto.getOrganizationId() <= 0) {

            throw new ValidationException("Invalid organization id");
        }

        if (dto.getOrganizationUnitId() == null
                || dto.getOrganizationUnitId() <= 0) {

            throw new ValidationException("Invalid organization unit id");
        }
    }

    private void validateDeleteFavorite(
            UserFavoriteDeleteDto dto
    ) {

        if (dto.getUserId() == null || dto.getUserId() <= 0) {
            throw new ValidationException("Invalid user id");
        }

        if (dto.getEventId() == null || dto.getEventId() <= 0) {
            throw new ValidationException("Invalid event id");
        }
    }

    private void validateUserId(Long userId) {

        if (userId == null || userId <= 0) {
            throw new ValidationException("Invalid user id");
        }
    }
}
