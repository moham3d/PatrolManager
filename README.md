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

-   **Backend**: Node.js (v20+), Express.js (v5), Sequelize ORM.
-   **Frontend (Web)**: EJS (Server-side rendering), TailwindCSS, Leaflet.js, Chart.js.
-   **Mobile**: Native Android (Kotlin, Jetpack Compose, CameraX, Room).
-   **Database**: SQLite (Dev), PostgreSQL (Prod).
-   **Real-time**: Socket.IO.

## ⚡ Quick Start

### Development Environment

1.  **Clone the repository**
    ```bash
    git clone https://github.com/your-org/PatrolManager.git
    cd PatrolManager
    ```

2.  **Install Dependencies**
    ```bash
    npm install
    ```

3.  **Start the Server**
    ```bash
    npm run dev
    ```

4.  **Access Web Dashboard**
    -   [http://localhost:3000](http://localhost:3000)

### Android App
1. Open the `android/` directory in Android Studio.
2. Build and run on an emulator or physical device.