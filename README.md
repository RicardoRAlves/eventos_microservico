# 🥋 Event Manager - Microservices Architecture

A scalable **event-driven microservices platform** designed to manage Capoeira events across organizations and units, with real-time notifications and distributed data synchronization.

---

## 🚀 Overview

This project demonstrates a **real-world backend architecture** built with:

* Event-driven communication (RabbitMQ)
* Multi-tenant design (organization + unit)
* Real-time updates (Firebase Cloud Messaging)
* Distributed data synchronization (Full Sync strategy)

It was designed to solve a common problem:

> How to reliably deliver and synchronize event data across multiple users and devices in a scalable way.

---

## 🧠 Key Concepts

* **Event-driven architecture**
* **Topic-based notification (FCM)**
* **Multi-tenant isolation**
* **Eventual consistency**
* **Scoped data synchronization (GET_ALL)**

---

## 🧩 Microservices Architecture

### 👤 User API

* Authentication & authorization (JWT)
* User management
* Organization association via `joinCode`

---

### 📌 Event API

* Entry point for event creation
* Validates business rules
* Uploads assets (AWS S3)
* Publishes events to RabbitMQ

---

### ⚙️ Processor API

* Consumes events from RabbitMQ
* Persists data in PostgreSQL
* Acts as the **source of truth**

---

### 🔔 Notification API

* Integrates with Firebase (FCM + Firestore)
* Resolves delivery topics
* Sends real-time notifications
* Handles full sync (`GET_ALL`)

---

### 🏢 Organization API

* Manages organizations and units
* Generates and validates `joinCode`
* Provides multi-tenant structure

---

## 🔄 Architecture Flow

```text
Client → Event API → RabbitMQ → Processor API → Notification API → FCM → Mobile App
```

---

## 🔔 Notification & Sync Strategy

### Topic-Based Delivery

| Scope        | Topic                       |
| ------------ | --------------------------- |
| PUBLIC       | `public`                    |
| ORGANIZATION | `org_<organizationId>`      |
| UNIT         | `unit_<organizationUnitId>` |

---

### Example

A user from:

* organization `1`
* unit `10`

Receives events from:

* `public`
* `org_1`
* `unit_10`

---

## 🔄 Full Sync (Data Reconciliation)

The system implements a **scope-based full sync strategy**:

* Fixes inconsistencies across devices
* Avoids global data overwrite
* Ensures tenant isolation

👉 See:
`/docs/flows/full-sync-flow.md`

---

## 📊 Documentation

Detailed architecture and business rules are documented:

### 📁 Architecture

* `docs/architecture/overview.md`
* `docs/architecture/integrations.md`

### 📁 Business Rules

* `docs/business-rules/event-api.md`
* `docs/business-rules/processor-api.md`
* `docs/business-rules/notification-api.md`
* `docs/business-rules/user-api.md`
* `docs/business-rules/organization-api.md`

### 📁 Flows

* `docs/flows/notification-flow.md`
* `docs/flows/full-sync-flow.md`

---

## 🔐 Authentication & Authorization

Authentication is centralized in **User API** using JWT.

### Example Token

```json
{
  "sub": "user@email.com",
  "roles": ["ADMIN"],
  "userId": 1,
  "organizationId": 10,
  "organizationUnitId": 5
}
```

### Flow

1. User logs in via User API
2. Receives JWT
3. Sends token in requests
4. Services validate and enforce roles

---

## 📡 Example Request

### Login

```bash
curl -X POST http://localhost:8082/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@test.com",
    "password": "123456"
  }'
```

---

## 🗄️ Data Storage

* **PostgreSQL** → main data (processor + organization)
* **MongoDB** → event-api support
* **Firestore** → event snapshot layer
* **AWS S3** → file storage

---

## 📡 Messaging

* RabbitMQ for async communication
* Decoupled services
* Scalable event propagation

---

## 📊 Diagrams

### Architecture

![Architecture](./docs/assets/diagram.png)

---

### RabbitMQ Flow

![RabbitMQ](./docs/assets/diagramRabbitMq.png)

---

### Notification Flow

![Notification Flow](./docs/assets/notification-flow.png)

---

## ▶️ Running the Project

```bash
docker compose up --build -d
```

---

## 🌐 Swagger

* Event API → http://localhost:8080/swagger-ui/index.html
* Organization API → http://localhost:8081/swagger-ui/index.html
* User API → http://localhost:8082/swagger-ui/index.html

---

## 🐇 RabbitMQ

http://localhost:15672/

```
username: rabbitmq
password: rabbitmq
```

---

## 🧠 Tech Stack

* Java 17 + Spring Boot
* Spring Security (JWT)
* RabbitMQ
* PostgreSQL
* MongoDB
* Firebase (FCM + Firestore)
* AWS S3
* Docker

---

## 💡 Highlights (Why this project matters)

* Real-world microservices architecture
* Event-driven communication
* Scalable notification system (FCM topics)
* Multi-tenant data isolation
* Distributed sync strategy (advanced topic)

---

## 🚀 Future Improvements

* API Gateway (Spring Cloud Gateway)
* Observability (OpenTelemetry)
* Centralized logging (ELK)
* CI/CD pipeline
* Rate limiting

---

## 👨‍💻 Author

**Ricardo Rodrigues Alves**
Backend Developer | Java | Microservices | Android

---
