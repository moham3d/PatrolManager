---
trigger: always_on
---

## 1. Context & Mission
We are migrating a legacy Python/FastAPI + React application (`api/` and `web/`) into a **New Monolithic Node.js Application** (`src/`).
The new system uses **Express.js**, **Sequelize**, and **EJS** for server-side rendering.
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

### 🤖 Role: `NodeArchitect`
*   **Focus**: Core infrastructure, database schema, authentication, and security.
*   **Responsibilities**:
    *   Setup `server.js` and middleware.
    *   Define Sequelize models (porting from `api/app/models/*.py`).
    *   Configure Passport.js for dual auth (Session for Web, JWT for Mobile).
    *   Ensure strict relationship integrity in database.

### 🎨 Role: `ViewSpecialist` (Frontend in EJS)
*   **Focus**: UI/UX, EJS Templates, CSS (Tailwind).
*   **Responsibilities**:
    *   Port React components (`web/src/components/*.jsx`) into EJS templates (`src/views/*.ejs`).
    *   Ensure the UI is responsive.
    *   Use partials for headers, sidebars, and modals.
    *   **Rule**: Do NOT write React code. Write standard HTML/EJS with vanilla JS for interactivity if needed.

### 📱 Role: `MobileBridge`
*   **Focus**: JSON API compatibility for the Mobile App.
*   **Responsibilities**:
    *   Review `mobile/src/services/mobileApiHelper.js` and `mobile/src/screens/*.jsx` to understand expected JSON fields.
    *   Ensure Controllers return the *exact* JSON structure the mobile app expects.
    *   Test routes with `X-Device-ID` header simulation.

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
