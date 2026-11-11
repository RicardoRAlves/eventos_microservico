# Riscos para QA e operação

```yaml
document_id: QAK-ANA-003
service: all
document_type: risks
related_endpoints: [EP-EVT-002, EP-ORG-005, EP-USR-008, EP-USR-022]
related_business_rules: [BR-MSG-001]
source_files: [docker-compose.yml, docker-compose.homolog.yml, docker-compose.prod.yml, event_api/src/main/java, organization_api/src/main/java, user_api/src/main/java, processor_api/src/main/java, notification_api/src/main/java]
last_analyzed: 2026-11-09
confidence: high
```

| ID | Severidade | Risco | Mitigação de teste |
|---|---|---|---|
| RSK-001 | crítica | arquivos de credencial/chave existem no workspace/repositório | não copiar; rotacionar fora desta missão; secret scan posterior |
| RSK-002 | crítica | homolog pode apontar S3/DB/Rabbit externos por env_file | validar destinos e usar recursos descartáveis antes de iniciar |
| RSK-003 | crítica | autorização horizontal ausente em perfil/senha/favoritos/reservas | teste de caracterização isolado; decisão de produto |
| RSK-004 | alta | ADMIN sem limitação organizacional no service | matriz multi-tenant antes de produção |
| RSK-005 | alta | persistência antes da publicação sem outbox | fault injection e reconciliação explícita |
| RSK-006 | alta | sem retry/DLQ; mensagens podem ser perdidas | Rabbit isolado e teste de rejeição |
| RSK-007 | alta | promoção remota não compensada por rollback local | teste de falha distribuída/harness |
| RSK-008 | alta | `ddl-auto=update` pode alterar schema | nunca iniciar contra DB compartilhado |
| RSK-009 | alta | reserva ignora active/estoque/ID informado | testes negativos e esclarecimento de regra |
| RSK-010 | alta | JWT compartilhado e SERVICE baseado em claim | proteger/rotacionar segredo e testar forgery boundaries |
| RSK-011 | média | FCM pode duplicar e write/push não é atômico | consumidores Android idempotentes |
| RSK-012 | média | full sync deixa stale documents | teste de reconciliação e decisão de limpeza |
| RSK-013 | média | BigDecimal vira Double no Firestore | teste de precisão e tolerância definida |
| RSK-014 | média | imagens órfãs/uploads públicos | bucket local, quota e cleanup |
| RSK-015 | média | `mongo:latest` e versões framework divergentes | pin de imagem/compatibilidade em trabalho futuro |
| RSK-016 | média | payload full sync pode exceder limites FCM | massa volumosa controlada e estratégia de chunking futura |
| RSK-017 | média | logs imprimem DTOs e principal/authorities | usar somente dados sintéticos; revisar logging posteriormente |
| RSK-018 | baixa | CORS só localhost pode bloquear clientes web de outros ambientes | teste por ambiente quando origem oficial for conhecida |

