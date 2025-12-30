# Implementation Plan - PatrolShield Enterprise (Node.js Migration)

**Goal**: Build a new monolithic Node.js application (Express.js + EJS + Sequelize) that replaces the legacy Python/React stack while maintaining full API compatibility for the existing Mobile App.

## User Review Required
> [!IMPORTANT]
> **Greenfield Project**: This is a fresh install. We will initialize `package.json` and the directory structure from scratch.
> **Content Negotiation Strategy**: We will use a strict `res.format()` pattern in controllers to serve both HTML (Browser) and JSON (Mobile App) from the same logic.
> **Database**: We assume a fresh PostgreSQL database setup. Connection details will be needed in `.env`.

## Proposed Changes

### Phase 1: Foundation & Architecture
Establish the core Node.js structure and authentication system.
#### [NEW] [package.json](file:///home/mohamed/Desktop/Projects/PatrolManager/package.json)
- Init with `express`, `ejs`, `sequelize`, `pg`, `pg-hstore`, `passport`, `passport-local`, `passport-jwt`, `jsonwebtoken`, `bcryptjs`, `dotenv`, `morgan`, `cors`, `multer`, `socket.io`.
- Dev dependencies: `nodemon`.

#### [NEW] [server.js](file:///home/mohamed/Desktop/Projects/PatrolManager/server.js)
- Entry point. Configures Express, EJS view engine, static assets, and global middleware.

#### [NEW] [src/config](file:///home/mohamed/Desktop/Projects/PatrolManager/src/config)
- `database.js`: Sequelize connection logic.
- `passport.js`: Auth strategies (Local for Web, JWT for Mobile).

#### [NEW] [src/models](file:///home/mohamed/Desktop/Projects/PatrolManager/src/models)
- `index.js`: Model loader and association definitions.
- `User.js`, `Role.js`, `Permission.js`: Auth schema.

#### [NEW] [src/middleware](file:///home/mohamed/Desktop/Projects/PatrolManager/src/middleware)
- `auth.js`: Middleware to protect routes and handle dual-auth (Session vs JWT).
- `mobileHelper.js`: Middleware to parse mobile-specific headers (`X-Device-ID`).

### Phase 2: Site & Asset Management (Module A)
The core data structure "Where security happens".
#### [NEW] [src/models](file:///home/mohamed/Desktop/Projects/PatrolManager/src/models)
- `Site.js`: Location data (Client Name, Address, Geofence Polygon).
- `Zone.js`: Areas within sites (Lobby, Parking).
- `Checkpoint.js`: NFC/QR/GPS points (Label, Type, Lat/Lng).

#### [NEW] [src/controllers/siteController.js](file:///home/mohamed/Desktop/Projects/PatrolManager/src/controllers/siteController.js)
- CRUD operations.
- `getSites`: Renders table for Web, returns JSON array for Mobile.

#### [NEW] [src/views/sites](file:///home/mohamed/Desktop/Projects/PatrolManager/src/views/sites)
- `index.ejs`: List of sites.
- `form.ejs`: Create/Edit Site (Create Checkpoints).
- `details.ejs`: View Site details.

### Phase 3: Patrol Operations (Module B)
The "Engine" of the system.
#### [NEW] [src/models](file:///home/mohamed/Desktop/Projects/PatrolManager/src/models)
- `PatrolTemplate.js`: Defined routes (Ordered, Random).
- `PatrolRun.js`: Active/Completed instances of a patrol.
- `CheckpointVisit.js`: Record of a guard scanning a tag.

#### [NEW] [src/controllers/patrolController.js](file:///home/mohamed/Desktop/Projects/PatrolManager/src/controllers/patrolController.js)
- `startPatrol`: API for Mobile to begin.
- `scanCheckpoint`: API for Mobile to record a hit (validates GPS/Time).
- `manager`: Web view to create templates.

### Phase 4: Incident & Real-Time (Module C & Dashboard)
#### [NEW] [src/sockets](file:///home/mohamed/Desktop/Projects/PatrolManager/src/sockets)
- `socketHandler.js`: Handle real-time events (Panic, Scan, Location Update).

#### [NEW] [src/models](file:///home/mohamed/Desktop/Projects/PatrolManager/src/models)
- `Incident.js`: Reports (Type, Evidence, Status).
- `PanicAlert.js`: Emergency logs.

