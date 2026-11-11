# Dependências entre serviços

```yaml
document_id: QAK-ARCH-002
service: all
document_type: dependencies
related_endpoints: [EP-EVT-002, EP-ORG-005, EP-USR-008]
related_business_rules: [BR-EVT-004, BR-ORG-002, BR-USR-006, BR-MSG-001]
source_files: [event_api/src/main/java/com/br/capoeira/eventos/event_api/restClient/OrganizationClient.java, organization_api/src/main/java/com/br/capoeira/eventos/organization_api/restClient/UserClient.java, user_api/src/main/java/com/br/capoeira/eventos/user_api/restClient/OrganizationClient.java]
last_analyzed: 2026-11-09
confidence: high
```

## Síncronas confirmadas

| Origem | Destino | Chamada | Autorização | Falha |
|---|---|---|---|---|
| event_api | organization_api | GET `/api/v1/organizacao/unit/{id}` ao validar escopo | repassa bearer do usuário quando o principal é `JwtAuthenticationToken` | 404/401/4xx viram `ValidationException`; 5xx/conexão viram 503 |
| user_api | organization_api | GET `/api/v1/organizacao/unit/code/{joinCode}` | repassa header Authorization da requisição | 404/401/outros erros viram 400/404 conforme mensagem/handler |
| organization_api | user_api | PATCH `/internal/users/promote-to-super-admin` | token HMAC próprio com `roles=[SERVICE]` | 4xx de negócio; 5xx/conexão viram 503, mas o handler do organization_api não trata explicitamente `ServiceUnavailableException` |

## Assíncronas confirmadas

`event_api -> processor_api -> notification_api` cobre create/update/delete/full sync de eventos e create/update/delete de venda. `user_api -> processor_api -> user_api` cobre reservas. Exclusões publicadas pelo processor também chegam a listeners do user_api para limpar favoritos/reservas porque exchanges são fanout. Detalhes em `messaging/message-contracts.json`.

## Dependências externas

- S3 ou endpoint compatível: uploads de evento, organização e usuário.
- Firebase Admin SDK: Firestore e FCM.
- MongoDB: escrita de comandos e seed de categorias.
- PostgreSQL: três bancos lógicos.
- RabbitMQ: fanout, JSON/Jackson.

Não há circuit breaker, timeout explícito do `RestClient`, service discovery ou gateway no código analisado.

