# 🔔 Notification API - Business Rules

## Purpose

The **notification-api** is responsible for distributing event data to client applications.

It integrates with Firebase to:

* Persist event snapshots in Firestore
* Deliver real-time updates via Firebase Cloud Messaging (FCM)
* Ensure data consistency across devices through scoped synchronization

---

## Responsibilities

* Receive processed events from upstream services
* Persist events in Firestore
* Resolve target topic based on event scope
* Send notifications via FCM
* Handle error notifications
* Perform full synchronization (GET_ALL)

---

## Input Sources

### RabbitMQ Messages

The notification-api receives processed events from:

* `processor-api`

### Supported Actions

* CREATE
* UPDATE
* GET_ALL
* ERROR_CREATE
* ERROR_UPDATE

---

## Core Business Rules

## 1. Firestore Persistence

The notification-api stores event snapshots in Firestore.

### Rules

* Only valid events (`EventRequestDto`) must be persisted
* Error events (`EventErrorDto`) must NOT be persisted
* Persistence must use `transactionId` as the document identifier
* Writes must use merge strategy to avoid overwriting unintended fields

---

## 2. Topic Resolution

Each event must be delivered to a topic based on its scope.

### Rules

| Scope        | Topic                       |
| ------------ | --------------------------- |
| PUBLIC       | `public`                    |
| ORGANIZATION | `org_<organizationId>`      |
| UNIT         | `unit_<organizationUnitId>` |

### Constraints

* `organizationId` is required for ORGANIZATION scope
* `organizationUnitId` is required for UNIT scope
* PUBLIC events must not contain organization identifiers

---

## 3. Notification Delivery

Notifications are sent via Firebase Cloud Messaging.

### Rules

* Each notification must contain:

    * `action`
    * serialized `payload`
* Payload must include scope and identifiers
* Delivery must be topic-based (not device-based)

---

## 4. Full Sync (GET_ALL)

Full synchronization ensures consistency between client and backend.

### Purpose

To correct inconsistencies caused by:

* missed notifications
* app reinstallation
* local data corruption

---

### Rule: Scope-Based Grouping

Events must be grouped before sending:

* Public events → `public`
* Organization events → grouped by `organizationId`
* Unit events → grouped by `organizationUnitId`

---

### Rule: One Payload per Scope

Each notification must contain only events of a single scope.

### Example

* `public` → all public events
* `org_1` → only events from organization 1
* `unit_10` → only events from unit 10

---

### Rule: No Cross-Scope Mixing

* Events from different scopes must never be sent in the same payload
* This guarantees correct client-side reconciliation

---

## 5. Client Delivery Model

A client subscribes to multiple topics.

### Example

User:

* organizationId = 1
* organizationUnitId = 10

Receives:

* `public`
* `org_1`
* `unit_10`

---

## 6. Client Synchronization Rule

Clients must reconcile data by scope.

### Expected behavior

* PUBLIC → replace public events
* ORGANIZATION → replace events of that organization
* UNIT → replace events of that unit

### Important

Clients must NOT:

* perform global delete-all
* mix scopes during reconciliation

---

## 7. Error Notification

Error events must be delivered but not persisted.

### Rules

* Use `EventErrorDto`
* Send to topic resolved by scope
* Include action:

    * ERROR_CREATE
    * ERROR_UPDATE

---

## Output

### Firebase Cloud Messaging (FCM)

Delivers notifications to client applications.

### Cloud Firestore

Stores event snapshots for:

* new device bootstrap
* external frontend consumption

---

## Invariants

The following conditions must always be true:

* Events must be delivered only to their correct scope
* Events from different organizations must never be mixed
* Firestore must reflect a consistent snapshot of events
* Error events must never be persisted as valid events
* Full sync must be scoped and isolated

---

## Consistency Strategy

The notification-api plays a key role in eventual consistency.

### Principles

* Firestore provides a synchronized snapshot layer
* FCM delivers real-time updates
* Full sync ensures recovery from inconsistencies
* Clients reconcile data locally per scope

---

## Example Flows

## Event Creation

1. notification-api receives event
2. Persists event in Firestore
3. Resolves topic based on scope
4. Sends CREATE notification to topic

---

## Event Update

1. notification-api receives event
2. Updates Firestore document
3. Resolves topic
4. Sends UPDATE notification

---

## Full Sync (GET_ALL)

1. notification-api receives full event list
2. Groups events by scope
3. Sends:

    * public → `public`
    * org_X → `org_X`
    * unit_Y → `unit_Y`
4. Clients reconcile locally

---

## Relationship with Other Services

### Upstream

* `processor-api`

### External

* Firebase Cloud Messaging
* Cloud Firestore

---

## Notes

* notification-api does not validate business data deeply
* notification-api does not own the main database
* notification-api focuses on distribution and synchronization
* Topic-based delivery is used for scalability, not security

---
