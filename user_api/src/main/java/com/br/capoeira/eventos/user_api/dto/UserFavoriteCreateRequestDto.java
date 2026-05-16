package com.br.capoeira.eventos.user_api.dto;

import com.br.capoeira.eventos.user_api.model.EventScope;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFavoriteCreateRequestDto {
    @NotNull(message = "User id must be informed")
    private Long userId;

    @NotNull(message = "Event id must be informed")
    private Long eventId;

    @NotNull(message = "Event scope must be informed")
    private EventScope eventScope;

    @NotNull(message = "Organization id must be informed")
    private Long organizationId;

    @NotNull(message = "Unit id must be informed")
    private Long organizationUnitId;
}