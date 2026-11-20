
ALTER TABLE knowledge_document
    ADD COLUMN visibility VARCHAR(20);

UPDATE knowledge_document
SET visibility = 'PRIVATE';

ALTER TABLE knowledge_document
    ALTER COLUMN visibility SET DEFAULT 'PUBLIC';

ALTER TABLE knowledge_document
    ALTER COLUMN visibility SET NOT NULL;

ALTER TABLE knowledge_document
    ADD CONSTRAINT chk_knowledge_document_visibility
        CHECK (visibility IN ('PUBLIC', 'SHARED', 'PRIVATE'));
