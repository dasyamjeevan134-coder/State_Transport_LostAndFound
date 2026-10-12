# State Public Transport - Lost & Found Management System

> **A centralized, multi-modal transit property recovery network** unifying metro rails, municipal buses, regional railways, and depot safe lockers with automated heuristic item-matching, claim verification, and custody handovers.

---

## 🏛️ System Architecture

- **Frontend**: React 19, Vite, React Router 7, Axios, Lucide Icons, Custom Transit Design System
- **Backend**: Java 21, Spring Boot 3.3.4, Spring Security, JWT (jjwt 0.12.6), Spring Data JPA, Hibernate, SpringDoc OpenAPI / Swagger
- **Database Support**:
  - **Aiven Cloud MySQL 8** (with SSL Encryption `sslMode=REQUIRED`)
  - **Local MySQL 8** (port 3306)
  - **In-Memory H2 Profile** (zero-config offline development fallback)

---

## 🚀 Quick Start Guide

### 1. Backend (Spring Boot 3)

Navigate to the `backend` folder:

```bash
cd backend
```

#### Option A: Running with Aiven Cloud MySQL or Local MySQL
Ensure your database credentials are set in [application.properties](file:///c:/Users/User/Desktop/LostAndFound/backend/src/main/resources/application.properties) or exported as environment variables:
- `DB_URL` (e.g. `jdbc:mysql://<aiven-host>.aivencloud.com:<port>/defaultdb?sslMode=REQUIRED`)
- Or `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`, `DB_SSL=true`

Run via Maven:
```bash
./mvnw spring-boot:run
```

#### Option B: Zero-Config Offline In-Memory Profile (H2)
Run immediately without requiring a running MySQL server:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```
*H2 Web Console available at:* `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:lost_found_db`, user: `sa`, password: empty)

---

### 2. Frontend (React 19 + Vite)

Navigate to the `frontend` folder:

```bash
cd frontend
npm install
npm run dev
```

The web application will launch at `http://localhost:5173`.

---

## 👥 Built-in Demo Personas & Fast One-Click Switcher

The application features instant one-click demo credentials accessible in both the top navigation bar and authentication forms:

| Role | Name | Email | Password | Access Capabilities |
| :--- | :--- | :--- | :--- | :--- |
| **Passenger** | Elena Rostova | `passenger@transport.gov` | `Pass@12345` | Report lost items, browse public directory, file ownership claims, review match scores |
| **Depot Staff** | Officer Robert Vance | `staff@transport.gov` | `Staff@12345` | Register found property, review & verify passenger claims, issue custody return certificates |
| **Administrator** | System Administrator | `admin@transport.gov` | `Admin@12345` | Full system clearance, analytics, user management |

---

## ✨ Key Features & Workflows

1. **Multi-Modal Transit Directory**: Filter recovered or lost items across Metro Rapid Transit, State Buses, Regional Rail, and Tram networks.
2. **Heuristic AI Correlation Engine**: Calculates weighted compatibility scores across Category (35%), Transit Line (15%), Transit Mode (15%), Station/Location (15%), Time Window (10%), and Keyword Overlap (10%).
3. **Claims Verification Workbench**: Depot staff review uploaded ownership proofs, receipts, or distinguishing features before authorizing property release.
4. **Official Handover Registry & Printable Receipts**: Issues verified return certificates with legal recipient identification and depot custody release logs.
5. **Aiven Console Cloud Database Helper**: Built-in modal in the top navigation bar providing connection diagnostics and direct connection properties copy.
