# 🚀 PatrolShield Master Execution Plan

**Objective**: Finalize Web Backend/Frontend & prepare for future Mobile polish.
**Current Status**: Mobile App development is **POSTPONED**. Focus is on Web UI modernization, Real-time features, and RBAC.

---

## 🎨 Phase 0: Foundation & UI Modernization (New Priority)
Refactor the frontend to use a standard framework for better maintainability and responsiveness.

### Task 0.1: Adopt Tailwind CSS
**Files**: `package.json`, `tailwind.config.js`, `src/public/css/style.css`
- [ ] **Install**: Add Tailwind CSS to the project (`npm install -D tailwindcss`).
- [ ] **Configure**: Set up `tailwind.config.js` to scan EJS files.
- [ ] **Migrate**: Replace `src/public/css/design-system.css` utility classes with Tailwind classes in main layout files (`header.ejs`, `footer.ejs`, `sidebar.ejs`).
- [ ] **Theme**: Define the "Unified Color Palette" in Tailwind config to match Android's Material3 theme.

### Task 0.2: Accessibility & Layout Refactor
**Files**: `src/views/**/*.ejs`
- [ ] **Mobile-First**: Ensure sidebar is collapsible and grids stack correctly on mobile.
- [ ] **Forms**: Audit all inputs to ensure they have associated `<label>` tags (not just placeholders).
- [ ] **Live Updates**: Add `aria-live` regions for socket updates (e.g., incident feed).

---

## 🟢 Phase 1: Dashboard Completion & Polish
Merge original functional gaps with new UX improvements.

### Task 1.1: Admin Dashboard Refinement
**File**: `src/views/dashboard/admin.ejs`
- [ ] **Split View Map**: Implement 60/40 Split (Map / Recent Incidents).
- [ ] **Pulse Animation**: Add CSS pulsing effect to map markers for guards active in the last 60s.
- [ ] **Skeleton Loaders**: Replace empty data states with shimmering skeleton loaders for "Recent Incidents" and "Stats".
- [ ] **Missing Columns**: Implement "Guard Performance" and "System Health" widgets.

### Task 1.2: Supervisor Dashboard Refinement
**File**: `src/views/dashboard/supervisor.ejs`
- [ ] **Active Patrols**: Horizontal scroll/carousel for active patrol progress.
- [ ] **Lazy Loading**: Implement lazy loading for the "Incident Feed" to improve initial render time.
- [ ] **Quick Actions**: Add "Broadcast Message" button.

### Task 1.3: Guard Dashboard Refinement
**File**: `src/views/dashboard/guard.ejs`
- [ ] **Real-Time Shift Timer**: HH:MM:SS timer counting up.
- [ ] **Patrol Interface**: Interactive checkpoint list with status indicators.

---

## 🟡 Phase 2: Walkie-Talkie Web Integration
**Goal**: Reduce hardware costs by enabling Push-to-Talk (PTT) via Web.

### Task 2.1: PTT UI Component
**Files**: `src/views/partials/walkie_talkie.ejs`, `src/public/js/walkie_talkie.js`
- [ ] **Recorder**: Implement `MediaRecorder` API to capture audio blobs.
- [ ] **Socket**: Emit `voice_message` events to `room:site_{id}`.
- [ ] **Playback**: Auto-play incoming audio queue.
- [ ] **Visuals**: "User X is talking..." indicator and audio waveform visualization.

---

## 🟠 Phase 3: Advanced RBAC UI
**Goal**: Dynamic role management without code changes.

### Task 3.1: Role Management
**Files**: `src/views/admin/roles/`
- [ ] **UI**: Create/Edit Role form with a "Permission Matrix" (checkbox grid).
- [ ] **Backend**: Update `roleController.js` to handle `role.setPermissions()` associations.

---

## 🔵 Phase 4: Indoor Mapping (New Feature)
**Goal**: GPS-denied tracking for malls/basements.

### Task 4.1: Database Schema
**File**: `src/models/Site.js`, `src/models/Checkpoint.js`
- [ ] Migration: Add `layoutImage` (string URL) to `Site`.
- [ ] Migration: Add `layoutX` (float), `layoutY` (float) to `Checkpoint`.

### Task 4.2: Map Interface
**File**: `src/views/sites/map_editor.ejs`
- [ ] **Upload**: Image upload middleware (`sharp` optimization for resizing).
- [ ] **Leaflet**: Use `L.imageOverlay` to render floor plans.
- [ ] **Interaction**: Click-to-place logic for capturing x/y coordinates relative to image bounds.

---

## 📱 Phase 5: Mobile App Polish (Postponed)
*These tasks are queued for when Mobile Development resumes.*

- [ ] **Offline Indicator**: UI banner showing "Offline - Queued" using `ConnectionState`.
- [ ] **Tablet Layout**: Split-view (List/Map) for GuardDashboard on large screens.
- [ ] **Empty States**: Illustrated empty states for Schedule/Notifications.
- [ ] **Feedback**: Replace Toasts with Themed Snackbars.
- [ ] **Indoor Map**: Canvas overlay for floor plan rendering.