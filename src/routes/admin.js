const express = require('express');
const router = express.Router();
const { ensureAuth, ensurePermission } = require('../middleware/auth');
const adminRoleController = require('../controllers/adminRoleController');

// ==================== WEB VIEWS (NEW DESIGN) ====================

// List all roles (Web - New Design)
router.get('/admin/roles',
    ensureAuth,
    ensurePermission('ROLE_MANAGE'),
    async (req, res) => {
        try {
            const { Role } = require('../models');

            const roles = await Role.findAll({
                include: [
                    { model: require('../models').Permission },
                    { model: require('../models').User, attributes: ['id'] }
                ],
                order: [['name', 'ASC']]
            });

            // Count users per role
            const rolesWithCounts = roles.map(role => {
                return {
                    ...role.toJSON(),
                    userCount: role.Users ? role.Users.length : 0
                };
            });

            res.render('admin/roles/index-new', {
                title: 'Role Management',
                roles: rolesWithCounts,
                path: req.path,
                user: req.user,
                csrfToken: req.csrfToken ? req.csrfToken() : null
            });
        } catch (error) {
            console.error('Error loading roles:', error);
            res.status(500).render('error', { message: 'Failed to load roles' });
        }
    }
);

// Create role page (Web - New Design)
router.get('/admin/roles/create',
    ensureAuth,
    ensurePermission('ROLE_MANAGE'),
    async (req, res) => {
        try {
            const { Permission } = require('../models');
            const permissions = await Permission.findAll({
                order: [['slug', 'ASC']]
            });

            // Group permissions by category
            const groupedPermissions = {
                'Sites': permissions.filter(p => p.slug.startsWith('SITE')),
                'Users': permissions.filter(p => p.slug.startsWith('USER')),
                'Reports': permissions.filter(p => p.slug.startsWith('REPORT')),
                'Patrols': permissions.filter(p => p.slug.startsWith('PATROL')),
                'Incidents': permissions.filter(p => p.slug.startsWith('INCIDENT')),
                'Schedules': permissions.filter(p => p.slug.startsWith('SCHEDULE')),
                'Zones': permissions.filter(p => p.slug.startsWith('ZONE')),
                'Roles': permissions.filter(p => p.slug.startsWith('ROLE'))
            };

            res.render('admin/roles/create', {
                title: 'Create Role',
                groupedPermissions,
                path: req.path,
                user: req.user,
                csrfToken: req.csrfToken ? req.csrfToken() : null
            });
        } catch (error) {
            console.error('Error loading create role page:', error);
            res.status(500).render('error', { message: 'Failed to load page' });
        }
    }
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
    async (req, res) => {
        try {
            const { Role, Permission } = require('../models');
            const role = await Role.findByPk(req.params.id, {
                include: [{ model: Permission }]
            });

            if (!role) {
                req.flash('error', 'Role not found');
                return res.redirect('/admin/roles');
            }

            const permissions = await Permission.findAll({
                order: [['slug', 'ASC']]
            });

            const assignedPermissionIds = role.Permissions.map(p => p.id);

            const groupedPermissions = {
                'Sites': permissions.filter(p => p.slug.startsWith('SITE')),
                'Users': permissions.filter(p => p.slug.startsWith('USER')),
                'Reports': permissions.filter(p => p.slug.startsWith('REPORT')),
                'Patrols': permissions.filter(p => p.slug.startsWith('PATROL')),
                'Incidents': permissions.filter(p => p.slug.startsWith('INCIDENT')),
                'Schedules': permissions.filter(p => p.slug.startsWith('SCHEDULE')),
                'Zones': permissions.filter(p => p.slug.startsWith('ZONE')),
                'Roles': permissions.filter(p => p.slug.startsWith('ROLE'))
            };

            res.render('admin/roles/edit', {
                title: `Edit Role: ${role.name}`,
                role,
                assignedPermissionIds,
                groupedPermissions,
                isSystemRole: ['admin', 'supervisor', 'guard'].includes(role.name.toLowerCase()),
                path: req.path,
                user: req.user,
                csrfToken: req.csrfToken ? req.csrfToken() : null
            });
        } catch (error) {
            console.error('Error loading edit role page:', error);
            res.status(500).render('error', { message: 'Failed to load page' });
        }
    }
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
