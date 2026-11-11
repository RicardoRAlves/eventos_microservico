# processor_api

```yaml
document_id: QAK-SVC-PRC-001
service: processor_api
document_type: service
related_endpoints: []
related_business_rules: [BR-MSG-001, BR-PRC-001, BR-RES-002]
source_files: [processor_api/src/main/java, processor_api/src/main/resources/application.properties]
last_analyzed: 2026-11-09
confidence: high
```

Worker sem controllers REST. Mantém a projeção relacional de eventos, categorias e itens de venda no banco `eventos_api` e encadeia mensagens para notification_api e user_api.

Consome oito filas: create/get-all/update/delete de evento, create/update/delete de venda e pedido de reserva. Produz dez tipos de envio. Usa Jackson JSON e exchanges fanout com routing key vazia.

Criação de evento não testa explicitamente duplicidade antes do insert; `transactionId` é único no banco. Em qualquer falha publica erro e relança, enquanto `default-requeue-rejected=false` sugere descarte sem requeue; não há DLQ configurada. Categorias ausentes são criadas após trim/normalização de espaços, com recuperação de corrida por unique constraint.

Criação de venda é idempotente por `transactionId`; update exige item existente; delete é lógico e republica mesmo se já inativo. Reserva somente comprova que o transactionId existe, mas não compara o `eventSaleId` enviado com o ID encontrado, não checa `active`, estoque ou decremento.

Sem HTTP público; a porta existe apenas para runtime/actuator, embora não haja SecurityConfig.

