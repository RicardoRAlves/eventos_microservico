# notification_api

```yaml
document_id: QAK-SVC-NTF-001
service: notification_api
document_type: service
related_endpoints: []
related_business_rules: [BR-NTF-001, BR-NTF-005]
source_files: [notification_api/src/main/java, notification_api/src/main/resources/application.properties]
last_analyzed: 2026-11-09
confidence: high
```

Worker sem controllers REST. Consome nove filas e projeta eventos em `events_v2`, itens de venda em `event_sale_items_v1`, além de publicar mensagens FCM.

Eventos create/update usam `SetOptions.merge()` por documento `transactionId`; delete marca `active=false`. Itens de venda create/update também usam merge; delete remove fisicamente o documento. Full sync grava todos em batch, mas não remove documentos ausentes e depois emite mensagens agrupadas por escopo.

Tópicos FCM: `public`, `org_{organizationId}`, `unit_{organizationUnitId}`; delete usa tópico global `delete_event`. Payload data contém `action` e `body` JSON. Ações confirmadas: `CREATE`, `UPDATE`, `DELETED`, `ERROR_CREATE`, `ERROR_UPDATE`, `GET_ALL`.

Credencial Firebase vem de `FIREBASE_CONFIG`; fallback classpath é desabilitado por padrão e depende também de profile Maven. O resolver valida `project_id` contra `firebase.expected-project-id`. O Compose diferencia projeto esperado de homologação e produção.

Operações aguardam `Future.get()` e relançam RuntimeException. Não há retry/DLQ/idempotency key explícitos; a chave de documento torna create/update repetidos convergentes, mas notificações FCM podem duplicar.

