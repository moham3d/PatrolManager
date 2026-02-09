const express = require('express');
const router = express.Router();
const { roleController, permissionController } = require('../controllers/rbacController');
const { ensureAuth, ensurePermission } = require('../middleware/auth');

// ===== PERMISSIONS =====

// List all permissions
router.get('/permissions/list', ensureAuth, permissionController.listPermissions);

// Get single permission
router.get('/permissions/:id', ensureAuth, permissionController.getPermission);

// Create permission (Admin only)
router.post('/permissions',
    ensureAuth,
    ensurePermission('ROLE_MANAGE'),
    permissionController.createPermission
);

// Update permission (Admin only)
router.put('/permissions/:id',
    ensureAuth,
    ensurePermission('ROLE_MANAGE'),
    permissionController.updatePermission
);

// Delete permission (Admin only)
router.delete('/permissions/:id',
    ensureAuth,
    ensurePermission('ROLE_MANAGE'),
    permissionController.deletePermission
);

// ===== ROLES =====

// List all roles
router.get('/roles/list', ensureAuth, roleController.listRoles);

// Get single role with permissions
router.get('/roles/:id', ensureAuth, roleController.getRole);

// Create role (Admin only)
router.post('/roles',
    ensureAuth,
    ensurePermission('ROLE_MANAGE'),
    roleController.createRole
);

// Update role (Admin only)
router.put('/roles/:id',
    ensureAuth,
    ensurePermission('ROLE_MANAGE'),
    roleController.updateRole
);

// Delete role (Admin only)
router.delete('/roles/:id',
    ensureAuth,
    ensurePermission('ROLE_MANAGE'),
    roleController.deleteRole
);

// List users in role (Admin only)
router.get('/roles/:id/users',
    ensureAuth,
    ensurePermission('ROLE_MANAGE'),
    roleController.listRoleUsers
);

module.exports = router;
