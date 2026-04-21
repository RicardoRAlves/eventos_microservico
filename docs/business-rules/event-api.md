# 📍 Event API - Business Rules

## Purpose

The **event-api** is responsible for handling incoming requests related to event creation and updates.

It acts as the entry point of the system for event management and ensures that only valid data is propagated to downstream services.

---

## Responsibilities

* Receive event creation and update requests via REST
* Validate input data and business constraints
* Generate a unique `transactionId`
* Persist initial event data
* Publish event messages to the message broker (RabbitMQ)

---

## Input Sources

### REST Endpoints

Typical operations:

* Create event
* Update event

### Payload

The API receives an `EventCreateRequestDto` or equivalent input DTO.

---

## Core Business Rules

### 1. Event Validation

An event must be validated before processing.

#### Rules:

* Title must not be null or empty
* Description must not be null or empty
* Start and end dates must be valid
* Category must exist and be active
* Scope must be defined

---

### 2. Scope Validation

The `scope` defines the visibility of the event.

#### Rules:

* `PUBLIC`

    * Must NOT have `organizationId`
    * Must NOT have `organizationUnitId`

* `ORGANIZATION`

    * Must have `organizationId`
    * Must NOT require `organizationUnitId`

* `UNIT`

    * Must have `organizationUnitId`
    * Should be associated with a valid `organizationId`

---

### 3. Category Validation

The event must be associated with a valid category.

#### Rules:

* Category must exist in the system
* Category must be active
* Category name comparison should be case-insensitive (recommended)

---

### 4. Transaction ID Generation

Each event must have a unique `transactionId`.

#### Rule:

* The `transactionId` must be generated before sending the event to downstream services

#### Example:

```text
<timestamp>-<uuid>
```

#### Purpose:

* Traceability across microservices
* Correlation of logs and events
* Error tracking

---

### 5. Persistence Rule

The event must be persisted before publishing.

#### Rule:

* If persistence fails, the event must NOT be published
* If publishing fails after persistence, the error must be logged and handled

---

### 6. Event Publication

After validation and persistence, the event must be published to RabbitMQ.

#### Rules:

* The published payload must contain all required event fields
* The `transactionId` must be included
* The `scope` must be preserved

---

## Output

### Message Broker (RabbitMQ)

The API publishes event messages for further processing.

#### Consumers:

* `processor-api`

---

## Error Handling

### Expected Scenarios

* Invalid category
* Invalid scope configuration
* Missing required fields
* Persistence failure
* Message publication failure

### Rules

* Errors must be logged with `transactionId`
* The API must return a meaningful error response to the client
* No invalid event should be propagated to other services

---

## Invariants

The following conditions must always be true:

* Every event has a unique `transactionId`
* Scope rules are respected
* Category is valid and active
* Invalid events are never published
* The system must remain consistent even in case of partial failures

---

## Consistency Strategy

The event-api is the first step in an eventually consistent system.

### Principles:

* Valid data is accepted and propagated asynchronously
* Downstream services are responsible for further processing
* The system tolerates temporary inconsistencies

---

## Example Flow

### Event Creation

1. Client sends request to event-api
2. event-api validates payload
3. event-api validates category
4. event-api validates scope
5. event-api generates `transactionId`
6. event-api persists the event
7. event-api publishes message to RabbitMQ

---

## Notes

* The event-api does not handle notifications
* The event-api does not handle client delivery logic
* It is strictly responsible for validation, persistence, and event publishing

---
