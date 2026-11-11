# event_api

```yaml
document_id: QAK-SVC-EVT-001
service: event_api
document_type: service
related_endpoints: [EP-EVT-001, EP-EVT-015]
related_business_rules: [BR-EVT-001, BR-EVT-006, BR-CAT-001, BR-SALE-001]
source_files: [event_api/src/main/java, event_api/src/main/resources/application.properties, event_api/src/main/resources/seed/categories.json]
last_analyzed: 2026-11-09
confidence: high
```

Serviço REST de comandos de evento, categoria e item de venda. Todos os caminhos, salvo Swagger/actuator/OPTIONS, exigem JWT; roles são extraídas de `roles` com prefixo `ROLE_`. EventSaleController não tem `@PreAuthorize`, portanto aceita qualquer JWT autenticado.

Responsabilidades confirmadas:

- CRUD lógico de categorias e seed transacional quando a coleção está vazia;
- criação/alteração/exclusão lógica de eventos em MongoDB e publicação assíncrona;
- validação síncrona de unidade organizacional via organization_api;
- criação/alteração/exclusão lógica de itens de venda em MongoDB e publicação;
- upload de imagem S3;
- consumo do erro de criação do processor, inativação local e notificação de erro.

Persistência: coleções `event`, `category`, `event_sale_items` e `application_lock`. O ID Mongo de `Event` é o `transactionId`; `EventRepository` está tipado incorretamente como `MongoRepository<Event, Long>`, embora o `@Id` seja `String` (inconsistência rastreada).

O create persiste antes de publicar e retorna 201 antes do processamento downstream. Não existe outbox; falha após `save` pode deixar registro local sem mensagem, enquanto falha downstream só é compensada para create por um fluxo de erro. Update/delete não têm compensação equivalente completa.

Seed: esquema 1, uma categoria `capoeira`; importação só se coleção vazia, lock distribuído com lease/renovação, transação Mongo obrigatória e falha sem fallback não transacional (`CategorySeedService` 67-140; `CategorySeedTransactionalImporter` 21-34).

Rotas e contratos: `api/endpoints.json`. Regras: `business-rules/rules.json`.

