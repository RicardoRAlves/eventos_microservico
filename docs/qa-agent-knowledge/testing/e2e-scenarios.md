# Cenários E2E

```yaml
document_id: QAK-TST-004
service: all
document_type: e2e_scenarios
related_endpoints: [EP-USR-006, EP-ORG-005, EP-EVT-002, EP-EVT-013, EP-USR-022]
related_business_rules: [BR-USR-001, BR-ORG-001, BR-EVT-001, BR-RES-001, BR-NTF-001]
source_files: [event_api/src/main/java, organization_api/src/main/java, user_api/src/main/java, processor_api/src/main/java, notification_api/src/main/java]
last_analyzed: 2026-11-09
confidence: high
```

## E2E-01 — onboarding até evento

Cadastro CLIENT → login → criar organização → promoção SUPER_ADMIN → criar categoria/evento → observar Mongo, PostgreSQL, Firestore e FCM. Bloqueios: Firebase de teste e ambiente completo. IDs: TST-API-004, TST-API-006, TST-MSG-001.

## E2E-02 — ingresso por join code

Segundo usuário cadastra/login → recebe join code fora de banda → PATCH joincode → consulta perfil e acessa dados permitidos. O Android deverá futuramente validar armazenamento do JWT e inscrição em `org_ID`/`unit_ID`; esta parte não foi mapeada.

## E2E-03 — venda e reserva

Admin cria evento e item → aguarda projeção → CLIENT solicita reserva → aguarda persistência → consulta lista/contagens → cancela. Validar duplicidade e ausência de controle de estoque. IDs: TST-MSG-005/006.

## E2E-04 — exclusão e convergência

Criar evento, favorito, venda e reserva → deletar venda e evento → verificar inativação/remoção em todos os stores e push Android. IDs: TST-MSG-007/008.

## E2E-05 — full sync móvel

SUPER_ADMIN solicita `/evento/all` → processor lista tudo → notification faz batch e agrupa tópicos → Android reconcilia. Parte API confirmada; interpretação Android de `action/body`, assinatura de tópicos e política para stale documents exigem mapeamento posterior.

Nenhum cenário está READY enquanto a stack isolada e o Firebase não produtivo não forem comprovados.

