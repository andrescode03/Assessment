# CoopCredit - Credit Application Management System

A modern microservices-based credit application system built with Spring Boot 3.4, implementing **Hexagonal Architecture** (Ports & Adapters), JWT security, advanced JPA persistence, automated testing, and Docker deployment.

---

## 🎯 Project Overview

CoopCredit is a comprehensive credit management system designed for cooperatives that allows:

- **Affiliate Management**: Manage cooperative members and their financial information
- **Credit Application Processing**: Register and evaluate credit requests with automated risk assessment
- **Risk Evaluation**: Integration with an independent risk assessment microservice
- **Business Rules Engine**: Automatic validation of income, installments, seniority, and risk levels
- **Security & Traceability**: JWT-based authentication, role-based authorization, and comprehensive auditing

---

## 🏗️ Architecture

### Hexagonal Architecture (Ports & Adapters)

```
┌─────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Entities   │  │Business Rules│  │  Use Cases   │  │
│  │   Models     │  │              │  │   (Ports)    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                           ↕
┌─────────────────────────────────────────────────────────┐
│               APPLICATION LAYER (Services)               │
│         ┌────────────────────────────────┐              │
│         │  Business Logic Implementation  │              │
│         └────────────────────────────────┘              │
└─────────────────────────────────────────────────────────┘
                           ↕
┌─────────────────────────────────────────────────────────┐
│            INFRASTRUCTURE LAYER (Adapters)               │
│  ┌─────────┐  ┌──────────┐  ┌────────────┐  ┌────────┐│
│  │REST APIs│  │JPA/Flyway│  │  External  │  │Security││
│  │Controllers  │PostgreSQL│  │  Services  │  │  JWT   ││
│  └─────────┘  └──────────┘  └────────────┘  └────────┘│
└─────────────────────────────────────────────────────────┘
```

### Core Principle
> **The domain layer has NO dependencies on Spring, JPA, or any framework.**

---

## 📦 Services

### 1. **CoopCredit Main Application** (Port 8082)

Primary service handling all business logic.

**Features:**
- Affiliate CRUD operations
- Credit application registration and evaluation (two-phase process)
- JWT authentication and role-based authorization
- JPA persistence with Flyway migrations
- REST API exposition
- Actuator metrics and health checks

**Tech Stack:**
- Spring Boot 3.4.0
- Java 21
- PostgreSQL 15
- Spring Security + JWT
- Spring Data JPA + Hibernate
- Flyway
- Lombok
- Testcontainers

### 2. **Risk Central Mock Service** (Port 8083)

Independent microservice simulating a credit risk bureau.

**Features:**
- Single endpoint: `POST /risk-evaluation`
- Returns score and risk level (300-950 range)
- Deterministic response by document (same document = same score)
- Considers amount and term in risk calculation
- No database required
- No authentication required

**Risk Calculation Logic:**
- Base score: Hash-based on document (300-950)
- Amount factor: High amounts (>30M) reduce score by 50, medium (>10M) by 20
- Term factor: Long terms (>60 months) reduce score by 30, medium (>36) by 10
- Classification:
  - 300-500: HIGH RISK (ALTO)
  - 501-700: MEDIUM RISK (MEDIO)
  - 701-950: LOW RISK (BAJO)

---

## 💾 Domain Model

### Affiliate
```java
- id: Long
- document: String (unique)
- name: String
- salary: BigDecimal
- affiliationDate: LocalDate
- status: AffiliateStatus (ACTIVE/INACTIVE)
```

### Credit Application
```java
- id: Long
- affiliate: Affiliate (ManyToOne)
- requestedAmount: BigDecimal
- termMonths: Integer
- proposedRate: BigDecimal
- applicationDate: LocalDateTime
- status: CreditStatus (PENDING/APPROVED/REJECTED)
- riskEvaluation: RiskEvaluation (embedded)
```

### Risk Evaluation
```java
- score: Integer
- riskLevel: String (BAJO/MEDIO/ALTO)
- decisionReason: String
- evaluationDate: LocalDateTime
```

**Relationships:**
- Affiliate 1 → N Credit Applications
- Credit Application 1 → 1 Risk Evaluation
- User N → 1 Affiliate (optional, only for ROLE_AFILIADO)

---

## 🔐 Security

### Authentication & Authorization
- **Stateless JWT** authentication
- **Roles**:
  - `ROLE_ADMIN`: Full access
  - `ROLE_ANALISTA`: Can evaluate applications
  - `ROLE_AFILIADO`: Can create their own applications
- **Password encryption**: BCrypt
- **Token expiration**: 24 hours (configurable)

### Endpoint Protection

