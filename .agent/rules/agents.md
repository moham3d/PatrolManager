---
trigger: always_on
---

## 0. App Context (Auto-Generated)
**Tech Stack**: Node.js (v20+), Express (v5), Sequelize (v6), EJS, Passport (Dual Auth).
**Database**: SQLite (Dev) / Postgres (Prod).
**Existing Models**: `User`, `Site`, `PatrolTemplate`, `PatrolRun`, `Checkpoint`, `CheckpointVisit`, `Incident`, `Shift`.
**Existing Controllers**: `patrolController.js`, `incidentController.js`, `reportController.js`.

## 1. Context & Mission
We are migrating a legacy Python/FastAPI + React application (`api/` and `web/`) into a **New Monolithic Node.js Application** (`src/`).
The new system uses **Express.js (v5)**, **Sequelize**, and **EJS** for server-side rendering.
Crucially, it must support **Content Negotiation** to serve both the browser (HTML) and the existing Mobile App (JSON) from the same controllers.


## 2. Directory Structure (Target)
All new code goes into the `src/` directory (or root where appropriate). **DO NOT modify `api/` or `web/`.**

```
/
├── package.json
├── server.js           # Entry point
├── AGENTS.md           # This file
├── PROJECT_PLAN.md     # The Task Queue
├── src/
│   ├── config/         # Database & App Config
│   ├── models/         # Sequelize Models
│   ├── controllers/    # Business Logic (HTML & JSON)
│   ├── routes/         # Express Routes
│   ├── middleware/     # Auth, Uploads, Mobile Check
│   ├── views/          # EJS Templates
│   │   ├── layouts/    # Master layouts (header, sidebar)
│   │   ├── partials/   # Reusable components
│   │   └── sites/      # Feature-specific views
│   └── public/         # Static assets (CSS, JS, Images)
```

## 3. Agent Roles
When picking up a task, assume one of the following personas.

### 🤖 Role: `NodeArchitect` → DEPRECATED
*   **Note**: This role has been replaced by specialized agents below.

### 🏗️ Role: `Database Architect`
*   **File**: `database-architect.md`
*   **Focus**: Database schema, migrations, relationships, data integrity.
*   **Responsibilities**:
    *   Design and implement database models
    *   Write migrations for schema changes
    *   Define associations and constraints
    *   Add indexes for performance
    *   Ensure referential integrity

### ⚙️ Role: `Backend Engineer`
*   **File**: `backend-engineer.md`
*   **Focus**: Controllers, business logic, API development.
*   **Responsibilities**:
    *   Implement controller methods
    *   Add comprehensive validation
    *   Handle errors appropriately
    *   Use transactions for multi-step operations
    *   Maintain `res.format()` pattern

### 🔒️ Role: `Security Specialist`
*   **File**: `security-specialist.md`
*   **Focus**: Authentication, authorization, security hardening.
*   **Responsibilities**:
    *   Implement rate limiting
    *   Add CSRF protection
    *   Secure session configuration
    *   Configure security headers
    *   Implement input validation and sanitization

### 🎨 Role: `Frontend Developer`
*   **File**: `frontend-developer.md`
*   **Focus**: EJS views, UI components, real-time dashboards.
*   **Responsibilities**:
    *   Create/update EJS views
    *   Implement Socket.IO client integration
    *   Add charts and visualizations
    *   Ensure responsive design
    *   Implement real-time updates

### 📱 Role: `Mobile Engineer`
*   **File**: `mobile-engineer.md`
*   **Focus**: Android native development, Kotlin, Jetpack Compose.
*   **Responsibilities**:
    *   Implement QR/NFC scanning
    *   Add camera/image upload features
    *   Create supervisor workflows
    *   Implement security measures
    *   Optimize battery usage

### 🚀 Role: `DevOps Engineer`
*   **File**: `devops-engineer.md`
*   **Focus**: Cron jobs, background services, monitoring, logging.
*   **Responsibilities**:
    *   Implement cron job schedules
    *   Set up structured logging (Winston)
    *   Monitor system health
    *   Create cleanup tasks
    *   Implement automation

### 🔌 Role: `Integration Specialist`
*   **File**: `integration-specialist.md`
*   **Focus**: Socket.IO, real-time features, API integrations.
*   **Responsibilities**:
    *   Implement socket events
    *   Add room management
    *   Optimize broadcasts
    *   Handle connection management
    *   Ensure event validation

### 📱 Role: `MobileBridge` → DEPRECATED
*   **Note**: This role has been replaced by specialized Mobile Engineer.

### 🎨 Role: `ViewSpecialist` → DEPRECATED
*   **Note**: This role has been replaced by specialized Frontend Developer.

## 4. Golden Rules (Directives)

### Rule #1: The `res.format` Pattern
Every controller that fetches data MUST support both HTML and JSON.

```javascript
// ✅ CORRECT PATTERN
exports.getDashboard = async (req, res) => {
    const data = await getDashboardData(req.user);
    res.format({
        'text/html': () => res.render('dashboard', { data }),
        'application/json': () => res.json(data) // Mobile/API response
    });
};
```

### Rule #2: Legacy Reference
*   **Logic**: Read `api/app/routers/*.py` to understand the business logic (e.g., how a patrol is started).
*   **Data**: Read `api/app/models/*.py` to understand database columns and relationships.
*   **UI**: Read `web/src/components/*.jsx` to see what the user sees (forms, buttons, maps).

### Rule #3: Mobile First (Data-wise)
The mobile app is *already built* and hard to change. The API *must* bend to fit the mobile app's expected JSON format.
*   If Mobile expects `site_id` but DB has `SiteId`, map it in the Controller `res.json()` block.

### Rule #4: No React, No Python
*   Do not install `react` or `python` in the new project.
*   The output is a standard Node.js server rendering HTML.

## 5. Development Workflow
1.  **Pick a Task**: detailed in `PROJECT_PLAN.md`.
2.  **Check Legacy**: Look at the old code to see how it worked.
3.  **Implement Model**: Create/Update Sequelize model.
4.  **Implement Controller**: Write the logic with `res.format`.
5.  **Implement View**: Create the EJS file (using Tailwind/Bootstrap).
6.  **Verify**:
    *   Visit the URL in browser (HTML?).
    *   Curl the URL with `Accept: application/json` (JSON?).

## 6. Verification Commands
Use these commands to verify your work:
*   `npm start`: Run the server.
*   `curl -H "Accept: application/json" -H "Authorization: Bearer <token>" http://localhost:3000/...`: Test Mobile JSON.
