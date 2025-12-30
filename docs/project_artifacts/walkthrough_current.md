# Walkthrough - Phase 5: Workforce & Scheduling

## 🚀 Overview
We have successfully implemented **Module D: Workforce & Scheduling**. This module enables managers to schedule shifts and allows guards to clock in and out using the mobile app, with GPS verification.

## 🛠️ Key Components Implemented

### 1. Database Models
- **Shift**: Stores schedule data (User, Site, Start/End Time, Status).
- **Attendance**: Stores operational data (Clock In/Out events, GPS coordinates, Timestamp).

### 2. Web Interface
- **Shift Calendar**: A new dashboard page (`/shifts`) to view all scheduled shifts.
- **Scheduler**: A modal interface to assign a guard to a site for a specific date and time.

### 3. Mobile API
- **Clock In (`POST /api/shifts/clock-in`)**:
    - Validates that the user has a scheduled shift at the current time.
    - **Geofencing**: Verifies the user is within 500m of the site's GPS location.
- **Clock Out (`POST /api/shifts/clock-out`)**:
    - Records the end of the shift and updates the shift status to 'completed'.

## 📸 Screenshots
*(To be added during visual verification)*

## ✅ Verification
1.  **Web**: Visit `http://localhost:3000/shifts` to see the calendar.
2.  **API**: Use the Postman collection or `curl` to test the clock-in endpoint:
    ```bash
    curl -X POST http://localhost:3000/api/shifts/clock-in \
      -H "Authorization: Bearer <TOKEN>" \
      -H "Content-Type: application/json" \
      -d '{"lat": 51.505, "lng": -0.09}'
    ```
