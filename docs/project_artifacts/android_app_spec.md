# Android App Specification: PatrolShield Enterprise

> [!IMPORTANT]
> **Source of Truth**: This document is the definitive technical guide for the Android Engineering Team.
> **Scope**: Native Android Application (Kotlin) only.

## 1. Technical Architecture

### Tech Stack
*   **Language**: Kotlin (JDK 17)
*   **UI Framework**: Jetpack Compose (Material 3 Design System)
*   **Dependency Injection**: Hilt (Dagger)
*   **Concurrency**: Coroutines & Flow
*   **Network**: Retrofit + OkHttp (with Interceptors for JWT & Offline Caching)
*   **Image Loading**: Coil

### Architecture Pattern
*   **MVVM (Model-View-ViewModel)** with Clean Architecture principles.
*   **Repository Pattern**: Mediates between Remote Data (API) and Local Data (Room).
*   **Offline-First Strategy**: All critical actions (Patrols, Incidents) are written to Local DB first, then synced via WorkManager.

### Local Persistence
*   **Room Database**:
    *   `UserEntity`: Session data.
    *   `PatrolEntity`: Cached patrol routes.
    *   `LogEntity`: Audit trail of actions waiting to sync.
*   **DataStore (Proto)**: User preferences and settings.

### Background Services
*   **Foreground Service (`LocationService`)**:
    *   **Crucial**: Must run with a persistent notification ("PatrolShield Active").
    *   **Responsibility**: Collect GPS coordinates every 30s (configurable) and post to local DB + API.
    *   **Resilience**: Auto-restart on boot (`BOOT_COMPLETED`) and crash.

## 2. App Manifest & Permissions

### Permissions Justification (Google Play)
| Permission | Usage | Criticality |
| :--- | :--- | :--- |
| `ACCESS_FINE_LOCATION` | Required to track guard patrols and verify checkpoint visits via Geofence. | **High** |
| `ACCESS_BACKGROUND_LOCATION` | Ensures safety/tracking even when phone is locked or app is minimized. | **High** |
| `CAMERA` | Required to scan QR Codes (Checkpoints) and capture Evidence (Incidents). | **High** |
| `POST_NOTIFICATIONS` | Required for Foreground Service notification and Critical Alerts (Instructions). | **Medium** |
| `NFC` | Required for scanning NFC Checkpoint tags. | **Medium** |
| `FOREGROUND_SERVICE` | Keeps the tracking engine alive. | **High** |

## 3. Detailed Screen Specifications

### 3.1 Login Screen
*   **UI Components**:
    *   `OutlinedTextField` (Email, Password)
    *   `Button` (Login) - Disable if fields empty.
    *   `LinearProgressIndicator` (Loading state).
*   **ViewModel State**:
    *   `uiState`: Idle | Loading | Success | Error(msg)
*   **API Interaction**:
    *   **POST** `/login`
    *   **Payload**: `{ email, password }`
    *   **Response**: `{ token, user: { id, name, role, ... } }`
*   **Logic**:
    *   On 200 OK: Save `token` to EncryptedSharedPreferences.
    *   Save `user` object to Room/DataStore.
    *   Navigate:
        *   If `role == 'guard'`: -> **Guard Dashboard**
        *   If `role == 'supervisor'`: -> **Supervisor Dashboard**

### 3.2 Guard Dashboard & Patrol Mode
*   **UI Components**:
    *   **Top Bar**: User Name, Shift Clock (Timer).
    *   **Map View**: Google Maps SDK (Lite Mode preferred for battery). Shows current site Geofences.
    *   **Floating Action Button (FAB)**: "SCAN CHECKPOINT" (Camera/NFC).
    *   **Bottom Card**: Next scheduled patrol or "Patrol in Progress" stats.
    *   **Panic Button**: Red, prominent, distinct from standard UI. Long-press action.
*   **ViewModel State**:
    *   `currentPatrol`: PatrolObject | null
    *   `gpsStatus`: connected | searching | disabled
    *   `shiftStatus`: active | ended
*   **Logic & Validation**:
    *   **Start Patrol**: Check GPS accuracy (<20m) and distance to Site (<500m). If too far, show specific error dialog.
    *   **Panic Button**:
        *   **Action**: Immediate **POST** `/incidents/panic`.
        *   **Offline Fallback**: Queue in RoomDB (`Priority: CRITICAL`). WorkManager attempts retry every 10s.
*   **API Interaction**:
    *   **GET** `/patrols/my-schedule`: Fetch assigned routes.
    *   **POST** `/patrols/start`: Begin patrol logic.

### 3.3 Checkpoint Scanner (QR / NFC)
*   **Implementation**:
    *   **QR**: CameraX + ML Kit (Barcode Scanning).
    *   **NFC**: Android NFC Adapter (Reader Mode).
*   **Logic**:
    *   On successful scan (String payload, e.g., `CP_123`), query Local DB for matching Checkpoint.
    *   **Validation**:
        *   Is this checkpoint part of the current patrol?
        *   Is the user physically near the checkpoint? (GPS Geofence check).
    *   **Success**: Play distinct "Success Beep".
    *   **Failure**: Haptic feedback (Long Vibrate) + "Invalid Checkpoint" Toast.
*   **API Interaction**:
    *   **POST** `/patrols/scan`
    *   **Payload**: `{ checkpointId, lat, lng, timestamp }`

### 3.4 Incident Reporting (Guard & Supervisor)
*   **UI Components**:
    *   `DropdownMenu` (Type: Theft, Fire, Maintenance).
    *   `OutlinedTextField` (Description).
    *   `ImageSelector` (Grid of captured thumbnails).
    *   `Button` "Submit Report".
*   **Logic**:
    *   **Evidence**: Photos must be compressed (JPEG, 80% quality) locally before upload.
    *   **Supervisor specific**: Can see a "Assign to Me" checkbox or "Priority" slider.
*   **API Interaction**:
    *   **POST** `/incidents` (Multipart/Form-Data).
    *   **Payload**: `type`, `description`, `priority`, `evidence` (File).

### 3.5 Supervisor Dashboard (Specific)
*   **UI Components**:
    *   `LazyColumn` of Active Guards (Status Indicators: Green/Red).
    *   `TabRow`: Overview | Incidents | Map.
*   **API Interaction**:
    *   **GET** `/incidents/active`: Poll every 30s.
    *   **POST** `/incidents/api/{id}/resolve`: Supervisor can resolve incidents on mobile.
*   **Offline Handling**:
    *   Supervisor features are "Read-Only" offline (show cached data).
    *   "Write" actions (Resolving incident) are queued.

## 4. Hardware Integration Spec

### 4.1 Location Engine
*   **Provider**: `FusedLocationProviderClient`.
*   **Modes**:
    *   **Patrol Mode**: High Accuracy, 10s interval, minDistance 10m.
    *   **Idle Mode**: Balanced Power, 5-minute interval.
*   **Battery Optimization**:
    *   Use `ActivityRecognition` to stop GPS updates if device is `STILL` for > 10 mins (saving battery for long shifts).

### 4.2 Data Synchronization (WorkManager)
*   **Tag**: `sync_critical_logs`.
*   **Constraints**: Created with `NetworkType.CONNECTED`.
*   **Backoff Policy**: Exponential (if server is down).
*   **Data Hierarchy**:
    1.  Panic Alerts (Immediate)
    2.  Checkpoint Scans
    3.  GPS Logs
    4.  Images (Wifi preferred, but cellular allowed for Incidents).
