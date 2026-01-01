# Mobile App Specification & Refactor Plan

This document outlines the **Feature Parity** between the Web Backend (Node.js) and the required Android Application. Use this as a blueprint for refactoring the Android app.

## 1. Authentication & Profile
**Goal**: Secure access for Field Agents (Guards) and Supervisors.

| Feature | Endpoint (Method) | Payload / logic | Data Requirements |
|:---|:---|:---|:---|
| **Login** | `/api/auth/login` (POST) | `email`, `password` | Returns `token` (JWT), `user` object with `roleId`. |
| **Session** | `Authorization: Bearer <token>` | Header in all requests | Store JWT securely (EncryptedSharedPreferences). |
| **Logout** | Local Only | Clear Token | — |

## 2. Shift Management (Attendance)
**Goal**: Track when guards are active and verify their location.

| Feature | Endpoint (Method) | Payload | Validation Logic (Backend) |
|:---|:---|:---|:---|
| **Clock In** | `/api/shifts/clock-in` (POST) | `siteId`, `latitude`, `longitude` | Checks Geofence (500m radius of Site). Fails if no GPS. |
| **Clock Out** | `/api/shifts/clock-out` (POST) | `latitude`, `longitude` | Updates active shift `endTime`. |
| **Auto-Clock In**| *Internal* | *Triggered by Patrol Start* | If guard forgets to clock in, starting a patrol does it auto. |

## 3. Patrol Operations (Core Duty)
**Goal**: Execute defined security routes with proof of presence.

| Feature | Endpoint | Payload | Logic & validation |
|:---|:---|:---|:---|
| **My Schedule** | `/api/patrols/my-schedule` (GET) | None | Returns list of `PatrolTemplate` assigned or available. |
| **Start Patrol** | `/api/patrols/start` (POST) | `templateId` | Creates `PatrolRun`. Auto-creates Shift if needed. |
| **Scan Checkpoint**| `/api/patrols/scan` (POST) | `runId`, `checkpointId`, `location: {lat, lng}` | **1. GPS**: Must be within 500m.<br>**2. Sequence**: Must match order (if Ordered).<br>**3. Validity**: Checkpoint must belong to route. |
| **End Patrol** | `/api/patrols/end` (POST) | `runId` | Marks run as completed. |
| **Heartbeat** | `/api/patrols/heartbeat` (POST) | `lat`, `lng`, `activeRunId` | Sends live location for Control Center map. |

## 4. Incident Reporting & SOS
**Goal**: Report issues from the field.

| Feature | Endpoint | Payload | Notes |
|:---|:---|:---|:---|
| **Report Incident**| `/api/incidents` (POST) | `type`, `priority`, `description`, `siteId`, `location`, `imageBase64` | **Base64 Support** added. Backend handles file saving. |
| **Panic Button** | `/api/incidents/panic` (POST) | `location`, `patrolRunId` | Triggers socket alerts to Command Center & **Nearest Guards**. |
| **View Active** | `/api/incidents/active` (GET) | None | List of open incidents (for claiming). |
| **Claim Incident** | `/api/incidents/:id/claim` (POST)| None | Assigns incident to self. |
| **Resolve** | `/api/incidents/api/:id/resolve` (POST) | `notes`, `evidence` (Multipart/File) | **Gap**: Currently logic might rely on Multipart. Need to verify Base64 support for resolution too. |

## 5. Visitor Management (Guard View)
**Goal**: Verify expected guests at the gate.

| Feature | Endpoint | Capability |
|:---|:---|:---|
| **Today's Visitors** | `/api/visitors/today` (GET) | Lists visitors expected today (e.g., for Gate Security). |
| **Check In** | `/api/visitors/:id/check-in` (POST)* | *Endpoint exists in controller but requires implementing in Mobile.* |

## 6. Supervisor/Manager Stats
**Goal**: Dashboard for mobile supervisors.

| Feature | Endpoint | Data |
|:---|:---|:---|
| **Manager Stats** | `/api/manager/stats` (GET) | `patrolsToday`, `incidentsToday`, `complianceRate`. |
| **Live Map** | `/api/supervisor/live-patrols` (GET) | List of active guards locations (lat/lng/status). |

---

## Refactor Roadmap (Android)

### Phase 1: Networking Core (Fixed)
- [x] Switch to HTTPS (`NetworkModule.kt`).
- [x] Add Lenient JSON parsing.
- [x] Add `Accept: application/json` header.

### Phase 2: Shift Logic (High Priority)
- [ ] **Add "Clock In" Screen**: Before showing Patrols, specific UI to select Site and Clock In.
- [ ] **GPS Service**: Implement background service to send `heartbeat` every 60s.

### Phase 3: Patrols (Optimization)
- [ ] **Offline Support**: Cache `my-schedule` via Room Database so guards can patrol dead zones (requires Sync logic later).
- [ ] **UI Update**: Show "Distance to next checkpoint" using the lat/lng from the Template response.

### Phase 4: Incidents (Validation)
- [ ] **Image Compression**: Ensure Base64 strings aren't too massive (>5MB) to prevent timeouts.
- [ ] **Panic Logic**: Add "Hold to Trigger" UI to prevent accidental SOS.

### Phase 5: Visitor Bridge (New)
- [ ] **Visitor List**: Add new screen "Gate Control" to list `/api/visitors/today`.
- [ ] **Action**: Add Check-In button calling `/api/visitors/:id/check-in`.
