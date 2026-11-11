# Tratamento de erros

```yaml
document_id: QAK-API-003
service: all
document_type: error_handling
related_endpoints: [EP-EVT-002, EP-ORG-005, EP-USR-006]
related_business_rules: []
source_files: [event_api/src/main/java/com/br/capoeira/eventos/event_api/config/exception/GlobalHandlerException.java, organization_api/src/main/java/com/br/capoeira/eventos/organization_api/config/exception/GlobalHandlerException.java, user_api/src/main/java/com/br/capoeira/eventos/user_api/config/exception/GlobalHandlerException.java]
last_analyzed: 2026-11-09
confidence: high
```

Formato pretendido nos três REST services: `timestamp`, `status`, `error`, `message`, `path`. Contudo, `event_api.StandardException` declara campos na ordem `timestamp,status,message,path,error`, enquanto o handler instancia como se fosse `timestamp,status,error,message,path`. Portanto o JSON efetivo do event_api troca semanticamente `path` e `error`; isto deve ser testado como comportamento atual e registrado como bug, não normalizado pelo QA.

| Serviço | Exceção | HTTP implementado |
|---|---|---|
| event_api | ValidationException | 404 se mensagem contém `not found` case-insensitive; senão 400 |
| event_api | FileException | 404 por texto; senão 400 |
| event_api | ServiceUnavailableException | 503 |
| event_api | AuthorizationDeniedException | 403 vazio |
| event_api | MethodArgumentNotValid/MissingPart | 400 |
| event_api | Exception | 500 |
| organization_api | ValidationException | 404 somente se mensagem contém `not found` com case sensível; senão 400 |
| organization_api | MethodArgumentNotValid | 400 |
| user_api | ValidationException/FileException | 404 por texto case sensível; senão 400 |
| user_api | MethodArgumentNotValid/MissingPart | 400 |
| user_api | DataIntegrityViolationException | HTTP 409, mas campo `status` do corpo é 400 |

Exceções não cobertas explicitamente: `UsernameNotFoundException`, `EntityNotFoundException`, `ServiceUnavailableException` do organization_api e falhas de autenticação. Seus status/formatos devem ser marcados `NEEDS_CLARIFICATION` até teste controlado ou revisão do framework/configuração.

Nos workers, exceções dos listeners são relançadas. As propriedades configuram `spring.rabbitmq.listener.simple.default-requeue-rejected=false`; não há recoverer/DLQ declarado. A consequência provável é reject sem requeue, mas o comportamento final depende da configuração do container/broker e deve ser confirmado em teste de mensageria isolado.

