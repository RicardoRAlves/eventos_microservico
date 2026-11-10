# Diagramas

```yaml
document_id: QAK-ARCH-004
service: all
document_type: diagrams
related_endpoints: [EP-EVT-002, EP-USR-021, EP-ORG-005]
related_business_rules: [BR-MSG-001, BR-RES-001, BR-ORG-001]
source_files: [event_api/src/main/java, processor_api/src/main/java, notification_api/src/main/java, user_api/src/main/java, organization_api/src/main/java]
last_analyzed: 2026-11-09
confidence: high
```

## Arquitetura confirmada

```mermaid
flowchart LR
  Client -->|REST/JWT| E[event_api]
  Client -->|REST/JWT| O[organization_api]
  Client -->|REST/JWT| U[user_api]
  E --> M[(MongoDB)]
  O --> PO[(PostgreSQL organization_api)]
  U --> PU[(PostgreSQL user_api)]
  E -->|REST validar unidade| O
  U -->|REST join code| O
  O -->|REST token SERVICE| U
  E -->|fanout JSON| R[RabbitMQ]
  U -->|reserva JSON| R
  R --> P[processor_api]
  P --> PP[(PostgreSQL eventos_api)]
  P -->|fanout JSON| R
  R --> N[notification_api]
  R --> U
  E --> S3[(S3/compatível)]
  O --> S3
  U --> S3
  N --> F[(Firestore)]
  N --> FCM[Firebase Cloud Messaging]
```

## Criação de evento

```mermaid
sequenceDiagram
  participant C as Cliente
  participant E as event_api
  participant M as MongoDB
  participant R as RabbitMQ
  participant P as processor_api
  participant DB as PostgreSQL
  participant N as notification_api
  participant F as Firestore/FCM
  C->>E: POST /api/v1/evento/create
  E->>E: valida DTO, datas, categoria e escopo
  E->>M: save(event)
  E->>R: events.v2.create-event
  E-->>C: 201 EventResponseDto
  R->>P: create-queue-event.save-database
  P->>DB: save(event/category)
  P->>R: create-notification-event
  R->>N: queue.create-notification-event
  N->>F: merge events_v2 + FCM topic
```

## Reserva

```mermaid
sequenceDiagram
  participant C as Cliente
  participant U as user_api
  participant R as RabbitMQ
  participant P as processor_api
  participant DB as PostgreSQL eventos_api
  participant UR as PostgreSQL user_api
  C->>U: POST /event-reservations
  U->>UR: verifica duplicidade já persistida
  U->>R: create-reservation-processor-sale
  U-->>C: 202
  R->>P: create-reservation-queue-processor-sale
  P->>DB: busca item por transactionId
  P->>R: create-reservation-sale
  R->>U: queue.create-reservation-event
  U->>UR: insert se ainda não existe
```

Fluxos não verificados: entrega efetiva em ambiente, assinatura Android nos tópicos, política de redelivery do broker além de `default-requeue-rejected=false`, e recuperação após falha entre persistência e publicação.

