ALTER TABLE organization
    ALTER COLUMN description TYPE VARCHAR(1000);

ALTER TABLE organization_unit
    ALTER COLUMN description TYPE VARCHAR(1000);

ALTER TABLE organization_unit
    ADD CONSTRAINT uk_organization_unit_organization_name
        UNIQUE (organization_id, name);
