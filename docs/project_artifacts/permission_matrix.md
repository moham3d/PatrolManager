# Permission Matrix

This document outlines the Access Control Levels (AVL) for the PatrolManager system.
The system enforces Role-Based Access Control (RBAC) via the `ensureRole` middleware and granular controller logic.

## Roles Defined

1.  **Admin**: Superuser with global access. Can view and edit all data.
2.  **Manager**: Operational lead. Assigns shifts, staff, and manages specific sites.
    *   *Constraint*: Can only view/manage Sites they are assigned to.
3.  **Supervisor**: Field lead. Can manage incidents and view schedules but has limited administrative power.
4.  **Guard**: End-user. Mobile app focused. Can clock in/out, report incidents, and view own schedule.

---

## 🔒 Permission Matrix

### 🏢 Site Management
| Action | Admin | Manager | Supervisor | Guard | Notes |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Create Site** | ✅ | ✅ | ❌ | ❌ | Managers can create new sites. |
| **View Site List** | ✅ | ✅ | ✅ | ✅ | Managers see *assigned* sites only. Guards/Supervisors see *assigned* sites only. |
| **Edit Site Details** | ✅ | ✅ | ❌ | ❌ | |
| **Delete Site** | ✅ | ✅ | ❌ | ❌ | |
| **Assign Staff** | ✅ | ✅ | ❌ | ❌ | Add/Remove Managers or Guards. |
| **Manage Zones/CPs** | ✅ | ✅ | ❌ | ❌ | Create Zones & QR Codes. |
| **Print QR Codes** | ✅ | ✅ | ✅ | ❌ | Supervisors can reprint damaged tags. |

### 📅 Shift & Schedule
| Action | Admin | Manager | Supervisor | Guard | Notes |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Create Shift** | ✅ | ✅ | ✅ | ❌ | Supervisors can fill emergency slots. |
| **Delete Shift** | ✅ | ✅ | ❌ | ❌ | |
| **View Calendar** | ✅ | ✅ | ✅ | ✅ | Guards see *own* shifts only. |
| **Recurring Shifts** | ✅ | ✅ | ❌ | ❌ | Feature unavailable to Supervisors. |

### 🚨 Incident Management
| Action | Admin | Manager | Supervisor | Guard | Notes |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Report Incident** | ✅ | ✅ | ✅ | ✅ | Everyone can report issues. |
| **View Dashboard** | ✅ | ✅ | ✅ | ❌ | Real-time map & stats. |
| **Assign Responder** | ✅ | ✅ | ✅ | ❌ | Dispatching guards to incidents. |
| **Resolve/Close** | ✅ | ✅ | ✅ | ❌ | |
| **Receive Alerts** | ✅ | ✅ | ✅ | ❌ | Panic button notifications. |

### 👮 Patrol Operations
| Action | Admin | Manager | Supervisor | Guard | Notes |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Start Patrol** | ❌ | ❌ | ❌ | ✅ | Mobile App only. |
| **Scan Checkpoint** | ❌ | ❌ | ❌ | ✅ | NFC/QR Scan. |
| **View Live Map** | ✅ | ✅ | ✅ | ❌ | Real-time GPS tracking. |
| **View History** | ✅ | ✅ | ✅ | ✅ | Guards see own history. |

### 👥 User Management
| Action | Admin | Manager | Supervisor | Guard | Notes |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Create User** | ✅ | ✅ | ❌ | ❌ | |
| **Edit User** | ✅ | ✅ | ❌ | ❌ | |
| **Delete User** | ✅ | ✅ | ❌ | ❌ | |
| **Reset Password** | ✅ | ✅ | ❌ | ❌ | |

### 📊 Reports & Analytics
| Action | Admin | Manager | Supervisor | Guard | Notes |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **View DAR** | ✅ | ✅ | ❌ | ❌ | Daily Activity Reports. |
| **View Stats** | ✅ | ✅ | ❌ | ❌ | Usage KPIs. |
| **Export PDF** | ✅ | ✅ | ❌ | ❌ | |

---

## 🛡️ Data Scope Rules

| Role | Scope |
| :--- | :--- |
| **Admin** | **Global**. Sees all data across the entire system. |
| **Manager** | **Assigned Sites Only**. Can only manage data (Shifts, Incidents, Staff) linked to sites they manage. |
| **Supervisor** | **Assigned Sites Only**. Read-only access to most admin configs; Write access to operational tasks (Shifts, Incidents). |
| **Guard** | **Self Only**. Can only see their own Schedule, Patrols, and assigned Incidents. |
