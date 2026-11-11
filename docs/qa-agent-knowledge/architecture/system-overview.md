# Visão geral do sistema

```yaml
document_id: QAK-ARCH-001
service: all
document_type: architecture
related_endpoints: [EP-EVT-001, EP-ORG-005, EP-USR-001]
related_business_rules: [BR-EVT-001, BR-ORG-001, BR-MSG-001]
source_files: [docker-compose.yml, event_api/src/main/resources/application.properties, organization_api/src/main/resources/application.properties, user_api/src/main/resources/application.properties, processor_api/src/main/resources/application.properties, notification_api/src/main/resources/application.properties]
last_analyzed: 2026-11-09
confidence: high
```

O sistema usa uma API de escrita de eventos em MongoDB (`event_api`), uma projeção relacional processada assincronamente (`processor_api`/PostgreSQL), uma projeção móvel e notificações (`notification_api`/Firestore+FCM), cadastro organizacional (`organization_api`/PostgreSQL) e identidade/favoritos/reservas (`user_api`/PostgreSQL).

| Serviço | Porta padrão | Responsabilidade confirmada | Persistência | Interfaces |
|---|---:|---|---|---|
| event_api | 8080 | categorias, comandos de evento e itens de venda, upload S3 | MongoDB `event_api` | 15 REST; 2 producers/8 envios; 1 consumer |
| organization_api | 8081 | organizações, unidades, join code, promoção inicial | PostgreSQL `organization_api` | 9 REST; REST para user_api; S3 |
| user_api | 8082 | cadastro/login/JWT, perfil/roles, favoritos e reservas | PostgreSQL `user_api` | 23 REST; REST para organization_api; 1 producer; 3 listeners; S3 |
| processor_api | 8083 | projeção relacional de eventos/vendas e validação de reserva | PostgreSQL `eventos_api` | sem REST; 8 listeners; 10 envios |
| notification_api | 8084 | projeção Firestore e push FCM | Firestore | sem REST; 9 listeners |

O fluxo normal de criação é eventual: `event_api` persiste primeiro no MongoDB e publica; `processor_api` persiste no PostgreSQL e publica; `notification_api` grava no Firestore e envia FCM. Não há outbox, saga, confirmação de publisher nem reconciliação automática comprovada.

Autenticação é JWT HMAC compartilhado por configuração. `event_api` e `organization_api` usam OAuth2 Resource Server e claim `roles`; `user_api` emite e valida JWT em filtro próprio. A role `SERVICE` é usada somente no endpoint interno de promoção.

Evidências principais: `EventService.sendingNewEventToProcessor` linhas 37-65; `EventProcessorService.createNewEvent` linhas 41-69; `NotificationEventService.createNewEvent` linhas 21-25; `JwtService.generateToken` linhas 42-57; `InternalServiceTokenProvider.generateServiceToken` linhas 29-40.

