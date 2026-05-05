# 🏢 Organization API - Business Rules

## Purpose

The **organization-api** is responsible for managing organizational structures within the system.

It defines and validates:

* Organizations
* Organization units
* Join codes used to associate users with organizations

This service acts as the authoritative source for organizational data.

---

## Responsibilities

* Manage organizations and organization units
* Generate and validate `joinCode`
* Provide organization and unit data to other services
* Ensure consistency of organizational relationships
* Support user association workflows

---

## Input Sources

### REST Endpoints

Typical operations:

* Create organization
* Update organization
* Retrieve organization details
* Create and manage organization units
* Validate `joinCode`

---

## Core Business Rules

## 1. Organization Management

An organization represents a logical group within the system.

### Rules

* An organization must have:

    * name
    * description
* The organization must have an active status

---

## 2. Organization Unit Management

An organization can contain multiple units.

### Rules

* A unit must belong to exactly one organization
* A unit must have:

    * name
    * location (city, state, country)
* Units must be identifiable by a unique ID
* Units must be active to be used in associations

---

## 3. Join Code Generation

Join codes are used to associate users with an organization unit.

### Rules

* A join code must be unique
* A join code must be linked to:

    * `organizationId`
    * `organizationUnitId`
* A join code may be time-limited (optional)
* A join code may be single-use or reusable (configurable)

---

## 4. Join Code Validation

Join codes must be validated before user association.

### Rules

* The join code must exist
* The join code must be active
* The join code must map to a valid organization and unit
* Invalid or expired join codes must be rejected

### Output

A valid join code returns:

* `organizationId`
* `organizationUnitId`

---

## 5. Organization Consistency

The relationship between organization and unit must always be consistent.

### Rules

* A unit must always reference a valid organization
* A join code must not point to inconsistent data
* Deleting or deactivating an organization must impact its units accordingly

---

## 6. Data Exposure

The organization-api provides data to other services.

### Rules

* Only necessary fields should be exposed
* Internal data must not leak unnecessary details
* External consumers must rely on validated responses

---

## Output

### OrganizationResponseDto

Contains:

* organizationId
* organizationUnitId
* additional metadata (name, etc.)

---

## Error Handling

### Expected Scenarios

* Invalid join code
* Organization not found
* Unit not found
* Inactive organization or unit

### Rules

* Errors must return clear and consistent messages
* Validation failures must prevent further processing
* Join code errors must not expose internal details

---

## Invariants

The following conditions must always be true:

* Every unit belongs to exactly one organization
* Every join code maps to a valid organization and unit
* Organization and unit relationships must be consistent
* Inactive entities must not be used for associations

---

## Consistency Strategy

The organization-api operates with strong consistency.

### Principles

* Organization and unit data must always reflect the latest state
* Join code validation must always be accurate
* Other services depend on this API for reliable organization data

---

## Example Flows

## Create Organization

1. Client sends request to create organization
2. organization-api validates input
4. organization-api persists organization

---

## Create Organization Unit

1. Client sends request to create unit
2. organization-api validates organization existence
3. organization-api persists unit linked to organization

---

## Join Organization

1. Client provides `joinCode`
2. user-api calls organization-api
3. organization-api validates join code
4. organization-api returns:

    * organizationId
    * organizationUnitId

---

## Relationship with Other Services

### Upstream

* Client applications

### Downstream Consumers

* `user-api` (join code validation)
* Any service that needs organization context

---

## Notes

* organization-api does not manage users directly
* organization-api does not handle events or notifications
* organization-api is the single source of truth for organization structure
* All services must rely on this API for organization validation

---
