# System Architecture

PatrolShield is built on a modern, containerized microservices-ready architecture designed for scalability, reliability, and offline capability.

---

## High-Level Diagram

```mermaid
graph TD
    Client_Web[Web Dashboard] -->|HTTPS| LoadBalancer
    Client_Mobile[Mobile App (PWA)] -->|HTTPS| LoadBalancer
    
    LoadBalancer --> API[FastAPI Backend]
    
    API --> DB[(PostgreSQL)]
    API --> FileStore[File Storage]
    
    subgraph "Mobile Offline Sync"
        Client_Mobile <-->|Read/Write| LocalDB[(IndexedDB)]
        Client_Mobile -.->|Background Sync| API
    end
```

---

## Component Details

### 1. Frontend Layer
-   **Technology**: React 18, Vite, TailwindCSS.
-   **Structure**: Monorepo-style source folder separating standard Web views and Mobile views.
-   **Mobile PWA**:
    -   Service Workers for asset caching.
    -   `IndexedDB` for structured data storage (Users, Sites, Routes).
    -   Optimized for touch interfaces and varying viewport sizes.

### 2. Backend Layer
-   **Technology**: Python FastAPI.
-   **Role**: REST API provider and business logic enforcer.
-   **Key Modules**:
    -   `Auth`: JWT handling, specialized mobile login with device fingerprinting.
    -   `Sites`: Hierarchical management of physical locations.
    -   `Patrols`: Logic for route verification and completion.
    -   `Sync`: specialized endpoints to handle batch uploads from mobile devices and conflict resolution.

### 3. Data Layer
-   **Primary Database**: PostgreSQL.
    -   Stores all relational data (Users, Sites, Patrol Logs).
    -   Utilizes PostGIS (optional extension) for advanced geospatial queries.
-   **File Storage**:
    -   Local filesystem (Development).
    -   S3-compatible (Production) for storing incident photos and reports.

---

## Security Architecture

### Authentication
-   **JWT (JSON Web Tokens)**: Used for stateless authentication.
-   **Dual Auth Pipelines**:
    -   Standard Web Auth: Short-lived tokens.
    -   Mobile Auth: Long-lived refresh tokens with device binding to prevent unauthorized device usage.

### Authorization
-   **RBAC (Role-Based Access Control)**:
    -   Permissions are granular (e.g., `patrols:view`, `sites:manage`).
    -   Roles (Manager, Supervisor, Guard) are collections of these permissions.

### Data Protection
-   **Transport**: All data in transit is encrypted via TLS 1.3.
-   **At Rest**: Sensitive fields (passwords) are hashed using Argon2.

---

## Data Synchronization Strategy (Hybrid)

PatrolShield uses a hybrid approach to handle connectivity:

1.  **Real-Time First**:
    -   The Mobile App attempts to send data (scans, reports, clock-ins) immediately via standard REST API endpoints (e.g., `POST /api/shifts/clock-in`).
    -   Crucial for live dashboard accuracy.

2.  **Offline Buffering**:
    -   If the network is unavailable, requests are queued locally in `IndexedDB`.
    -   A background service retries these individual requests when connectivity is restored.
    -   This simplifies the backend architecture by maintaining a standard REST interface for both online and buffered offline actions.
