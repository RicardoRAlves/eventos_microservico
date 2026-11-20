package com.br.capoeira.eventos.organization_api.seed;

public record BonfimSeedManifest(
        String source,
        OrganizationData organization,
        UnitData unit
) {
    public record OrganizationData(
            String name,
            String expectedKey,
            String description
    ) {
    }

    public record UnitData(
            String name,
            String description,
            String city,
            String state,
            String country,
            String address,
            String contactEmail,
            String documentedResponsible,
            boolean contactDataRequiresCurrentConfirmation
    ) {
    }
}
