# Informações não identificadas

```yaml
document_id: QAK-ANA-001
service: all
document_type: unknowns
related_endpoints: [EP-EVT-002, EP-ORG-005, EP-USR-001]
related_business_rules: []
source_files: [event_api/src/main/java, organization_api/src/main/java, user_api/src/main/java, processor_api/src/main/java, notification_api/src/main/java, docker-compose.homolog.yml, docker-compose.prod.yml]
last_analyzed: 2026-11-09
confidence: high
```

1. `UNK-001`: comportamento desejado de PUBLIC sem organizationId/unitId; código possui regras contraditórias.
2. `UNK-002`: se a desativação deve revogar JWT já emitido; o login novo é bloqueado, mas o filtro aceita token anterior até expirar.
3. `UNK-003`: política desejada de ownership para perfil, senha, favoritos e reservas.
4. `UNK-004`: limites de ADMIN à própria organização/unidade.
5. `UNK-005`: regra de unicidade para nome de organização/unidade.
6. `UNK-006`: fluxos de excluir organização/unidade ou rotacionar join code; não existem.
7. `UNK-007`: política operacional de ack/redelivery e destino de mensagens rejeitadas.
8. `UNK-008`: timeouts de RestClient e SLA entre serviços.
9. `UNK-009`: como Android assina/desassina tópicos FCM e reconcilia `action/body`.
10. `UNK-010`: recursos reais apontados por S3/bancos/RabbitMQ em homologação, sem expor credenciais.
11. `UNK-011`: infraestrutura real de produção, observabilidade, backup e restore.
12. `UNK-012`: rotação/revogação do JWT compartilhado e de tokens emitidos.
13. `UNK-013`: sincronização desejada entre categorias Mongo e categorias criadas sob demanda no PostgreSQL.
14. `UNK-014`: timezone contratual das datas `LocalDateTime`.
15. `UNK-015`: API de consulta pública de eventos; não foi encontrada nos cinco serviços.
16. `UNK-016`: regra de estoque/capacidade e reserva de item inativo/quantity=0.
17. `UNK-017`: hierarquia permitida de roles (por exemplo ADMIN atribuir SUPER_ADMIN).
18. `UNK-018`: limites máximos e política de fragmentação de payload full sync/FCM.

Esses itens não devem ser completados por inferência do agente. Use `NEEDS_CLARIFICATION` ou `NEEDS_ENVIRONMENT` conforme o caso.