| Endpoint | Public | AFILIADO | ANALISTA | ADMIN |
|:---------|:------:|:--------:|:--------:|:-----:|
| POST `/auth/register` | ✅ | - | - | - |
| POST `/auth/login` | ✅ | - | - | - |
| POST `/api/afiliados` | ❌ | ❌ | ✅ | ✅ |
| GET `/api/afiliados/{doc}` | ❌ | ✅ | ✅ | ✅ |
| PUT `/api/afiliados/{doc}` | ❌ | ❌ | ✅ | ✅ |
| POST `/api/solicitudes` | ❌ | ✅ | ✅ | ✅ |
| GET `/api/solicitudes/{id}` | ❌ | ✅ | ✅ | ✅ |
| GET `/api/solicitudes` | ❌ | ✅ | ✅ | ✅ |
| GET `/api/solicitudes/pendientes` | ❌ | ❌ | ✅ | ✅ |
| POST `/api/solicitudes/{id}/evaluar` | ❌ | ❌ | ✅ | ✅ |
| POST `/risk-evaluation` | ✅ | - | - | - |
| `/actuator/**` | ✅ | - | - | - |

---

## 🔄 Two-Phase Credit Application Flow

### Phase 1: Registration (PENDING Status)
```
User → POST /api/solicitudes
       Body: { amount, term }
         ↓
    Validations:
    ✓ Affiliate is ACTIVE
    ✓ Seniority ≥ 6 months
    ✓ Monthly installment ≤ 50% of salary
    ✓ Requested amount ≤ 12x salary
         ↓
    Save with status = PENDING
         ↓
    Response: { id: 1, status: "PENDING", riskEvaluation: null }
```

### Phase 2: Evaluation (APPROVED/REJECTED Status)
```
Analyst → POST /api/solicitudes/1/evaluar
            ↓
       Verify status == PENDING
            ↓
       Call Risk Service:
       POST http://localhost:8083/risk-evaluation
       Body: { documento, monto, plazo }
            ↓
       Response: { score: 642, nivelRiesgo: "MEDIO" }
            ↓
       Business Rule:
       • score ≤ 500 (ALTO) → REJECTED
       • score > 500 → APPROVED
            ↓
       Save with updated status
            ↓
       Response: {
         id: 1,
         status: "APPROVED",
         riskEvaluation: {
           score: 642,
           riskLevel: "MEDIO",
           decisionReason: "Moderate credit history. Score: 642."
         }
       }
```

---

## 🌐 REST API Endpoints

### Authentication

#### Register User
```http
POST /auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123",
  "role": "ROLE_AFILIADO",
  "affiliateDocument": "12345678"  // Optional, required for ROLE_AFILIADO
}
```

Response:
```json
{
  "message": "User registered successfully",
  "role": "ROLE_AFILIADO",
  "linkedAffiliate": "12345678"
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Affiliates

#### Create Affiliate
```http
POST /api/afiliados
Authorization: Bearer {token}
Content-Type: application/json

{
  "document": "12345678",
  "name": "John Doe",
  "salary": 5000000,
  "affiliationDate": "2024-01-15"
}
```

#### Get Affiliate
```http
GET /api/afiliados/12345678
Authorization: Bearer {token}
```

#### Update Affiliate
```http
PUT /api/afiliados/12345678
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "John Doe Updated",
  "salary": 6000000,
  "status": "ACTIVE"
}
```

### Credit Applications

#### Create Application (PENDING)
```http
POST /api/solicitudes
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 10000000,
  "term": 36
  // affiliateDocument optional if user is ROLE_AFILIADO
}
```

Response:
```json
{
  "id": 1,
  "affiliate": {
    "document": "12345678",
    "name": "John Doe"
  },
  "requestedAmount": 10000000,
  "termMonths": 36,
  "applicationDate": "2025-12-09T20:30:00",
  "status": "PENDING",
  "riskEvaluation": null
}
```

#### Get Application
```http
GET /api/solicitudes/1
Authorization: Bearer {token}
```

#### List All Applications
```http
GET /api/solicitudes
Authorization: Bearer {token}
```

#### List Pending Applications
```http
GET /api/solicitudes/pendientes
Authorization: Bearer {token}
```

#### Evaluate Application
```http
POST /api/solicitudes/1/evaluar
Authorization: Bearer {token}
```

Response:
```json
{
  "id": 1,
  "status": "APPROVED",
  "riskEvaluation": {
    "score": 720,
    "riskLevel": "BAJO",
    "decisionReason": "Excellent credit history. Score: 720. Approved.",
    "evaluationDate": "2025-12-09T20:35:00"
  }
}
```

### Risk Evaluation (Mock Service)

```http
POST http://localhost:8083/risk-evaluation
Content-Type: application/json

