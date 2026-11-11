# Fluxos de negócio

```yaml
document_id: QAK-BR-002
service: all
document_type: business_flows
related_endpoints: [EP-EVT-002, EP-ORG-005, EP-USR-009, EP-USR-022]
related_business_rules: [BR-EVT-001, BR-ORG-001, BR-USR-006, BR-RES-001]
source_files: [event_api/src/main/java, organization_api/src/main/java, user_api/src/main/java, processor_api/src/main/java, notification_api/src/main/java]
last_analyzed: 2026-11-09
confidence: high
```

## Cadastro e autenticação

1. Cliente pode fazer upload público e recebe URL.
2. POST users cria CLIENT ativo com senha codificada.
3. Login autentica e emite JWT.
4. Recursos protegidos usam o bearer. Alteração de role não revoga tokens já emitidos.

## Organização

1. Usuário autenticado cria organização com unidade principal.
2. organization_api grava localmente, gera join code e chama user_api com token SERVICE.
3. user_api promove o usuário informado e grava os IDs.
4. Outros usuários consultam join code por meio do user_api e são associados sem mudança automática de role.

Ponto de falha: a chamada remota não participa da transação PostgreSQL do organization_api. Estados parciais são possíveis.

## Evento e notificação

1. ADMIN/SUPER_ADMIN cria o evento; event_api valida categoria/data/escopo.
2. Mongo recebe o comando e RabbitMQ o encaminha.
3. processor_api cria categoria se necessário, grava a projeção e publica sucesso.
4. notification_api faz merge no Firestore e envia FCM.
5. Em falha de create no processor, a mensagem volta ao event_api, que inativa o comando e publica notificação de erro.

O HTTP 201 confirma apenas a etapa local; não confirma PostgreSQL, Firestore ou FCM.

## Item de venda e reserva

1. Qualquer JWT pode criar item; event_api salva e publica.
2. processor_api liga o item ao evento pelo `eventTransactionId`.
3. Cliente solicita reserva com IDs e transactionId; user_api retorna 202.
4. processor_api ignora o ID informado para busca e usa o transactionId; cria payload com quantidade 1.
5. user_api persiste se `(user,eventSale)` ainda não existe.

Não existe controle de estoque: quantidade não é reduzida, item inativo pode ser reservado e reservas concorrentes antes da confirmação podem publicar duplicatas (a constraint final reduz duplicação de persistência).

## Exclusões

Evento e item são inativados nas projeções principais. O delete de evento no Firestore também é lógico; item de venda no Firestore é apagado fisicamente. Mensagens fanout de delete limpam favoritos e reservas no user_api.

