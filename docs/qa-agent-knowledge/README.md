# Base de conhecimento do QA Agent

```yaml
document_id: QAK-README-001
service: all
document_type: index
related_endpoints: [EP-EVT-001, EP-ORG-001, EP-USR-001]
related_business_rules: [BR-EVT-001, BR-ORG-001, BR-USR-001, BR-MSG-001]
source_files: [event_api/src/main/java, organization_api/src/main/java, user_api/src/main/java, processor_api/src/main/java, notification_api/src/main/java, docker-compose.yml]
last_analyzed: 2026-11-09
confidence: high
```

Base modular e rastreável para um agente local Ollama/RAG. O levantamento é estático: nenhum serviço, banco, migração ou teste funcional foi executado. Valores secretos não foram copiados.

## Como consultar

- Comece por `architecture/system-overview.md`.
- Para uma rota, procure o `endpoint_id` em `api/endpoints.json` e siga `business_rule_ids` e `source`.
- Para regras, consulte `business-rules/rules.json`.
- Para mensageria, combine `messaging/message-contracts.json` com `messaging/rabbitmq.md`.
- Para automação, filtre `testing/test-catalog.json` por `readiness` e `priority`.
- Lacunas, contradições e riscos ficam em `analysis/` e não devem ser convertidos em suposições pelo agente.

## Escopo e convenções

Foram analisados os cinco serviços solicitados, configurações, Docker, documentação legada e o seed `event_api/src/main/resources/seed/categories.json`. `confidence=high` significa evidência direta no código; `medium`, inferência forte de múltiplas configurações; `low`, hipótese explicitamente não confirmada. URLs dos endpoints são caminhos do serviço; host/porta variam por ambiente.

Os catálogos são JSON válido, UTF-8, com IDs estáveis. A cobertura quantitativa e o critério de contagem estão em `analysis/coverage-report.md`.

## Limites

- Não houve consulta/exportação de dados reais.
- Não houve verificação do Android.
- Não houve validação dinâmica de códigos padrão do Spring Security (401/403), a menos que um handler/controller os implemente explicitamente.
- A documentação preexistente foi usada apenas como fonte secundária e comparada com o código.

