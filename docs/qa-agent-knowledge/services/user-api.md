# user_api

```yaml
document_id: QAK-SVC-USR-001
service: user_api
document_type: service
related_endpoints: [EP-USR-001, EP-USR-023]
related_business_rules: [BR-USR-001, BR-USR-008, BR-FAV-001, BR-RES-001]
source_files: [user_api/src/main/java, user_api/src/main/resources/application.properties]
last_analyzed: 2026-11-09
confidence: high
```

Serviço de identidade, autorização, favoritos e reservas. Cadastro e upload de avatar são públicos; login emite JWT; demais rotas exigem autenticação. A senha usa `DelegatingPasswordEncoder`; o JWT contém `roles`, `userId` e opcionalmente `organizationId`, mas não `organizationUnitId`.

Roles confirmadas: `CLIENT`, `ADMIN`, `SUPER_ADMIN`, `SERVICE`. Cadastro sempre força CLIENT e ativo. Mudança de role/deativação impede agir sobre o próprio usuário, mas update, mudança de senha, join code, favoritos e reservas não verificam no service se o ID do corpo/path corresponde ao principal autenticado. Isso é um risco de autorização horizontal.

Join code só é aceito quando `organizationId` do usuário é nulo; consulta organization_api com o bearer recebido e grava organização/unidade. Não há fluxo de saída/troca de organização.

Favoritos possuem unicidade `(user_id,event_id)` e limpeza assíncrona por `eventTransactionId`. Reserva HTTP apenas publica pedido e retorna 202; processor valida a existência pelo transactionId e devolve payload enriquecido; user_api persiste idempotentemente por `(user_id,event_sale_id)`. Não há decremento de estoque nem validação de quantidade disponível.

Entidades: `users`, `user_favorite_events`, `user_reservation_events`. Não existem FKs JPA para IDs remotos de eventos/organizações.

