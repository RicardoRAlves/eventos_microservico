# events_microservice

This project is an event-driven microservices architecture designed to manage Capoeira events.

It is composed of multiple Spring Boot services that communicate asynchronously using RabbitMQ, and integrate with external services such as AWS S3 and Firebase.

---

## 🧩 Architecture Overview

The system is composed of the following microservices:

### 📌 Event API
- Receives requests via REST endpoints to create new events
- Uploads event-related data (e.g., images) to AWS S3
- Sends messages to RabbitMQ to trigger event processing
- Can request all events to be sent to clients (Android)

---

### ⚙️ Processor API (Event Database API)
- Consumes messages from RabbitMQ
- Persists events into the database (PostgreSQL)
- Publishes messages to notify clients about new or updated events

---

### 🔔 Notification API
- Receives event payloads
- Sends push notifications to Android clients using Firebase (FCM)

> ⚠️ If running locally via IntelliJ, you may need to add the following VM options:
> --add-opens java.base/java.time.chrono=ALL-UNNAMED
--add-opens java.base/java.time.format=ALL-UNNAMED
--add-opens java.base/java.time.temporal=ALL-UNNAMED
--add-opens java.base/java.time.zone=ALL-UNNAMED
---

### 🏢 Organization API (NEW)
- Manages organizations and their units (e.g., Capoeira groups)
- Provides CRUD operations for organizations and units
- Can be integrated with events to associate events with specific organizations
- Uses PostgreSQL as its data source

---

## 🗄️ Databases

- **MongoDB** → Event API (temporary or document-based data)
- **PostgreSQL** → Processor API & Organization API
- **AWS S3** → Storage for images/files

---

## 🔄 Messaging

- RabbitMQ is used for asynchronous communication between services
- Ensures decoupling between APIs and better scalability

---

## 📊 Architecture Diagram

![diagram.png](diagram.png)

---

## 📨 RabbitMQ Flow

![diagramRabbitMq.png](diagramRabbitMq.png)

---

## ▶️ Running the Project

```bash
docker compose up --build -d

🌐 API Documentation (Swagger)
Event API:
http://localhost:8080/swagger-ui/index.html
Organization API:
http://localhost:8081/swagger-ui/index.html

🐇 RabbitMQ Management

Access the RabbitMQ dashboard:
http://localhost:15672/
Credentials:
username: rabbitmq
password: rabbitmq

🧠 Tech Stack
Java / Spring Boot
Docker / Docker Compose
RabbitMQ
PostgreSQL
MongoDB
AWS S3
Firebase Cloud Messaging (FCM)

📌 Notes
Each microservice is responsible for its own database
Communication is asynchronous via RabbitMQ
Designed with scalability and decoupling in mind
