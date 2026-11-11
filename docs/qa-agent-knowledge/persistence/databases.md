# Bancos e persistência

```yaml
document_id: QAK-PER-002
service: all
document_type: databases
related_endpoints: [EP-EVT-002, EP-ORG-005, EP-USR-006]
related_business_rules: [BR-EVT-001, BR-PRC-001, BR-NTF-001]
source_files: [docker/postgres/init/01-init-databases.sql, event_api/src/main/resources/application.properties, organization_api/src/main/resources/application.properties, user_api/src/main/resources/application.properties, processor_api/src/main/resources/application.properties]
last_analyzed: 2026-11-09
confidence: high
```

| Banco | Dono lógico | Responsabilidade |
|---|---|---|
| MongoDB `event_api` | event_api | comando/origem imediata de eventos, categorias, vendas e lock do seed |
| PostgreSQL `organization_api` | organization_api | organizações e unidades |
| PostgreSQL `user_api` | user_api | identidade, favoritos e reservas |
| PostgreSQL `eventos_api` | processor_api | projeção relacional de eventos/categorias/vendas |
| Firestore | notification_api | projeção móvel `events_v2` e `event_sale_items_v1` |

Todas as três aplicações JPA usam `ddl-auto=update`. Não foram encontrados Flyway/Liquibase nem scripts de schema de domínio. O único SQL cria bancos vazios.

Inconsistências de contrato/modelo:

- DTOs de evento exigem organizationId e organizationUnitId, inclusive PUBLIC; a lógica aceita PUBLIC sem vínculo, e o processor possui colunas não nulas para ambos.
- `event_api.EventRepository` declara ID genérico Long, mas a entidade usa String transactionId.
- venda aceita valor zero; reserva rejeita valor zero.
- Firestore converte BigDecimal da venda para Double, com possível perda de precisão.
- exclusão de venda é lógica em Mongo/PostgreSQL e física no Firestore.
- full sync Firestore faz merge e não remove documentos obsoletos.
- IDs remotos em user_api não têm foreign keys, logo integridade depende de mensagens e clientes.

Não foram consultados dados reais nem metadados runtime dos bancos.

