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
3. Prepare your database (`ìnit.sql`).

### Running (Locally / Docker)
#### Using Docker / Docker Compose
```bash
docker-compose up --build
```
This will spin up the database

#### Usage / API Endpoints

Environment Variables / Configuration
Example configuration variables you likely need:
Key	Description
- DB_HOST	Database host
- DB_PORT	Database port
- DB_NAME	Database name
- DB_USER	Database user
- DB_PASSWORD	Database password
- MAILFLOW_API_PORT	Port where this API listens
- SMTP_HOST / SMTP_PORT	(If integrated) SMTP / mail server settings
- LOG_LEVEL	Logging verbosity

Include a sample application.yml.example or .env.example file in the repo to help new developers.

### Testing
Unit tests for service, controller, and repository layers
Integration tests (e.g. spinning up an in-memory DB or test containers)
Use ./gradlew test to run all tests
Deployment
Build an executable JAR:
./gradlew clean bootJar

### Use Docker image built via Dockerfile
Deploy to your preferred environment (Kubernetes, AWS ECS, DigitalOcean, etc.)
Ensure proper environment configuration, secrets, and DB migrations
