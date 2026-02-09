# 📋 Phase 2: Core Views - Task List

**Phase**: 2 - Core Views
**Status**: 🟡 READY TO START
**Priority**: HIGH
**Estimated Duration**: 1 week

---

## 🎯 Overview

Phase 2 focuses on creating the three core dashboard views:
1. **Admin Dashboard** - High-level overview for administrators
2. **Supervisor Dashboard** - Live monitoring for field supervisors
3. **Guard Dashboard** - Patrol execution for security guards

Each dashboard will leverage the design system and components created in Phase 1.

---

## 📊 Task Breakdown

### Task 2.1: Admin Dashboard
**File**: `src/views/dashboard/admin/index.ejs`
**Priority**: HIGH
**Estimated Time**: 4 hours

#### Subtasks:
- [ ] **Top Section**: Key Metrics (4 cards)
  - [ ] Total Sites card with trend
  - [ ] Active Guards card with on-duty indicator
  - [ ] Total Incidents card with critical count
  - [ ] Patrol Completion Rate card with percentage

- [ ] **Middle Section**: Split View (2 columns)
  - [ ] Left Column (60%): Live Operations Map
    - [ ] Integrate Leaflet.js
    - [ ] Display all sites as markers
    - [ ] Display active guards with live markers
    - [ ] Display active incidents with red markers
    - [ ] Add zone overlays for sites
  - [ ] Right Column (40%): Recent Incidents (top 5)
    - [ ] Status badge (Open/In Progress/Resolved)
    - [ ] Priority indicator (High/Medium/Low)
    - [ ] Time ago (relative)
    - [ ] Site name
    - [ ] View all link

- [ ] **Bottom Section**: 3-column layout
  - [ ] Column 1 (33%): Guard Performance
    - [ ] Patrols completed today
    - [ ] Average completion time
    - [ ] Top 5 performers list
  - [ ] Column 2 (33%): Site Status
    - [ ] Sites with active guards
    - [ ] Sites with incidents
    - [ ] Sites needing attention
    - [ ] View all sites link
  - [ ] Column 3 (33%): System Health
    - [ ] Server uptime
    - [ ] Database status
    - [ ] Last backup time
    - [ ] Active sessions

- [ ] **Route Setup**
  - [ ] Update route to use new view
  - [ ] Add `ensurePermission('VIEW_REPORTS')`
  - [ ] Test on different screen sizes

#### Acceptance Criteria:
- [ ] All 4 stat cards display correctly with data
- [ ] Leaflet map loads and displays markers
- [ ] Recent incidents list shows 5 items with proper badges
- [ ] Guard performance, site status, and system health cards render
- [ ] Responsive: 3 columns on desktop, 1 on mobile
- [ ] Dark/light mode toggle works on all components

---

### Task 2.2: Supervisor Dashboard
**File**: `src/views/dashboard/supervisor/index.ejs`
**Priority**: HIGH
**Estimated Time**: 5 hours

#### Subtasks:
- [ ] **Top Section**: Shift Overview (2 cards)
  - [ ] On-Duty Guards card with count
  - [ ] Current Shifts card with time remaining

- [ ] **Middle Section**: Live Monitoring (full width)
  - [ ] Row 1: Guard Grid (2x3 grid)
    - [ ] Guard cards showing:
      - [ ] Guard name & photo/avatar
      - [ ] Current status (Patrolling/Idle/In Emergency)
      - [ ] Last checkpoint scanned
      - [ ] Battery level
      - [ ] Location distance from site
    - [ ] Real-time updates via Socket.IO
  - [ ] Row 2: Active Patrols
    - [ ] Patrol cards (horizontal scroll)
    - [ ] Each patrol showing:
      - [ ] Patrol route name
      - [ ] Progress bar (% complete)
      - [ ] Started time
      - [ ] Estimated completion
      - [ ] Guard assigned
    - [ ] Real-time updates via Socket.IO
  - [ ] Row 3: Incident Feed
    - [ ] Incident list with status
    - [ ] Filter: All/Open/In Progress/Resolved
    - [ ] Sort: Newest/By Priority
    - [ ] Each incident showing:
      - [ ] Title
      - [ ] Priority badge
      - [ ] Status badge
      - [ ] Time ago
      - [ ] Assign/Escalate buttons
    - [ ] Real-time updates via Socket.IO

- [ ] **Bottom Section**: Quick Actions
  - [ ] Create Ad-hoc Patrol button
  - [ ] Broadcast Message button
  - [ ] View All Guards link
  - [ ] View All Incidents link

- [ ] **Route Setup**
  - [ ] Update route to use new view
  - [ ] Add `ensurePermission('PATROL_VIEW')` and `ensurePermission('INCIDENT_VIEW')`
  - [ ] Test on different screen sizes

#### Acceptance Criteria:
- [ ] Shift overview cards display correctly
- [ ] Guard grid shows 6 guards with real-time status
- [ ] Active patrols carousel works and scrolls horizontally
- [ ] Incident feed filters and sorts correctly
- [ ] Real-time updates work via Socket.IO
- [ ] Quick action buttons navigate to correct pages
- [ ] Responsive: Grid adjusts on tablet/mobile

