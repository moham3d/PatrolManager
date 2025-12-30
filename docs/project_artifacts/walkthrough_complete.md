# Walkthrough - PatrolShield Enterprise (Final v1.1)

## 🚀 Key Updates: RBAC & Workflow

We have addressed critical security feedback and enabled full "Guard Mode" via the browser.

### 1. 🛡️ RBAC Overhaul (Security)
-   **Problem**: Guards could see/edit sites they didn't belong to.
-   **Fix**:
    -   **Visibility**: `SiteController` now filters the "My Sites" list based on assignments.
    -   **Protection**: `POST` (Edit/Create) routes are now strictly protected by `ensureRole(['admin', 'manager'])`.
    -   **New Role**: Added `Supervisor` role (same permissions as Guard + Print QR Codes).

### 2. 👮 Guard Workflow (Browser Simulator)
-   **Clock In**: Added a "CLOCK IN" button to the Guard Dashboard (`/dashboard`) that simulates GPS coordinates.
-   **Manual**: Added a detailed **Guard Manual** (`/docs/guard`) explaining the flow.

### 3. 👥 Roles & Hierarchy
| Role | Permissions |
| :--- | :--- |
| **Admin** | Full System Access (All Sites, All Users) |
| **Manager** | Manage assigned Sites, Create Shifts, View Reports |
| **Supervisor** | View Assigned Sites, Print QRs, Manage Incidents |
| **Guard** | View Assigned Site, Patrol, Report Incident (Mobile/Web) |

## 🧪 Credentials (Reset)
The database has been re-seeded with the following accounts (Password: `password123`):

-   **Admin**: `admin@patrol.eg`
-   **Manager**: `manager@patrol.eg`
-   **Supervisor**: `supervisor@patrol.eg`
-   **Guard**: `ahmed@patrol.eg` (Assigned to *Cairo Festival City*)

## 🏁 How to Test
1.  **Login as Admin**: Verify you see ALL sites.
2.  **Login as Guard (Ahmed)**: Verify you ONLY see *Cairo Festival City* and CANNOT edit it.
3.  **Login as Manager**: Verify you can Assign Shifts and Edit your Site.