#### [NEW] [src/views/dashboard](file:///home/mohamed/Desktop/Projects/PatrolManager/src/views/dashboard)
#### [NEW] [src/views/dashboard](file:///home/mohamed/Desktop/Projects/PatrolManager/src/views/dashboard)
- `live.ejs`: Real-time map view using Leaflet/Google Maps.

### Phase 5: Workforce & Scheduling (Module D)
Managing the "Who" and "When".
#### [NEW] [src/models](file:///home/mohamed/Desktop/Projects/PatrolManager/src/models)
- `Shift.js`: Schedule definition (User, Site, StartTime, EndTime).
- `Attendance.js`: Clock-in/out records with GPS verification.

#### [NEW] [src/controllers/shiftController.js](file:///home/mohamed/Desktop/Projects/PatrolManager/src/controllers/shiftController.js)
- `createShift`: Admin assigns user to site.
- `clockIn`: Mobile API. Checks if User is inside Site.geofence.
- `clockOut`: Mobile API.

#### [NEW] [src/views/shifts](file:///home/mohamed/Desktop/Projects/PatrolManager/src/views/shifts)
- `calendar.ejs`: Visual calendar for scheduling.

### Phase 6: Analytics & Reporting (Module E)
#### [NEW] [src/libs](file:///home/mohamed/Desktop/Projects/PatrolManager/src/libs)
- `reportGenerator.js`: PDF generation for Daily Activity Reports (DAR).

#### [NEW] [src/controllers/reportController.js](file:///home/mohamed/Desktop/Projects/PatrolManager/src/controllers/reportController.js)
- `generateDailyReport`: Aggregates runs, incidents, and attendance for a site.
- `getAnalytics`: Returns stats (On-time %, Patrol Completion).

#### [NEW] [src/views/reports](file:///home/mohamed/Desktop/Projects/PatrolManager/src/views/reports)
#### [NEW] [src/views/reports](file:///home/mohamed/Desktop/Projects/PatrolManager/src/views/reports)
- `index.ejs`: Dashboard for downloading reports.

### Phase 7: Gap Closure & Polish
Addressing workflow gaps identified in Product Review.
#### [FIX] [src/cron/attendanceMonitor.js](file:///home/mohamed/Desktop/Projects/PatrolManager/src/cron/attendanceMonitor.js)
- Refactor to use `Shift` model instead of non-existent `Schedule`.
- Check `Attendance` table for presence.

#### [MODIFY] [src/controllers/visitorController.js](file:///home/mohamed/Desktop/Projects/PatrolManager/src/controllers/visitorController.js)
- `getTodayVisitors`: New API endpoint for Mobile Guard App.

#### [MODIFY] [src/models/Incident.js](file:///home/mohamed/Desktop/Projects/PatrolManager/src/models/Incident.js)
- Add `assignedTo` (UserId) field.

#### [MODIFY] [src/controllers/incidentController.js](file:///home/mohamed/Desktop/Projects/PatrolManager/src/controllers/incidentController.js)
- Add logic to assign incidents and filter by assignee.

## Verification Plan

### Automated Tests
- **API Testing**: Use `curl` or Postman scripts to simulate Mobile App requests.
    - `POST /login`: Verify JWT return.
    - `GET /sites`: Verify JSON list of sites.
    - `POST /patrols/scan`: Verify record creation.
- **Unit Tests**: (Optional for Ph 1) Jest tests for Model validation logic.

### Manual Verification
1.  **Web Walkthrough**:
    - Start server `npm run dev`.
    - Open `localhost:3000`.
    - Log in as Admin.
    - Create a Site -> Create Zones -> Create Checkpoints.
    - Verify data persists in DB.
2.  **Mobile Simulation**:
### Phase 8: Security Hardening & Documentation (Completed)
- [x] RBAC Implementation (`ensureRole`)
- [x] User Manuals (`/docs`)
- [x] Shift Assignment UI

## Phase 9: Shift Improvements (Recurring Shifts)
- **Goal**: Allow managers to schedule repeating shifts (Daily/Weekly) in one go.
- **Changes**:
    - `src/views/sites/details.ejs`: Add Repeat (Frequency, End Date) fields to modal.
    - `src/controllers/shiftController.js`: Update `create` to handle bulk insertion.

