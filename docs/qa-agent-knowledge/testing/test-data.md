# Massa de testes

```yaml
document_id: QAK-TST-003
service: all
document_type: test_data
related_endpoints: [EP-USR-006, EP-ORG-005, EP-EVT-002, EP-USR-022]
related_business_rules: [BR-USR-001, BR-ORG-001, BR-EVT-001, BR-RES-001]
source_files: [event_api/src/main/resources/seed/categories.json, user_api/src/main/java/com/br/capoeira/eventos/user_api/enums/Role.java]
last_analyzed: 2026-11-09
confidence: high
```

Conjunto mínimo sintético, nunca copiado de produção:

- usuários: `qa.client.N@invalid.test`, `qa.admin.N@invalid.test`, `qa.super.N@invalid.test`; senha aleatória por execução; avatar no S3 local;
- organização: nome `QA_ORG_{runId}`, unidade `QA_UNIT_{runId}`; capture o join code retornado, não o fixe;
- categoria: use seed `capoeira` ou crie `QA_CAT_{runId}`;
- evento: título/transactionId gerado pela API; datas UTC/local consistentes, fim uma hora após início;
- venda: descrição curta, quantity 2, value 10.00;
- favoritos/reservas: usar IDs retornados pelo processor, não IDs inventados.

Variações negativas: strings vazias/brancas, datas iguais/invertidas, enums inválidos, IDs 0/negativos, descrição 150/151, quantidade/valor -1/0, transactionId inexistente, join code inválido, duplicatas e JWT expirado/role errada.

Cleanup deve operar por banco/container descartável. Quando inevitável usar ambiente compartilhado de homologação, prefixar `QA_{runId}`, registrar todos IDs e executar apenas deletes suportados/seguros; como organização/categoria não têm delete físico, a preferência é não usar ambiente compartilhado.

