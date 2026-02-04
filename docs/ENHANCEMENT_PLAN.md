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

---

## 🛠️ Architecture Improvements

*(Add future architectural changes here)*

---

## 🎨 UX/UI Enhancements

*(Add future design changes here)*