## Phase 10: Site-Centric Dashboard Refactor
- **Goal**: Consolidate Incidents, Patrols, Shifts, and Staff into a single Tabbed View for the Site.
- **Changes**:
    - `src/controllers/siteController.js`: Include `PatrolRun` and `Incident` models in the `show` query.
    - `src/views/sites/details.ejs`: Refactor into tabs: [Overview, Schedule, Staff, Patrols, Incidents].

#### [MODIFY] [src/middleware/auth.js](file:///home/mohamed/Desktop/Projects/PatrolManager/src/middleware/auth.js)
- Add `ensureRole(roles)` middleware.

#### [MODIFY] [src/routes](file:///home/mohamed/Desktop/Projects/PatrolManager/src/routes)
- Protect `create`, `assign`, `delete` routes with `ensureRole('manager')`.

#### [MODIFY] [src/controllers/shiftController.js](file:///home/mohamed/Desktop/Projects/PatrolManager/src/controllers/shiftController.js)
- Implement `create` method for Manager Shift Assignment.

#### [NEW] [src/views/docs](file:///home/mohamed/Desktop/Projects/PatrolManager/src/views/docs)
- `manual.ejs`: Comprehensive HTML user manual.

## Phase 12: UX/UI Polish - Error Handling
- **Goal**: Replace raw JSON/HTML error responses with standard UI Toasts/Flash Messages.
- **Changes**:
    - `npm install connect-flash express-session`: Setup flash messages.
    - `src/views/partials/flash.ejs`: Create a Toast component (Notyf/Tailwind).
    - `server.js`: Configure flash middleware and global locals (`res.locals.success`, `res.locals.error`).
    - `src/controllers/shiftController.js`: Update `clockIn`/`clockOut` to use flash + redirect logic for HTML requests.
    - `src/middleware/auth.js`: Update `ensureRole` to flash "Access Denied" and redirect to previous page or dashboard.
    - `src/views/dashboard/guard.ejs`: Update forms to standard submission (or handle fetch redirect transparently), ensuring Toasts appear on reload.

## Phase 13: Activate SOS Button
- **Goal**: Make the visual SOS button functional.
- **Changes**:
    - `src/views/dashboard/guard.ejs`: Add JS click listener to `#panicBtn`.
    - Logic: Get GPS -> POST `/incidents/panic` -> Show Success/Error Toast.

## Phase 14: Incident Module Refactor
- **Goal**: Modernize the Incident management workflow.
- **Changes**:
    - **Self-Assignment**: Allow guards to "Claim" unassigned incidents.
    - **Resolution Workflow**: Add a dedicated Modal for resolving incidents with **Notes** and **Evidence Photo** (optional).
    - **UI Overhaul**:
        - Convert `index.ejs` from a simple table to a **Card Grid** or **Kanban Board**.
        - Add **Filters** (Status, Priority, Site).
        - Add **Search Bar**.
    - **Backend**: Update `incidentController.js` to handle `claim` action and evidence upload on resolution.

## Phase 15: Incident Enrichment & Details
- **Goal**: Add granular location (Zones) and a dedicated details page.
- **Changes**:
    - **Database**: Add `zoneId` to `Incident` model.
    - **Report UI**: Add **Zone** dropdown to `create.ejs` (dependent on Site).
    - **Details UI**: Create `show.ejs` for full incident history, map location, and evidence.
    - **Navigation**: Link incident title in `index.ejs` to `show.ejs`.

## Phase 16: Admin Dashboard & Live Map
- **Goal**: Differentiate Admin Dashboard and add Real-time Map.
- **Changes**:
    - **Admin Dashboard**: Embed "Live Map" widget (Leaflet + Socket.IO) directly into `admin.ejs`.
    - **Map Component**: Extract map logic to `partials/live_map.ejs` for reuse.
    - **Controller**: Ensure `dashboard/admin` route passes necessary initial data for map (active incidents).

## Phase 17: Live User Tracking
- **Goal**: Visualize online users (Guards) on the Live Map.
- **Changes**:
    - **Backend (`socketHandler.js`)**: Implement in-memory tracking of connected users with location data. Handle `update_location` event.
    - **Frontend (`live_map.ejs`)**: Add User markers (Blue/Green dots) distinguishing them from Incidents.
    - **Frontend (`guard.ejs` / `footer.ejs`)**: Add `navigator.geolocation` logic to emit `update_location` for authenticated users.
