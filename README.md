# mailflow-api

[![Java CI](https://img.shields.io/badge/build-passing-green)](#)  

> A backend API for Mailflow — handles email processing, message flows, and integrations.

## About

`mailflow-api` is the server-side component for the Mailflow system. It is responsible for receiving, processing, routing, and persisting email-related events and workflows. It is forked from [SmallServiceTechnologies/mailflow-api]. :contentReference[oaicite:0]{index=0}

## Features

- REST API for managing mail events  
- Workflow / routing logic (e.g. inbound, outbound flows)  
- Integration with database for persistence  
- Docker / container support for ease of deployment  
- Modular structure for extendability  

## Architecture / Modules

The project uses a modular layout. Key modules include:

- `mailflow-common`: Shared utilities, DTOs, domain models, etc.  
- `src/` (main application): API controllers, services, data access, etc.  
- Docker / `docker-compose.yaml` for infrastructure orchestration  
- `init.sql`: Initial schema / seed SQL file

<img width="1169" height="827" alt="MailFlow Architecture Diagram" src="https://github.com/user-attachments/assets/f9ae996b-8bfa-4d16-b65c-d46207afb282" />


## Getting Started

### Requirements

- Java 17+ (or configured Java version)  
- Gradle  
- PostgreSQL (or the configured database)  
- Docker & Docker Compose (if running via containers)  

### Setup & Configuration

1. Clone the repo:

   ```bash
   git clone https://github.com/Moritzslz/mailflow-api.git
   cd mailflow-api
   ```
2. Copy or create a configuration file (e.g. `application.yml` / `.env`) with necessary environment variables (see Environment Variables).
   2.1 You need to change the following variable in the application properties:
   - spring.mail.host
   - spring.mail.port
   - spring.mail.username
4. Prepare your database (`ìnit.sql`).
   4.1 You can leave it like it is for now but it is recommened to at least have a look at it and understand it

### Running (Locally / Docker)

### [Postman Collection](https://app.getpostman.com/join-team?invite_code=21f8ac5d0e967131c6a9cf795897ff54811c386a6cb1bb24eadb221781835d14&target_code=f0930bdbfd16b2f4d599919b20446392)

#### Using Docker / Docker Compose
```bash
docker-compose up --build
```
This will spin up the database:
- PGADMIN_DEFAULT_EMAIL: admin@admin.com
- PGADMIN_DEFAULT_PASSWORD: root

#### Usage / API Endpoints

Customer = Tenant

- `/api/v1/auth/token`
-    Issues JWTs
- `/api/v1/customers/{customerId}/users/{userId}/blacklist`
-    Manages blacklisted email adresses that are automatically excluded from processing by mailflow for the users email account
- `/api/v1/clients`
-    Manages connected clients (=mircoservices)
- `/api/v1/customers/`
-    Manages tenants (CRUD and more)
- `/api/v1/customers/{customerId}/message-categories`
-    (CRUD) Manages a tenants message categories which are then used for classification
- `/api/v1/customers/{customerId}/users/{userId}/message-log`
-    Manages database log of every processes email
- `/api/v1/customers/{customerId}/rag-urls`
-    Manages the RAG Urls configured on a customer level for the RAG context that will be crawled and used by mailflow to based email answers on
- `/api/v1/customers/{customerId}/users/{userId}/response-ratings`
-    This feature creates a link for each email allowing the receipients to rate their satisfaction with the response
- `/api/v1/customers/{customerId}/users/{userId}/settings`
-    Configures mailflow
- `/api/v1/customers/{customerId}/users/`
-    CRUD with double opt in an more for users

### Environment Variables / Configuration
Example configuration variables you likely need:
Key	Description
- ACTIVE_PROFILE=dev;
- AES_B64_SECRET_KEY=someKey; Please create a B64 encoded AES key
- CLIENT_NAME=mailflow-api;
- CLIENT_SECRET=secret;
- DB_PASSWORD=root;
- DB_URL=jdbc:postgresql://localhost:5432/test_db;
- DB_USERNAME=admin;
- GOOGLE_RECAPTCHA_SECRET_KEY=someKey;
- HMAC_B64_SECRET_KEY=someKey; Please create a B64 encoded HMAC key
- MAIL_PASSWORD=somePassword;   The password to the mail account which is specified in the `spring.mail.username` application property
- RSA_PRIVATE_KEY=-----BEGIN PRIVATE KEY----- someKeySequence -----END PRIVATE KEY----- ; Please create a RSA keypair
- RSA_PUBLIC_KEY=-----BEGIN PUBLIC KEY----- someKeySequence -----END PUBLIC KEY----- Please create a RSA keypair

Include a sample application.yml.example or .env.example file in the repo to help new developers.

### Testing
Unit tests for service, controller, and repository layers
Integration tests (e.g. spinning up an in-memory DB or test containers)
Use ./gradlew test to run all tests (should work without env vars)
Deployment
Build an executable JAR:
./gradlew clean bootJar

### Use Docker image built via Dockerfile
Deploy to your preferred environment (Kubernetes, AWS ECS, DigitalOcean, etc.)
Ensure proper environment configuration, secrets, and DB migrations
