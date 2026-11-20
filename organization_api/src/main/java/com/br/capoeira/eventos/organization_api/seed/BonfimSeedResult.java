package com.br.capoeira.eventos.organization_api.seed;

public record BonfimSeedResult(
        Long userId,
        Long organizationId,
        Long organizationUnitId,
        boolean organizationCreated,
        boolean organizationUnitCreated,
        boolean userLinkUpdated
) {
}
