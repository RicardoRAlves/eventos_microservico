# ⚙️ Processor API - Business Rules

## Purpose

The **processor-api** is responsible for consuming event messages, applying processing rules, and persisting the final event state in the main database.

It acts as the system component that transforms validated incoming messages into the authoritative persisted state of the platform.

---

## Responsibilities

* Consume event messages from RabbitMQ
* Apply processing and persistence rules
* Persist events in the main database
* Update existing events when required
* Prepare processed data for downstream distribution
* Forward processed events to `notification-api`

---

## Input Sources

### RabbitMQ Messages

The processor-api receives event payloads asynchronously from upstream services.

### Typical input actions

* Create event
* Update event
* Full synchronization preparation, when applicable

---

## Core Business Rules

### 1. Message Consumption

The processor-api must consume only valid event messages published by upstream services.

#### Rules

* The payload must contain a valid `transactionId`
* The payload must contain a defined `scope`
* The payload must contain the minimum required event data for persistence

---

### 2. Main Persistence Rule

The processor-api is responsible for saving the final event state in the primary database.

#### Rules

* A create flow must persist a new event record
* An update flow must modify the existing persisted event
* The persisted record must reflect the latest valid state received by the service

---

### 3. Source of Truth Rule

The relational database managed by processor-api is the primary source of truth for event data.

#### Implications

* Firebase layers do not replace the main database
* Notifications do not define authoritative state
* Full sync operations must be based on the database state managed by processor-api

---

### 4. Transaction Traceability

Every processed event must preserve its `transactionId`.

#### Purpose

* Correlate logs across services
* Track event propagation between microservices
* Support debugging and failure investigation

---

### 5. Update Processing Rule

An event update must preserve consistency with the existing persisted entity.

#### Rules

* The target event must be identifiable
* Only valid changes should be persisted
* Invalid updates must not corrupt existing data

---

### 6. Downstream Propagation

After a successful persistence operation, the event must be forwarded to downstream consumers.

#### Rules

* Only successfully processed events should be propagated
* The downstream payload must preserve scope and audience-related fields
* Propagation must support create, update, error, and sync-related flows

---

## Output

### Main Database

The processor-api persists the final event state in the primary storage layer.

### RabbitMQ / Downstream Messaging

After successful persistence, processed events are made available to downstream consumers.

#### Consumers

* `notification-api`

---

## Error Handling

### Expected Scenarios

* Invalid or incomplete message payload
* Persistence failure
* Update target not found
* Message forwarding failure

### Rules

* Errors must be logged with `transactionId`
* The service must preserve traceability of failed operations
* Invalid or partially processed data must not be propagated as successful state

---

## Invariants

The following conditions must always be true:

* The main database is the authoritative source of event data
* Every processed event must preserve its `transactionId`
* Only successfully persisted events can be forwarded as successful operations
* Event scope must remain unchanged during processing unless explicitly required by business rules
* Invalid processing must never overwrite valid persisted state

---

## Consistency Strategy

The processor-api is a core component of the platform’s eventual consistency model.

### Principles

* Incoming messages are processed asynchronously
* Persistence happens before downstream notification
* Other services depend on the state produced by processor-api
* Full sync operations rely on the persisted database state

---

## Example Flow

### Event Creation Processing

1. processor-api consumes a create message from RabbitMQ
2. processor-api validates the incoming payload for persistence
3. processor-api saves the event in the main database
4. processor-api forwards the processed event to notification-api

---

### Event Update Processing

1. processor-api consumes an update message from RabbitMQ
2. processor-api identifies the target persisted event
3. processor-api updates the database record
4. processor-api forwards the updated event to notification-api

---

## Relationship with Other Services

### Upstream

* `event-api`

### Downstream

* `notification-api`

### Notes

* `event-api` is responsible for request entry and initial validation
* `processor-api` is responsible for authoritative persistence
* `notification-api` is responsible for distribution and Firebase integration

---

## Notes

* The processor-api does not deliver notifications directly to clients
* The processor-api does not own Firebase topic resolution
* The processor-api exists to guarantee that downstream services receive processed data based on the main persisted state

---