---

### Task 2.3: Guard Dashboard
**File**: `src/views/dashboard/guard/index.ejs`
**Priority**: HIGH
**Estimated Time**: 4 hours

#### Subtasks:
- [ ] **Top Section**: Shift Status (full width card)
  - [ ] Shift Timer display (HH:MM:SS)
  - [ ] Started time
  - [ ] Scheduled end time
  - [ ] Current site
  - [ ] Clock out button (prominent)
  - [ ] Real-time timer updates

- [ ] **Middle Section**: Active Patrol (if on patrol)
  - [ ] Patrol Progress:
    - [ ] Route name
    - [ ] Progress bar with percentage
    - [ ] Next checkpoint highlighted
    - [ ] Distance to next (meters)
    - [ ] Estimated time (minutes)
  - [ ] Checkpoint List:
    - [ ] Checkpoints (vertical list)
    - [ ] Status indicators:
      - [ ] ✅ Completed (green checkmark)
      - [ ] 🔵 Next (blue circle, highlighted)
      - [ ] ⚪ Pending (gray circle)
    - [ ] Scan button for next checkpoint

- [ ] **Bottom Section**: Quick Actions
  - [ ] Report Incident button (prominent, red)
  - [ ] SOS Button (prominent, pulsing red)
  - [ ] View History link
  - [ ] View Shifts link

- [ ] **Route Setup**
  - [ ] Update route to use new view
  - [ ] Add `ensurePermission('PATROL_VIEW')` and `ensurePermission('INCIDENT_MANAGE')`
  - [ ] Test on mobile (guards use phones)

#### Acceptance Criteria:
- [ ] Shift timer counts up correctly in real-time
- [ ] Clock out button works and ends shift
- [ ] Patrol progress bar updates as checkpoints are scanned
- [ ] Checkpoint list shows correct status for each checkpoint
- [ ] Report Incident button navigates to incident creation
- [ ] SOS button triggers emergency alert (test with mock)
- [ ] Mobile-responsive (one-handed operation)

---

### Task 2.4: Route Configuration
**File**: `src/routes/dashboard.js`
**Priority**: MEDIUM
**Estimated Time**: 2 hours

#### Subtasks:
- [ ] Create or update `dashboard.js` route file
- [ ] Add route: `GET /dashboard` (Admin dashboard)
  - [ ] Check user role
  - [ ] Redirect to appropriate dashboard
  - [ ] Admin: `/dashboard/admin`
  - [ ] Supervisor: `/dashboard/supervisor`
  - [ ] Guard: `/dashboard/guard`

- [ ] Add route: `GET /dashboard/admin` (Admin dashboard)
  - [ ] Fetch all data needed (sites, guards, incidents, stats)
  - [ ] Pass data to view
  - [ ] Apply `ensurePermission('VIEW_REPORTS')`

- [ ] Add route: `GET /dashboard/supervisor` (Supervisor dashboard)
  - [ ] Fetch all data needed (guards, patrols, incidents, shifts)
  - [ ] Pass data to view
  - [ ] Apply `ensurePermission('PATROL_VIEW')` and `ensurePermission('INCIDENT_VIEW')`

- [ ] Add route: `GET /dashboard/guard` (Guard dashboard)
  - [ ] Fetch all data needed (active shift, active patrol, checkpoints)
  - [ ] Pass data to view
  - [ ] Apply `ensurePermission('PATROL_VIEW')` and `ensurePermission('INCIDENT_MANAGE')`

#### Acceptance Criteria:
- [ ] `/dashboard` redirects based on user role
- [ ] Each dashboard route loads correct data
- [ ] All routes are properly protected with permissions
- [ ] Error handling for missing/invalid data

---

### Task 2.5: Socket.IO Integration
**Files**: `src/sockets/dashboardSocket.js`, `src/public/js/dashboard.js`
**Priority**: MEDIUM
**Estimated Time**: 3 hours

#### Subtasks:
- [ ] **Server-Side** (`dashboardSocket.js`)
  - [ ] Create dashboard namespace: `/dashboard`
  - [ ] Events:
    - [ ] `guard:status:update` - Guard status changes
    - [ ] `patrol:progress:update` - Patrol progress updates
    - [ ] `incident:new` - New incident reported
    - [ ] `incident:status:update` - Incident status changes
  - [ ] Join rooms based on user role and site assignments
  - [ ] Broadcast to appropriate users

- [ ] **Client-Side** (`dashboard.js`)
  - [ ] Connect to `/dashboard` namespace
  - [ ] Listen for events:
    - [ ] `guard:status:update` - Update guard cards
    - [ ] `patrol:progress:update` - Update patrol progress bars
    - [ ] `incident:new` - Add to incident feed
    - [ ] `incident:status:update` - Update incident status badges
  - [ ] Handle connection errors
  - [ ] Auto-reconnect logic
  - [ ] Clean up on page unload

- [ ] **Integrate into Views**
  - [ ] Include `dashboard.js` in supervisor dashboard
  - [ ] Include `dashboard.js` in guard dashboard
  - [ ] Include `dashboard.js` in admin dashboard (map updates)

