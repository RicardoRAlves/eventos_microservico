# RabbitMQ e fluxos assíncronos

```yaml
document_id: QAK-MSG-002
service: event_api,user_api,processor_api,notification_api
document_type: messaging
related_endpoints: [EP-EVT-001, EP-EVT-002, EP-EVT-005, EP-USR-022]
related_business_rules: [BR-MSG-001, BR-RES-001]
source_files: [event_api/src/main/java/com/br/capoeira/eventos/event_api/config/RabbitMqConfig.java, user_api/src/main/java/com/br/capoeira/eventos/user_api/config/RabbitMqConfig.java, processor_api/src/main/java/com/br/capoeira/eventos/processor_api/config/RabbitMqConfig.java, notification_api/src/main/java/com/br/capoeira/eventos/notification/config/RabbitMqConfig.java]
last_analyzed: 2026-11-09
confidence: high
```

Foram encontradas 6 classes producer (2 event, 1 user, 3 processor) e 8 classes consumer (1 event, 2 user, 3 processor, 2 notification). Contagem operacional: 19 chamadas `convertAndSend`, 19 contratos de publicação e 21 métodos `@RabbitListener`; dois publishes fanout possuem dois consumers em filas distintas.

Todas as exchanges declaradas são fanout e todos os producers usam routing key vazia. Filas criadas com `new Queue(name)` são duráveis por default do Spring AMQP. O conversor é Jackson JSON em todos os serviços.

Ack/retry/falhas:

- ack mode não é configurado; aplica-se o default do listener container;
- `default-requeue-rejected=false` nos quatro serviços com listeners;
- não há `RetryInterceptor`, `RepublishMessageRecoverer`, DLX, DLQ, TTL, parking lot ou publisher confirms no código/configuração analisados;
- consumers geralmente deixam exceções subirem; portanto mensagens inválidas podem ser perdidas/rejeitadas sem trilha de DLQ;
- nenhum envelope contém `messageId`, versão, timestamp ou correlationId.

Idempotência parcial: vendas create e reservas persistidas têm checks/constraints; writes Firestore por transactionId convergem; notificações FCM e algumas republicações de delete podem duplicar. Evento create no processor não possui guard explícito antes da unique constraint.

Há compatibilidade estrutural, não compartilhamento de biblioteca: o mesmo JSON é deserializado em DTOs duplicados de pacotes diferentes. Mudança unilateral de enum/campo pode quebrar consumers em runtime.