{
  "documento": "12345678",
  "monto": 10000000,
  "plazo": 36
}
```

Response:
```json
{
  "documento": "12345678",
  "score": 720,
  "nivelRiesgo": "BAJO",
  "detalle": "Excellent credit history. Score: 720. Approved."
}
```

### Observability

```http
GET /actuator/health       # Health check
GET /actuator/info         # Application info
GET /actuator/metrics      # Metrics
```

---

## ✅ Business Validation Rules

1. **Affiliate must be ACTIVE**
2. **Seniority ≥ 6 months** from affiliation date
3. **Debt capacity**: Monthly installment ≤ 50% of salary
4. **Maximum amount**: Requested amount ≤ 12x monthly salary
5. **Risk-based approval**:
   - HIGH RISK (ALTO) → Automatic rejection
   - MEDIUM/LOW RISK → Automatic approval

---

## 🗄️ Database Schema

### Tables

**users**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    affiliate_id BIGINT REFERENCES affiliates(id)
);
```

**affiliates**
```sql
CREATE TABLE affiliates (
    id BIGSERIAL PRIMARY KEY,
    document VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    salary DECIMAL(19,2) NOT NULL,
    affiliation_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL
);
```

**credit_applications**
```sql
CREATE TABLE credit_applications (
    id BIGSERIAL PRIMARY KEY,
    affiliate_id BIGINT NOT NULL REFERENCES affiliates(id),
    requested_amount DECIMAL(19,2) NOT NULL,
    term_months INTEGER NOT NULL,
    proposed_rate DECIMAL(5,4),
    application_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    risk_score INTEGER,
    risk_level VARCHAR(20),
    decision_reason VARCHAR(255),
    evaluation_date TIMESTAMP
);
```

### Migrations
- **Flyway** manages schema versioning
- Location: `src/main/resources/db/migration/`
- Migrations:
  - `V1__init.sql`: Initial schema
  - `V2__add_user_affiliate_link.sql`: User-Affiliate relationship

---

## 🚀 Running the Application

### Prerequisites
- Java 21
- Docker & Docker Compose
- Maven 3.9+

### Option 1: Local Execution

```bash
# 1. Start PostgreSQL
sudo docker compose up postgres -d

# 2. Start Risk Central Mock (Terminal 1)
java -jar risk-central-mock/target/risk-central-mock-1.0.0.jar

# 3. Start CoopCredit App (Terminal 2)
./mvnw spring-boot:run
```

### Option 2: Full Docker Deployment

```bash
# Build both projects
./mvnw clean package -DskipTests
cd risk-central-mock && ../mvnw clean package -DskipTests && cd ..

# Start all services
sudo docker compose up --build
```

### Verification

