package com.br.capoeira.eventos.user_api.dto;

import com.br.capoeira.eventos.user_api.model.EventScope;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFavoriteResponseDto {

    private Long id;

    private Long userId;

    private Long eventId;

    private EventScope eventScope;

    private Long organizationId;

    private Long organizationUnitId;

    private LocalDateTime createdAt;
}
