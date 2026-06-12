# 🥋 Event Manager Platform

A real-world event management platform built to support Capoeira communities through event organization, reservations, notifications, and cultural engagement.

The project was designed as a complete microservices ecosystem focused on scalability, asynchronous communication, multi-tenancy, and mobile synchronization.

---

# 🚀 Project Goals

This project has two primary objectives:

### Social Impact

Provide an accessible platform that helps Capoeira groups organize and promote cultural events while strengthening community engagement.

### Technical Exploration

Serve as a playground for modern software architecture and engineering practices, including:

* Event-Driven Architecture
* Microservices
* Distributed Systems
* Eventual Consistency
* Multi-Tenant Design
* Mobile Synchronization
* Real-Time Notifications
* CI/CD Automation
* AI-Assisted Code Reviews

---

# 💡 Why This Project Matters

Unlike typical CRUD portfolio projects, this platform explores several real-world engineering challenges:

* Event-driven communication
* Distributed data synchronization
* Multi-tenant isolation
* Real-time notifications
* Asynchronous processing
* Role-based security
* Eventual consistency
* Mobile offline synchronization
* Automated quality gates
* AI-assisted code reviews

The goal was not only to build an application but to experiment with architectural patterns commonly found in production environments.

---

# 🧩 Core Features

## Event Management

* Create and update events
* Event image upload
* Public, Organization and Unit visibility scopes
* Real-time event propagation

## Reservations

* Reserve event items
* User reservation dashboard
* Reservation statistics for administrators
* Availability tracking

## Favorites

* Save favorite events
* Device synchronization
* Personalized event experience

## Notifications

* Real-time Firebase notifications
* Topic-based delivery
* Organization-level notifications
* Unit-level notifications

## Administration

* Organization management
* Unit management
* Role-based access control
* Multi-tenant governance

---

# 🏗️ Architecture Overview

The platform follows an event-driven architecture where event creation, persistence and notification delivery are fully decoupled.

```text
Client
   │
   ▼
Event API
   │
   ▼
RabbitMQ
   │
   ▼
Processor API
   │
   ▼
Notification API
   │
   ▼
Firebase Cloud Messaging
   │
   ▼
Mobile Application
```

---

# 🔧 Microservices

## 👤 User API

Responsibilities:

* Authentication
* Authorization
* JWT generation
* User management
* Organization association via joinCode

---

## 📌 Event API

Responsibilities:

* Event creation
* Event updates
* Business rule validation
* Asset upload (AWS S3)
* RabbitMQ publishing

---

## ⚙️ Processor API

Responsibilities:

* RabbitMQ consumption
* Event persistence
* PostgreSQL management
* Source of truth for events

---

## 🔔 Notification API

Responsibilities:

* Firebase Cloud Messaging integration
* Firestore synchronization
* Topic resolution
* Notification delivery
* Full synchronization support

---

## 🏢 Organization API

Responsibilities:

* Organization management
* Unit management
* JoinCode generation
* Multi-tenant structure

---

# 🔄 Technical Challenges Solved

## Eventual Consistency

Events are processed asynchronously through RabbitMQ while maintaining consistency across independent services.

## Multi-Tenant Isolation

Users only access data belonging to their organization and unit.

## Distributed Synchronization

A full synchronization strategy allows mobile devices to recover from data inconsistencies.

## Real-Time Notification Routing

Notifications are dynamically routed based on organization and unit membership.

---

# 🔔 Notification Strategy

## Topic-Based Delivery

| Scope        | Topic                     |
| ------------ | ------------------------- |
| Public       | public                    |
| Organization | org_<organizationId>      |
| Unit         | unit_<organizationUnitId> |

### Example

A user from:

* Organization 1
* Unit 10

Receives notifications from:

* public
* org_1
* unit_10

---

# 🔄 Full Sync Strategy

The platform implements a scope-based full synchronization mechanism.

Benefits:

* Device recovery after offline periods
* Event reconciliation
* Tenant isolation preservation
* Reduced data inconsistencies

Documentation:

```text
/docs/flows/full-sync-flow.md
```

---

# 🔐 Authentication & Authorization

Authentication is centralized through the User API using JWT.

Example token:

```json
{
  "sub": "user@email.com",
  "roles": ["ADMIN"],
  "userId": 1,
  "organizationId": 10,
  "organizationUnitId": 5
}
```

Flow:

1. User authenticates
2. JWT is issued
3. Token is propagated across services
4. Services enforce role-based authorization

---

# 🗄️ Data Storage

| Technology | Purpose                     |
| ---------- | --------------------------- |
| PostgreSQL | Core transactional data     |
| MongoDB    | Event API support           |
| Firestore  | Event synchronization layer |
| AWS S3     | Image and file storage      |

---

# 🐇 Messaging

RabbitMQ is used for:

* Event propagation
* Service decoupling
* Asynchronous processing
* Scalability
* Eventual consistency

---

# 🧪 Quality Engineering

This project includes multiple automated quality gates.

### Testing

* Unit Tests
* Integration Tests
* Testcontainers

### Static Analysis

* Qodana

### Code Quality

* Code Coverage Reports

### AI-Assisted Code Review

GitHub Actions + OpenRouter

The AI review pipeline analyzes Pull Requests and provides suggestions related to:

* SOLID principles
* Clean Code
* Security
* Testability
* Maintainability
* Spring Boot best practices

Final approval remains human-driven.

---

# ⚙️ DevOps

### Docker

```bash
docker compose up --build -d
```

### CI/CD Pipeline

Quality Gates:

* Build Validation
* Automated Tests
* Coverage Analysis
* Qodana Static Analysis
* AI-Assisted Code Review

---

# 📊 Architecture Diagrams

### Architecture Overview

![Architecture](./docs/images/architecture-diagram.png)

### Notification Flow

![Notification Flow](./docs/images/notification-flow.png)

---

# 🌐 API Documentation

### Swagger

Event API

```text
http://localhost:8080/swagger-ui/index.html
```

Organization API

```text
http://localhost:8081/swagger-ui/index.html
```

User API

```text
http://localhost:8082/swagger-ui/index.html
```

---

# 📚 Documentation

## Architecture

* docs/architecture/overview.md
* docs/architecture/integrations.md

## Business Rules

* docs/business-rules/event-api.md
* docs/business-rules/processor-api.md
* docs/business-rules/notification-api.md
* docs/business-rules/user-api.md
* docs/business-rules/organization-api.md

## Flows

* docs/flows/notification-flow.md
* docs/flows/full-sync-flow.md

---

# 🛠️ Technology Stack

## Backend

* Java 17
* Spring Boot
* Spring Security
* JWT

## Messaging

* RabbitMQ

## Databases

* PostgreSQL
* MongoDB
* Firestore

## Cloud

* AWS S3
* Firebase Cloud Messaging

## DevOps

* Docker
* GitHub Actions
* Qodana
* OpenRouter

---

# 🚀 Future Improvements

* API Gateway (Spring Cloud Gateway)
* OpenTelemetry
* Distributed Tracing
* ELK Stack
* Centralized Logging
* Rate Limiting
* Kubernetes Deployment
* Service Discovery

---

# 👨‍💻 Author

Ricardo Rodrigues Alves

Backend Engineer | Java | Spring Boot | Microservices | Android

Building software with a focus on scalability, maintainability, and real-world architecture challenges.