```bash
# PostgreSQL (port 5433)
nc -zv localhost 5433

# Risk Mock (port 8083)
curl http://localhost:8083/risk-evaluation

# CoopCredit App (port 8082)
curl http://localhost:8082/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

---

## 🧪 Testing

### Test Coverage

**Unit Tests:**
- Service layer with Mockito
- Business rule validation
- Error scenarios

**Integration Tests:**
- Full flow with TestRestTemplate
- Testcontainers for PostgreSQL
- Security endpoint protection
- Happy path scenarios

**Test Files:**
- `CoopCreditApplicationTests.java`: Context loading
- `HappyPathIntegrationTest.java`: End-to-end flow
- `application/service/*Test.java`: Unit tests

### Running Tests

```bash
# All tests
./mvnw test

# Specific test
./mvnw test -Dtest=HappyPathIntegrationTest

# Skip tests
./mvnw package -DskipTests
```

---

## 📊 Project Structure

```
Assessment/
├── src/
│   ├── main/
│   │   ├── java/com/coopcredit/
│   │   │   ├── domain/
│   │   │   │   ├── model/              # Pure domain entities
│   │   │   │   └── port/
│   │   │   │       ├── in/             # Use case interfaces
│   │   │   │       └── out/            # Repository interfaces
│   │   │   ├── application/
│   │   │   │   └── service/            # Business logic implementation
│   │   │   ├── infrastructure/
│   │   │   │   ├── adapter/
│   │   │   │   │   ├── in/web/         # REST controllers
│   │   │   │   │   └── out/
│   │   │   │   │       ├── persistence/ # JPA adapters
│   │   │   │   │       └── external/   # External service clients
│   │   │   │   └── configuration/
│   │   │   │       └── security/       # JWT & Spring Security
│   │   │   └── riskmock/               # Risk mock controller (legacy)
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/           # Flyway SQL scripts
│   └── test/                           # Test classes
├── risk-central-mock/                  # Independent microservice
│   ├── src/main/java/com/riskmock/
│   │   ├── RiskMockApplication.java
│   │   ├── controller/
│   │   └── dto/
│   ├── Dockerfile
│   └── pom.xml
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 🔧 Configuration

### application.properties

```properties
# Application
server.port=8082

# Database
spring.datasource.url=jdbc:postgresql://localhost:5433/coopcredit
spring.datasource.username=coopuser
spring.datasource.password=cooppassword

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# JWT
jwt.secret=7d4c6e5a6f2b7a8d9e1c3f5a8b2d4e7f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d
jwt.expiration=86400000

# Risk Service
risk.service.url=http://localhost:8083/risk-evaluation

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
```

---

## 🐳 Docker Compose

```yaml
services:
  postgres:
    image: postgres:15-alpine
    ports:
      - "5433:5432"
    environment:
      POSTGRES_DB: coopcredit
      POSTGRES_USER: coopuser
      POSTGRES_PASSWORD: cooppassword
    networks:
      - coopcredit-network

  risk-central-mock:
    build: ./risk-central-mock
    ports:
      - "8083:8083"
    networks:
      - coopcredit-network

  coopcredit-app:
    build: .
    ports:
      - "8082:8082"
    environment:
      RISK_SERVICE_URL: http://risk-central-mock:8083/risk-evaluation
    depends_on:
      - postgres
      - risk-central-mock
    networks:
      - coopcredit-network

networks:
  coopcredit-network:
    driver: bridge
```

---

## 📝 Complete End-to-End Example

```bash
#!/bin/bash
URL="http://localhost:8082"

# 1. Register admin
curl -X POST $URL/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","role":"ROLE_ADMIN"}'

# 2. Login
TOKEN=$(curl -s -X POST $URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r .token)

# 3. Create affiliate
curl -X POST $URL/api/afiliados \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "document": "98765432",
    "name": "Jane Smith",
    "salary": 7000000,
    "affiliationDate": "2023-06-01"
  }'

# 4. Register user for affiliate
curl -X POST $URL/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jane",
    "password": "jane123",
    "role": "ROLE_AFILIADO",
    "affiliateDocument": "98765432"
  }'

# 5. Login as affiliate
AFILIADO_TOKEN=$(curl -s -X POST $URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"jane","password":"jane123"}' | jq -r .token)

# 6. Create application (PENDING)
APP_ID=$(curl -s -X POST $URL/api/solicitudes \
  -H "Authorization: Bearer $AFILIADO_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount": 15000000, "term": 48}' | jq -r .id)

echo "Application created with ID: $APP_ID and status: PENDING"

# 7. List pending applications
curl -s $URL/api/solicitudes/pendientes \
  -H "Authorization: Bearer $TOKEN" | jq .

# 8. Evaluate application
curl -s -X POST $URL/api/solicitudes/$APP_ID/evaluar \
  -H "Authorization: Bearer $TOKEN" | jq .
```

---

## 🛠️ Technology Stack

| Layer | Technology |
|:------|:-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.0 |
| Security | Spring Security + JWT |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL 15 |
| Migration | Flyway |
| Build Tool | Maven 3.9+ |
| Testing | JUnit 5 + Mockito + Testcontainers |
| Observability | Spring Actuator + Micrometer |
| Containerization | Docker + Docker Compose |
| Code Generation | Lombok |

---

## 📈 Key Features Implemented

✅ **Hexagonal Architecture**: Clean separation of concerns
✅ **Two-Phase Flow**: Registration (PENDING) → Evaluation (APPROVED/REJECTED)
✅ **Independent Microservices**: Risk Central Mock as separate service
✅ **JWT Security**: Stateless authentication with role-based access
✅ **User-Affiliate Linking**: Automatic association on registration
✅ **Business Rules Engine**: Comprehensive validation (seniority, debt capacity, amount limits)
✅ **Risk-Based Decisions**: Automatic approval/rejection based on score
✅ **Database Migrations**: Flyway for version control
✅ **N+1 Prevention**: EntityGraph for optimized queries
✅ **Global Error Handling**: RFC 7807 Problem Details
✅ **Integration Testing**: Testcontainers for real database tests
✅ **Docker Ready**: Full containerization support

---

## 📄 License

This is an academic project for educational purposes.

---

## 👥 Authors

**CoopCredit Development Team**

---

## 📞 Support

For issues or questions, please create an issue in the project repository.

---

**Built with ❤️ using Spring Boot and Hexagonal Architecture**
