# PatrolShield - Security Management System

PatrolShield is a comprehensive, site-centric security management platform designed to streamline operations for security companies. It provides a seamless workflow for managing sites, zones, checkpoints, shifts, and patrols, connecting managers in the office with guards in the field via a robust mobile application.

## 🚀 Product Overview

PatrolShield bridges the gap between physical security operations and digital management.

-   **Site-Centric Architecture**: All operations revolve around physical sites, ensuring logical organization of zones, users, and patrols.
-   **Role-Based Access**: Specialized interfaces for Site Managers, Supervisors, and Guards.
-   **Real-Time Monitoring**: Live tracking of patrol progress, guard locations, and incident reports.
-   **Offline-First Mobile App**: Guards can operate seamlessly without internet connectivity, syncing data when back online.
-   **Verifiable Security**: GPS and QR/NFC checkpoint scanning ensure patrols are actually performed.

## 👥 User Roles

PatrolShield serves three primary distinct user roles:

1.  **👨‍💼 Site Manager**
    -   **Focus**: Strategic oversight and configuration.
    -   **Capabilities**: Create sites/zones, assign users, schedule shifts, view analytics, and manage emergency protocols.
    -   **Interface**: Web Dashboard & Mobile Manager View.

2.  **👮‍♂️ Supervisor**
    -   **Focus**: Tactical field management.
    -   **Capabilities**: Monitor active patrols, track guard locations, handle escalated incidents, and assigning ad-hoc tasks.
    -   **Interface**: Mobile Supervisor View.

3.  **🛡️ Guard**
    -   **Focus**: Execution and reporting.
    -   **Capabilities**: Execute patrol routes, scan checkpoints, report incidents, and trigger emergency SOS.
    -   **Interface**: Mobile Guard View (PWA).

## 🛠️ Technology Stack

-   **Backend**: Python (FastAPI), PostgreSQL, SQLAlchemy.
-   **Frontend (Web)**: React, Vite, TailwindCSS.
-   **Mobile**: React PWA (Progressive Web App) with offline capabilities (Service Workers, IndexedDB).
-   **Infrastructure**: Docker, Kubernetes ready.

## 📚 Documentation Index

-   **[Product Overview](docs/PRODUCT_OVERVIEW.md)**: Detailed breakdown of workflows and capabilities.
-   **[User Guide](docs/USER_GUIDE.md)**: Manual for Managers, Supervisors, and Guards.
-   **[Developer Guide](docs/DEVELOPER_GUIDE.md)**: Setup, architecture, and contribution guidelines.
-   **[Architecture](docs/ARCHITECTURE.md)**: System design, data flow, and security details.
-   **[API Documentation](docs/)**: Detailed API specifications.

## ⚡ Quick Start

### Prerequisites
-   Docker & Docker Compose

### Development Environment

1.  **Clone the repository**
    ```bash
    git clone https://github.com/your-org/PatrolShield.git
    cd PatrolShield
    ```

2.  **Start Services**
    ```bash
    docker-compose up -d
    ```

3.  **Access Interfaces**
    -   **Web App**: [http://localhost:3000](http://localhost:3000)
    -   **API Docs**: [http://localhost:8000/docs](http://localhost:8000/docs)

For detailed development instructions, see the [Developer Guide](docs/DEVELOPER_GUIDE.md).