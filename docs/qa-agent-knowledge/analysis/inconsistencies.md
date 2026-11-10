# Inconsistências encontradas

```yaml
document_id: QAK-ANA-002
service: all
document_type: inconsistencies
related_endpoints: [EP-EVT-002, EP-USR-006, EP-USR-022]
related_business_rules: [BR-EVT-004, BR-RES-003]
source_files: [event_api/src/main/java, user_api/src/main/java, processor_api/src/main/java, notification_api/src/main/java, docs]
last_analyzed: 2026-11-09
confidence: high
```

- `INC-001`: `event_api.StandardException` tem ordem de campos diferente da usada pelo handler; `error/path` ficam semanticamente trocados.
- `INC-002`: lógica de escopo permite PUBLIC sem IDs, mas DTO create/update exige organizationId e organizationUnitId.
- `INC-003`: processor `Event` declara organizationId/unitId não nulos, reforçando conflito com PUBLIC sem vínculo.
- `INC-004`: `EventRepository` usa `MongoRepository<Event, Long>`, mas `@Id transactionId` é String.
- `INC-005`: item de venda permite value=0; reserva validada exige value>0.
- `INC-006`: delete de venda é lógico em Mongo/PostgreSQL e físico no Firestore.
- `INC-007`: full sync é descrito como reconciliação, mas só faz merge e não remove documentos ausentes.
- `INC-008`: handler de DataIntegrityViolation retorna HTTP 409, porém corpo declara status 400.
- `INC-009`: organization_api lança `ServiceUnavailableException`, mas seu advice não a trata.
- `INC-010`: EventSaleController não restringe roles, diferentemente de eventos/categorias administrativas.
- `INC-011`: favorito create retorna 200, enquanto outras criações retornam 201; comportamento confirmado, sem inferir correção.
- `INC-012`: update é salvo no Mongo antes do processor; falha downstream apenas envia ERROR_UPDATE e não reverte o Mongo.
- `INC-013`: pedido de reserva envia eventSaleId, mas processor seleciona por transactionId e não compara os IDs.
- `INC-014`: S3 event/user aceitam qualquer content type/nome válido; organization restringe/inferre tipos de imagem.
- `INC-015`: documentação legada afirma manutenção de consistência, mas não há outbox/saga/retry/DLQ que a garanta.
- `INC-016`: versões Spring Boot diferem (3.5.11 e 3.5.13) entre serviços.
- `INC-017`: user_api usa JWT filter próprio; event/organization usam Resource Server, criando caminhos de erro/validação diferentes.
- `INC-018`: delete de evento inexistente retorna 204 sem mensagem; update inexistente retorna erro.

