const express = require('express');
const router = express.Router();
const { ensureAuth, ensurePermission } = require('../middleware/auth');
const adminRoleController = require('../controllers/adminRoleController');

// ==================== WEB VIEWS (NEW DESIGN) ====================

// List all roles (Web - New Design)
router.get('/admin/roles',
    ensureAuth,
    ensurePermission('ROLE_MANAGE'),
    adminRoleController.listRolesPage
);

// Create role page (Web - New Design)
router.get('/admin/roles/create',
    ensureAuth,
    ensurePermission('ROLE_MANAGE'),
    adminRoleController.createRolePage
);

// Create role action (Web POST)
router.post('/admin/roles/create',
    ensureAuth,
    ensurePermission('ROLE_MANAGE'),
    adminRoleController.createRoleWeb
);

// Edit role page (Web - New Design)
router.get('/admin/roles/:id/edit',
    ensureAuth,
    ensurePermission('ROLE_MANAGE'),
    adminRoleController.editRolePage
);

// Edit role action (Web POST)
router.post('/admin/roles/:id/edit',
    ensureAuth,
    ensurePermission('ROLE_MANAGE'),
    adminRoleController.updateRoleWeb
);

// Delete role (Web POST)
router.post('/admin/roles/:id/delete',
    ensureAuth,
    ensurePermission('ROLE_MANAGE'),
    adminRoleController.deleteRoleWeb
);

module.exports = router;
