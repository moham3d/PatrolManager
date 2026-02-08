# 🚀 PatrolShield Enhancement Plan

This document tracks future features, architectural improvements, and major changes planned for implementation.

## 📦 Feature Requests

### 1. Site Layout & Indoor Mapping (Floor Plans)
**Status:** Planned
**Priority:** High (Requested for Indoor Patrols)

#### 📝 Description
Allow admins to upload image files (blueprints, floor plans) for specific sites. These images will serve as an alternative map layer, enabling checkpoints to be placed on the floor plan instead of just GPS coordinates. Critical for malls, basements, and multi-story buildings where GPS is unreliable.

#### ⚙️ Technical Approach
1.  **Database**:
    *   Add `layoutImage` (string, URL) to `Site` model.
    *   Add `layoutX` (float) and `layoutY` (float) to `Checkpoint` model (storing relative percentage 0-100% for responsiveness).
2.  **Backend**:
    *   Update `siteController` to handle image upload (multer).
    *   Serve image statically.
3.  **Frontend (Web)**:
    *   Use **Leaflet.js `L.imageOverlay`** to project the uploaded image as a map.
    *   Allow admins to click on the image to capture `x, y` coordinates for checkpoints.
4.  **Mobile App**:
    *   Add a toggle switch on the Patrol Screen: "GPS Map" vs "Floor Plan".
    *   Render markers on the image using the relative coordinates.

#### ✅ Benefits
*   Enables accurate tracking in GPS-denied environments.
*   Better visualization for guards in complex buildings.
*   Professional presentation for clients.

### 2. Walkie Talkie (Push-to-Talk) System
**Status:** ✅ POC Verified
**Priority:** High (Operational Efficiency)

#### 📝 Description
Enable voice communication between guards and the command center directly within the app. Replaces expensive traditional radio hardware. Uses a "Hold-to-Talk" mechanism similar to WhatsApp voice notes but with auto-playback.

#### ⚙️ Technical Approach
*   **Protocol**: WebSockets (Socket.io) for relaying Audio Blobs.
*   **Audio Format**: WebM/Ogg (Efficient compression).
*   **Channels**: Use Socket Rooms to isolate voice traffic per Site or Zone (e.g., `room:site_1`).
*   **Reference**: See `docs/POC_WALKIE_TALKIE.md` for the successful proof-of-concept.

---

### 3. Dynamic RBAC (Role-Based Access Control)
**Status:** Planned
**Priority:** Critical (Security & Admin UX)

#### 📝 Description
Move from hardcoded roles (Admin/Guard) to a dynamic permission system. Admins should be able to create custom roles (e.g., "Camera Operator") and assign granular permissions via the dashboard without code changes.

#### ⚙️ Technical Approach
1.  **Database**:
    *   Create `Permissions` table (e.g., `view_reports`, `delete_sites`).
    *   Create `Roles` table (Dynamic).
    *   Create `RolePermissions` (Many-to-Many link).
2.  **Backend**:
    *   Update `middleware/auth.js` to check `user.Role.Permissions` instead of role names.
    *   Implement caching (Redis/Memory) for permissions to avoid DB hits on every request.
3.  **Frontend**:
    *   Build "Role Management" interface with checkboxes for permissions.

---

## 🛠️ Architecture Improvements

*(Add future architectural changes here)*

---

## 🎨 UX/UI Enhancements

*(Add future design changes here)*