#### Acceptance Criteria:
- [ ] Socket connection establishes successfully
- [ ] Guard status updates appear in real-time
- [ ] Patrol progress bars update in real-time
- [ ] New incidents appear in feed immediately
- [ ] Incident status updates show correct badges
- [ ] Connection errors are handled gracefully
- [ ] Auto-reconnect works after disconnect

---

### Task 2.6: Testing & Polish
**Priority**: HIGH
**Estimated Time**: 3 hours

#### Subtasks:
- [ ] **Functional Testing**
  - [ ] Test Admin Dashboard:
    - [ ] Stat cards display data
    - [ ] Leaflet map loads and displays markers
    - [ ] Recent incidents list shows correctly
    - [ ] Guard performance, site status, system health render
  - [ ] Test Supervisor Dashboard:
    - [ ] Guard grid shows all on-duty guards
    - [ ] Active patrols carousel works
    - [ ] Incident feed filters and sorts
    - [ ] Real-time updates work
  - [ ] Test Guard Dashboard:
    - [ ] Shift timer updates
    - [ ] Patrol progress bar updates
    - [ ] Checkpoint scanning works
    - [ ] Report incident button works

- [ ] **Responsive Testing**
  - [ ] Test on mobile (320px, 375px, 414px)
  - [ ] Test on tablet (768px, 1024px)
  - [ ] Test on desktop (1280px, 1920px)
  - [ ] Verify sidebar works on all sizes
  - [ ] Verify tables/cards stack correctly

- [ ] **Browser Testing**
  - [ ] Chrome (latest)
  - [ ] Firefox (latest)
  - [ ] Safari (latest)
  - [ ] Edge (latest)

- [ ] **Accessibility Testing**
  - [ ] Keyboard navigation works
  - [ ] Focus states visible
  - [ ] Screen reader compatible (basic)
  - [ ] Color contrast meets WCAG AA

- [ ] **Performance Testing**
  - [ ] Page load time < 2 seconds
  - [ ] No console errors
  - [ ] Socket connection < 500ms
  - [ ] Leaflet map loads < 1 second

#### Acceptance Criteria:
- [ ] All dashboards work correctly on all devices
- [ ] No console errors in all browsers
- [ ] Real-time updates work smoothly
- [ ] Accessibility features work
- [ ] Performance targets met

---

## 🎨 Design Guidelines

### Admin Dashboard
- **Tone**: Authoritative, data-driven
- **Layout**: Metrics → Map → Details (top-down)
- **Colors**: Navy blue + electric blue accents
- **Focus**: High-level overview, strategic decisions

### Supervisor Dashboard
- **Tone**: Operational, real-time
- **Layout**: Overview → Live Feed (horizontal)
- **Colors**: Navy blue + orange accents (for actions)
- **Focus**: Live monitoring, quick decisions

### Guard Dashboard
- **Tone**: Task-focused, simple
- **Layout**: Status → Task (vertical stack)
- **Colors**: Navy blue + green (success) + red (alerts)
- **Focus**: Current task, easy completion

---

## 📊 Success Metrics

| Metric | Target | Current |
|--------|---------|---------|
| **Admin Dashboard Complete** | 100% | 0% |
| **Supervisor Dashboard Complete** | 100% | 0% |
| **Guard Dashboard Complete** | 100% | 0% |
| **Socket.IO Integration** | 100% | 0% |
| **Testing Complete** | 100% | 0% |
| **Browser Compatibility** | 4/4 | 0/4 |
| **Responsive** | 100% | 0% |
| **Accessibility** | WCAG AA | N/A |

---

## 🚀 Getting Started

1. **Review Phase 1 Foundation**
   - Read `docs/PHASE_1_COMPLETION.md`
   - Familiarize with design system
   - Understand available components

2. **Set Up Development Environment**
   - Run `pm2 restart patrolmanager`
   - Open `http://localhost:3000/login`
   - Login as different roles to test dashboards

3. **Start with Task 2.1**
   - Create `src/views/dashboard/admin/index.ejs`
   - Build the layout structure
   - Integrate components from Phase 1

4. **Work Through Tasks**
   - Complete tasks in order: 2.1 → 2.2 → 2.3 → 2.4 → 2.5 → 2.6
   - Test each task before moving to next
   - Update task list as you complete items

---

## 📝 Notes

- **Socket.IO**: Already set up in `src/sockets/socketHandler.js`
- **Leaflet.js**: Need to integrate for admin dashboard map
- **Real-time**: Already using Socket.IO in existing code
- **Data Fetching**: Use Sequelize queries in routes
- **Role-based Dashboards**: Use `ensurePermission` middleware

---

## 🎯 Next Phase

After Phase 2:
- **Phase 3**: Management Pages (Sites, Users, Roles)
- **Phase 4**: Operations Pages (Patrols, Incidents)
- **Phase 5**: Polish & Testing

---

**Ready to begin Phase 2?**
Start with Task 2.1: Admin Dashboard!

---

**Last Updated**: 2026-02-08
**Status**: 🟡 READY TO START
