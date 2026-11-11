# organization_api

```yaml
document_id: QAK-SVC-ORG-001
service: organization_api
document_type: service
related_endpoints: [EP-ORG-001, EP-ORG-009]
related_business_rules: [BR-ORG-001, BR-ORG-005]
source_files: [organization_api/src/main/java, organization_api/src/main/resources/application.properties]
last_analyzed: 2026-11-09
confidence: high
```

Gerencia organizações e unidades em PostgreSQL. Todas as rotas exigem JWT; permissões por método variam entre autenticado, ADMIN e SUPER_ADMIN.

Na criação, salva organização e unidade principal numa transação local, gera join code de seis caracteres (alfabeto sem caracteres ambíguos, até 20 tentativas) e chama user_api com token SERVICE para promover o usuário a SUPER_ADMIN e associá-lo. O rollback local não compensa uma promoção remota já concluída.

Entidades: `organization` 1:N `organization_unit`. `join_code` é único. Datas são preenchidas em callbacks JPA. Não há endpoint de exclusão, rotação de join code ou listagem de organizações.

Upload exige multipart `image` e query `organizationName`, mas não associa automaticamente a URL retornada a uma entidade. S3 aceita endpoint compatível.

Falhas do user_api podem gerar `ServiceUnavailableException`, mas o advice local só trata `ValidationException` e validação bean; o status efetivo para essa exceção não está explicitamente definido e tende ao tratamento padrão 500.

