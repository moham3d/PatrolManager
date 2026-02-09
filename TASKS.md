# 📋 Implementation Tasks (2026-02-08)

Plan approved by user. Executing in background.

## 🛡️ Phase 1: Dynamic RBAC (Backend)
- [ ] **Migration**: Create `Permissions` and `RolePermissions` tables.
- [ ] **Model**: Create `Permission.js` and update `Role.js` associations.
- [ ] **Seeding**: Populate default permissions (e.g., `MANAGE_SITES`, `VIEW_REPORTS`).
- [ ] **Logic**: Update `auth.js` middleware to check permissions dynamically.

## 🎙️ Phase 2: Walkie Talkie Integration (Backend)
- [ ] **Socket**: Update `src/sockets/socketHandler.js` to handle `voice_message` events.
- [ ] **Rooms**: Ensure voice traffic is isolated by `siteId` (Security).

## 🖥️ Phase 3: Admin UI (Basic)
- [ ] **Routes**: Add `/admin/roles` routes.
- [ ] **Controller**: Create `roleController.js`.
- [ ] **Views**: Create basic EJS views for Role Management.
