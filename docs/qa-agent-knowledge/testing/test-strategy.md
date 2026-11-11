# Estratégia de testes

```yaml
document_id: QAK-TST-002
service: all
document_type: test_strategy
related_endpoints: [EP-EVT-002, EP-ORG-005, EP-USR-001]
related_business_rules: [BR-MSG-001]
source_files: [event_api/src/test, organization_api/src/test, user_api/src/test, processor_api/src/test, notification_api/src/test]
last_analyzed: 2026-11-09
confidence: high
```

Ordem recomendada: unitários puros; controller/contrato com MockMvc; repositories com Testcontainers; mensageria com RabbitMQ isolado; integrações S3/Firebase não produtivas; E2E completo. Cada camada deve gerar evidência por ID de teste e limpar apenas recursos com prefixo/namespace de execução.

Gates:

1. JSON/contratos estáticos válidos.
2. Unitários P0/P1 READY.
3. Segurança e validação sem infraestrutura externa.
4. Banco/Rabbit em containers descartáveis.
5. S3 local RustFS.
6. Firebase somente quando `project_id` esperado for explicitamente de teste/homologação.
7. E2E Android apenas após mapear o cliente.

Não usar `ddl-auto=update` contra banco compartilhado. Para Testcontainers, banco novo por suite e cleanup por descarte do container. Para RabbitMQ, use vhost exclusivo por execução e aguarde por correlação/estado, nunca sleeps fixos longos. Para eventos eventualmente consistentes, registre timeout, polling e estado de cada hop.

O agente Ollama deve selecionar no máximo um pequeno lote de testes correlatos por prompt, usando IDs e recuperando apenas endpoint/regra/mensagem citados. `NEEDS_CLARIFICATION` não deve ser automaticamente convertido em assert de produto; pode executar teste de caracterização se o ambiente for descartável.

