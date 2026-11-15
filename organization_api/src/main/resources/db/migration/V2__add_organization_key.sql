DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM organization) THEN
        RAISE EXCEPTION
            'V2 requires an empty organization table; prepare existing records before applying organization_key';
    END IF;
END
$$;

ALTER TABLE organization
    ADD COLUMN organization_key VARCHAR(120) NOT NULL;

ALTER TABLE organization
    ADD CONSTRAINT uk_organization_organization_key UNIQUE (organization_key);
