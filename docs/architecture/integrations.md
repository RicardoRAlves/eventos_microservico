# 🔗 Event Manager - Integrations

## Purpose

This document describes how the Event Manager services communicate with each other and with external platforms.

It covers:

* Internal service-to-service integrations
* Messaging flows
* External dependencies
* Contract expectations
* Delivery and synchronization rules

---

## Integration Map

The system uses both synchronous and asynchronous communication.

### Internal communication patterns

* **REST** for validation and direct service calls
* **RabbitMQ** for asynchronous event-driven communication

### External platforms

* **Firebase Cloud Messaging (FCM)** for push notifications
* **Cloud Firestore** for event snapshots and client data access

---

## Internal Integrations

## 1. event-api → RabbitMQ

### Purpose

Publishes event-related messages after receiving and validating requests.

### Communication type

Asynchronous

### Trigger

* Event creation
* Event update
* Full synchronization requests, when applicable

### Responsibilities

* Validate incoming payload
* Generate `transactionId`
* Publish message to the broker

### Expected outcome

The message becomes available for downstream processing.

---

## 2. RabbitMQ → processor-api

### Purpose

Allows `processor-api` to consume event messages published by `event-api`.

### Communication type

Asynchronous

### Responsibilities

* Consume messages from the queue
* Apply business rules
* Persist data in the main database
* Forward processed events to the next integration stage

### Expected outcome

The event becomes part of the system source of truth.

---

## 3. processor-api → notification-api

### Purpose

Forwards processed and validated event data to `notification-api`.

### Communication type

Asynchronous

### Responsibilities

* Deliver processed event payloads
* Support create, update, error, and full sync flows

### Expected outcome

`notification-api` receives the payload required to notify clients and update Firebase layers.

---

## 4. user-api ↔ organization-api

### Purpose

Validates organizational relationships and user association rules.

### Communication type

Synchronous (REST)

### Common use cases

* Validate `joinCode`
* Resolve `organizationId`
* Resolve `organizationUnitId`
* Associate a user with an organization/unit

### Expected outcome

Only valid associations are accepted by the system.

---

## External Integrations

## 5. notification-api → Firebase Cloud Messaging (FCM)

### Purpose

Distributes notifications to client applications using topic-based delivery.

### Communication type

External / push

### Topic strategy

| Scope        | Topic format                |
| ------------ | --------------------------- |
| PUBLIC       | `public`                    |
| ORGANIZATION | `org_<organizationId>`      |
| UNIT         | `unit_<organizationUnitId>` |

### Delivery rule

A notification is sent to the topic resolved from the event scope.

### Examples

* Public event → `public`
* Organization event for organization 1 → `org_1`
* Unit event for unit 10 → `unit_10`

---

## 6. notification-api → Cloud Firestore

### Purpose

Stores event snapshots for client bootstrap and external consumption.

### Communication type

External / persistence

### Main use cases

* Provide initial data for newly installed devices
* Support front-end clients that read event data directly
* Maintain a synchronized event view outside the relational database

### Notes

Firestore does not replace the main database.
It acts as a synchronized read-oriented layer for clients and integrations.

---

## Message Contracts

## EventRequestDto

Represents a valid event payload used in create, update, and synchronization flows.

### Relevant fields

* `id`
* `transactionId`
* `title`
* `description`
* `dateStarted`
* `dateFinished`
* `locationName`
* `address`
* `typeContact`
* `contact`
* `image`
* `categoryName`
* `scope`
* `organizationId`
* `organizationUnitId`
* `active`

---

## EventErrorDto

Represents an error payload used when an event operation fails.

### Relevant fields

* `transactionId`
* `title`
* `description`
* `scope`
* `organizationId`
* `organizationUnitId`
* `active`

### Notes

Error payloads are used for client notification and should not be persisted as regular event snapshots.

---

## EventSyncDto

Represents a scoped full synchronization payload.

### Relevant fields

* `scope`
* `organizationId`
* `organizationUnitId`
* `events`

### Purpose

Used during `GET_ALL` flows to reconcile client state by scope.

---

## Delivery Rules

A client must only receive the events it is allowed to see.

### Visibility model

A user can access:

* Public events
* Events from the user’s organization
* Events from the user’s organization unit

### Example

A user associated with:

* `organizationId = 1`
* `organizationUnitId = 10`

must receive events from:

* `public`
* `org_1`
* `unit_10`

and must not receive events from:

* other organizations
* other units

---

## Full Sync Strategy

Full synchronization is scope-based.

### Rule

`GET_ALL` messages must be grouped and delivered according to event scope.

### Example

* Public events → topic `public`
* Organization 1 events → topic `org_1`
* Unit 10 events → topic `unit_10`

### Client expectation

Clients reconcile data by scope instead of treating all incoming sync payloads as global replacements.

---

## Error Handling

Integration failures must be explicit and observable.

### Expected failure scenarios

* Invalid organization or unit association
* Invalid join code
* Firestore persistence error
* FCM delivery failure
* Message broker publication or consumption failure

### Recommended handling

* Log the error with `transactionId`
* Preserve traceability between services
* Notify clients when business flow requires error feedback
* Avoid silently swallowing integration failures

---

## Consistency Strategy

The system follows an eventual consistency model.

### Main principles

* The relational database is the primary source of truth
* RabbitMQ propagates state changes asynchronously
* Firestore stores synchronized snapshots for client consumption
* FCM distributes update signals and sync payloads
* Clients may require reconciliation through scoped full sync

---

## Security Considerations

* Topic subscription is a distribution mechanism, not an authorization layer
* Authorization must be enforced by backend services
* Clients must not infer access rights only from topic names
* Organizational isolation must be guaranteed before notification delivery

---

## Summary

The integration model is based on:

* asynchronous event propagation through RabbitMQ
* scoped delivery through Firebase topics
* Firestore as a client-oriented synchronized store
* backend-enforced business boundaries between organizations and units

This approach allows the platform to scale while keeping event distribution segmented and consistent.

---
