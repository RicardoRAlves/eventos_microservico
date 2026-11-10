# Relatório de cobertura

```yaml
document_id: QAK-ANA-004
service: all
document_type: coverage
related_endpoints: [EP-EVT-001, EP-ORG-001, EP-USR-001]
related_business_rules: [BR-EVT-001, BR-ORG-001, BR-USR-001, BR-MSG-001]
source_files: [event_api/src/main/java, organization_api/src/main/java, user_api/src/main/java, processor_api/src/main/java, notification_api/src/main/java, docs/qa-agent-knowledge/api/endpoints.json, docs/qa-agent-knowledge/business-rules/rules.json, docs/qa-agent-knowledge/testing/test-catalog.json]
last_analyzed: 2026-11-09
confidence: high
```

## Totais calculados

| Métrica | Total | Critério |
|---|---:|---|
| Microsserviços analisados | 5 | módulos solicitados |
| Controllers REST | 9 | classes com `@RestController` exato, excluindo 3 advices |
| Endpoints mapeados | 47 | métodos Get/Post/Put/Patch/DeleteMapping |
| Artefatos no pacote DTO analisados | 79 | 69 classes, 3 records, 4 enums e 1 interface, somando por serviço |
| Modelos persistidos catalogados | 14 | entidades/documentos/coleções, incluindo lock e 2 documentos Firestore |
| Regras de negócio | 43 | entradas únicas em `rules.json` |
| Classes consumer | 8 | classes com `@RabbitListener` |
| Métodos consumer | 21 | ocorrências `@RabbitListener` |
| Classes producer | 6 | classes de produção com `convertAndSend` |
| Publicações/contratos | 19 | chamadas `convertAndSend` e entradas em message-contracts |
| Testes propostos | 41 | entradas únicas em `test-catalog.json` |
| Testes READY | 15 | unitários/mocados sem infraestrutura desconhecida |
| Lacunas explícitas | 18 | itens `UNK-001..018` |
| Inconsistências | 18 | itens `INC-001..018` |
| Riscos | 18 | itens `RSK-001..018` |

Distribuição de endpoints: event_api 15, organization_api 9, user_api 23, processor_api 0, notification_api 0. Distribuição DTO: event 17, organization 13, user 25, processor 11, notification 13.

Readiness dos testes: READY 15; NEEDS_DATA 3; NEEDS_ENVIRONMENT 18; NEEDS_CLARIFICATION 4; BLOCKED 1.

## Cobertura qualitativa

Coberto por fonte estática: controllers, services, DTOs, entidades, repositories, mappers relevantes, segurança, advices, REST clients, producers/consumers, configs Rabbit/S3/Firebase, properties, Compose, seed, POMs e documentação legada. Testes existentes foram usados como evidência secundária, não como prova do runtime.

Não coberto dinamicamente: status padrão do framework para exceções não tratadas, topologia/estado real do broker, schemas reais gerados por Hibernate, dados reais, credenciais/destinos efetivos, latência/redelivery, limites FCM e comportamento Android. Por isso esta documentação não declara cobertura runtime completa.

## Método de validação

Contagens de código foram extraídas por busca de anotações/arquivos. Contagens de catálogos e unicidade de IDs foram obtidas após parse de JSON. Todos os JSON desta árvore foram parseados com sucesso em 2026-11-09.

