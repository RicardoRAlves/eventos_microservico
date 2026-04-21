# 🔄 Full Sync Flow - Data Reconciliation

## Purpose

The **Full Sync Flow (GET_ALL)** is designed to ensure data consistency between backend systems and client applications.

It is used to recover from inconsistencies caused by:

* missed push notifications
* application reinstallation
* offline scenarios
* local data corruption

---

## Problem Statement

Due to the asynchronous nature of the system:

* Clients may miss updates
* Local databases may become outdated
* Events may exist locally but not in the backend anymore

The system must provide a reliable way to **reconcile the client state with the backend source of truth**.

---

## Solution Overview

The system uses a **scope-based full synchronization strategy**.

Instead of sending all events globally, the sync is segmented by **event scope**:

* PUBLIC
* ORGANIZATION
* UNIT

This ensures:

* data isolation between tenants
* efficient updates
* correct client-side reconciliation

---

## Key Concept: Scoped Synchronization

Each sync payload contains events from a **single scope only**.

### Scope Types

| Scope        | Description                                 |
| ------------ | ------------------------------------------- |
| PUBLIC       | Global events                               |
| ORGANIZATION | Events belonging to a specific organization |
| UNIT         | Events belonging to a specific unit         |

---

## Flow Overview

### Step-by-step

1. notification-api retrieves the full event list (source of truth)
2. Events are grouped by `scope`
3. Events are further grouped by:

    * `organizationId` (for ORGANIZATION scope)
    * `organizationUnitId` (for UNIT scope)
4. Separate payloads are created per group
5. Each payload is sent to its corresponding FCM topic

---

## Topic Mapping

| Scope        | Topic                       |
| ------------ | --------------------------- |
| PUBLIC       | `public`                    |
| ORGANIZATION | `org_<organizationId>`      |
| UNIT         | `unit_<organizationUnitId>` |

---

## Payload Structure

### EventSyncDto

```json
{
  "scope": "ORGANIZATION",
  "organizationId": 1,
  "organizationUnitId": null,
  "events": [ ... ]
}
```

### Rules

* Each payload must contain events of only one scope
* Identifiers must match the topic
* Empty payloads should not be sent

---

## Delivery Model

### Example

User:

* organizationId = 1
* organizationUnitId = 10

Subscribed topics:

* `public`
* `org_1`
* `unit_10`

### Receives:

* public events
* organization 1 events
* unit 10 events

### Does NOT receive:

* other organizations
* other units

---

## Client-Side Reconciliation

The client must process sync data **per scope**.

### Rules

* PUBLIC payload → replace only public events
* ORGANIZATION payload → replace events of that organization
* UNIT payload → replace events of that unit

---

## Important Constraint

❌ The client must NOT perform a global delete-all

### Why?

Because each payload represents only a subset of the data.

---

## Correct Client Strategy

### Pseudocode

```kotlin
when (payload.scope) {
    PUBLIC -> deleteAllPublic()
    ORGANIZATION -> deleteAllByOrganization(payload.organizationId)
    UNIT -> deleteAllByUnit(payload.organizationUnitId)
}

upsertAll(payload.events)
```

---

## Backend Guarantees

The backend ensures:

* Events are grouped correctly by scope
* No cross-scope mixing occurs
* Payloads are delivered to the correct topics
* Each client receives only relevant data

---

## Consistency Model

The system follows **eventual consistency**.

### Guarantees

* The backend holds the authoritative state
* Full sync restores client correctness
* Clients converge to the correct state over time

---

## Failure Scenarios Covered

The full sync flow handles:

* Missed FCM messages
* Device offline during updates
* App reinstall
* Local database corruption
* Partial update failures

---

## Example Flow

### Full Sync Execution

1. processor-api ensures database is consistent
2. notification-api retrieves all valid events
3. Events are grouped by scope
4. Payloads are created per topic
5. notification-api sends:

    * public → `public`
    * org_1 → `org_1`
    * unit_10 → `unit_10`
6. Client receives multiple payloads
7. Client reconciles data per scope

---

## Invariants

The following must always be true:

* Payloads contain only one scope
* Topics match payload scope
* No data leakage between organizations
* Clients never mix scopes during reconciliation
* Backend state is the source of truth

---

## Summary

The full sync mechanism:

* ensures data consistency across devices
* isolates tenants using scope-based delivery
* avoids global data replacement issues
* complements real-time updates with reconciliation

This approach enables scalable and reliable synchronization in a distributed system.

---
