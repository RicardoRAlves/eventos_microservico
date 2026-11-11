# Integração Firebase

```yaml
document_id: QAK-INT-FB-001
service: notification_api
document_type: integration
related_endpoints: []
related_business_rules: [BR-NTF-001, BR-NTF-005]
source_files: [notification_api/src/main/java/com/br/capoeira/eventos/notification/config/FirebaseConfig.java, notification_api/src/main/java/com/br/capoeira/eventos/notification/config/FirebaseCredentialResolver.java, notification_api/src/main/java/com/br/capoeira/eventos/notification/service/FirebaseEventService.java, notification_api/src/main/java/com/br/capoeira/eventos/notification/service/FirebaseEventSaleItemService.java]
last_analyzed: 2026-11-09
confidence: high
```

O Firebase Admin SDK inicializa um app nomeado `notification-api`. Credencial prioritária: JSON em `FIREBASE_CONFIG`; fallback `firebase_config.json` requer propriedade e profile Maven explícitos. O `project_id` da credencial é validado contra `FIREBASE_EXPECTED_PROJECT_ID` quando configurado. A inicialização recusa reutilizar app já existente.

Firestore:

- `events_v2/{transactionId}`: merge em create/update/full sync; delete muda `active=false`.
- `event_sale_items_v1/{transactionId}`: merge em create/update; delete físico.

FCM usa mensagens data-only: `action=<enum>` e `body=<JSON>`. Tópicos por escopo: `public`, `org_ID`, `unit_ID`; delete usa `delete_event`. Não há sanitização/validação explícita de tamanho do payload, retry, collapse key, TTL ou condição de plataforma.

Falha de Firestore impede o push subsequente; falha de FCM ocorre depois do write e deixa projeção atualizada sem notificação. Reentrega pode repetir FCM. Full sync não elimina documentos não presentes na lista.

Não há Firebase Emulator no Compose. Testes contra um projeto real ficam `NEEDS_ENVIRONMENT` e devem validar o project ID antes de execução; nunca usar projeto de produção.

