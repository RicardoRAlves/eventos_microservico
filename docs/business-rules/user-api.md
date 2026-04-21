# 👤 User API - Business Rules

## Purpose

The **user-api** is responsible for user management, authentication, and association with organizational structures.

It defines how users are identified, authenticated, and linked to organizations and units within the system.

---

## Responsibilities

* Handle user registration and authentication
* Manage user credentials and roles
* Associate users with organizations and organization units
* Validate access-related rules
* Provide user identity data for other services

---

## Input Sources

### REST Endpoints

Typical operations:

* User registration
* User authentication (login)
* User association with organization (via join code)
* Retrieve user profile and associations

---

## Core Business Rules

## 1. User Registration

A user must be created with valid credentials.

### Rules

* Email must be unique
* Email must follow a valid format
* Password must meet minimum requirements
* A user may be created without an organization initially

---

## 2. Authentication

The user must be authenticated before accessing protected resources.

### Rules

* Authentication must validate email and password
* On success, a token (e.g., JWT) must be generated
* The token must include user identity and roles

---

## 3. Organization Association

Users can be linked to an organization using a `joinCode`.

### Rules

* The `joinCode` must be validated via `organization-api`
* A valid `joinCode` returns:

    * `organizationId`
    * `organizationUnitId`
* The user must be associated with the returned identifiers

---

## 4. Organization Constraints

### Rules

* A user must belong to only one organization context at a time (recommended)
* A user must be associated with a valid organization before accessing restricted features
* Organization and unit must always be consistent

---

## 5. Role Management

Users may have roles that define access permissions.

### Example roles

* ADMIN
* CLIENT

### Rules

* Roles must be assigned at user creation or association
* Role-based access must be enforced at API level
* Sensitive operations must require elevated roles (e.g., ADMIN)

---

## 6. Access Control

User access must be restricted based on identity and association.

### Rules

* A user can only access data related to:

    * their organization
    * their organization unit
* A user must not access data from other organizations

---

## 7. Identity Propagation

User identity must be propagated to other services when required.

### Rules

* Downstream services must receive:

    * userId
    * organizationId
    * organizationUnitId
* Identity must be extracted from the authentication token

---

## Output

### Authentication Token (JWT)

Contains:

* userId
* roles
* organizationId (if available)
* organizationUnitId (if available)

---

### User Data

Used by other services for:

* authorization checks
* filtering data
* enforcing scope boundaries

---

## Error Handling

### Expected Scenarios

* Invalid credentials
* Duplicate email
* Invalid join code
* Organization not found
* Unauthorized access

### Rules

* Errors must return clear messages
* Authentication errors must not expose sensitive information
* Invalid associations must be rejected

---

## Invariants

The following conditions must always be true:

* Each user has a unique email
* A user must not belong to multiple conflicting organizations
* Organization association must be validated externally
* User identity must be reliable and verifiable
* Unauthorized access must always be prevented

---

## Consistency Strategy

The user-api operates in a strongly consistent manner.

### Principles

* Authentication must always reflect the latest user state
* Organization association must be validated before persistence
* Identity data must remain consistent across services

---

## Example Flows

## User Registration

1. Client sends registration request
2. user-api validates input
3. user-api checks email uniqueness
4. user-api saves user
5. user-api returns success response

---

## User Login

1. Client sends credentials
2. user-api validates credentials
3. user-api generates JWT token
4. user-api returns token to client

---

## Join Organization

1. Client sends `joinCode`
2. user-api calls organization-api
3. organization-api validates code
4. user-api associates user with organization and unit
5. user-api updates user record

---

## Relationship with Other Services

### External

* `organization-api` (for join code validation)

### Downstream Consumers

* All services that require identity and authorization context

---

## Notes

* user-api does not manage event data
* user-api does not manage notifications
* user-api is the source of truth for identity and access control
* Authorization must be enforced across all services using user context

---
