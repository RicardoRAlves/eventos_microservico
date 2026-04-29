package com.br.capoeira.eventos.organization_api.dto;

import jakarta.validation.constraints.NotNull;

public record PromoteToSuperAdminDtoRequest(
        @NotNull(message = "User Id must be informed")
        Long userId,

        @NotNull(message = "Organization Id must be informed")
        Long organizationId,

        @NotNull(message = "Organization Unit Id must be informed")
        Long organizationUnitId
) {
}
