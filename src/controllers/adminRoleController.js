const { Role, Permission, User } = require('../models');

// ==================== WEB VIEWS ====================

// List all roles (Web)
const listRolesPage = async (req, res) => {
    try {
        const roles = await Role.findAll({
            include: [
                { model: Permission },
                { model: User, attributes: ['id'] } // Just count users
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

        res.render('admin/roles/index', {
            title: 'Role Management',
            roles: rolesWithCounts,
            success: req.flash('success'),
            error: req.flash('error')
        });
    } catch (error) {
        console.error('Error loading roles:', error);
        res.status(500).render('error', { message: 'Failed to load roles' });
    }
};

// Create role page (Web)
const createRolePage = async (req, res) => {
    try {
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
            success: req.flash('success'),
            error: req.flash('error')
        });
    } catch (error) {
        console.error('Error loading create role page:', error);
        res.status(500).render('error', { message: 'Failed to load page' });
    }
};

// Edit role page (Web)
const editRolePage = async (req, res) => {
    try {
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

        // Get assigned permission IDs
        const assignedPermissionIds = role.Permissions.map(p => p.id);

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

        res.render('admin/roles/edit', {
            title: `Edit Role: ${role.name}`,
            role,
            assignedPermissionIds,
            groupedPermissions,
            isSystemRole: ['admin', 'supervisor', 'guard'].includes(role.name.toLowerCase()),
            success: req.flash('success'),
            error: req.flash('error')
        });
    } catch (error) {
        console.error('Error loading edit role page:', error);
        res.status(500).render('error', { message: 'Failed to load page' });
    }
};

// ==================== WEB ACTIONS ====================

// Create role (Web POST)
const createRoleWeb = async (req, res) => {
    try {
        const { name, description, permissionIds } = req.body;

        // Check if role name is already taken
        const existingRole = await Role.findOne({
            where: { name: name.toLowerCase() }
        });

        if (existingRole) {
            req.flash('error', 'Role name already exists');
            return res.redirect('/admin/roles/create');
        }

        const role = await Role.create({
            name: name.toLowerCase(),
            description,
            createdBy: req.user.id
        });

        // Assign permissions
        if (permissionIds && Array.isArray(permissionIds)) {
            await role.setPermissions(permissionIds);
        }

        req.flash('success', `Role "${name}" created successfully`);
        res.redirect('/admin/roles');
    } catch (error) {
        console.error('Error creating role:', error);
        req.flash('error', 'Failed to create role');
        res.redirect('/admin/roles/create');
    }
};

// Update role (Web POST)
const updateRoleWeb = async (req, res) => {
    try {
        const role = await Role.findByPk(req.params.id);

        if (!role) {
            req.flash('error', 'Role not found');
            return res.redirect('/admin/roles');
        }

        // Prevent editing system roles
        if (['admin', 'supervisor', 'guard'].includes(role.name.toLowerCase())) {
            req.flash('error', 'Cannot edit system roles');
            return res.redirect('/admin/roles');
        }

        const { name, description, permissionIds } = req.body;

        await role.update({
            name: name ? name.toLowerCase() : role.name,
            description,
            updatedBy: req.user.id
        });

        // Update permissions
        if (permissionIds !== undefined) {
            await role.setPermissions(permissionIds);
        }

        req.flash('success', `Role "${role.name}" updated successfully`);
        res.redirect('/admin/roles');
    } catch (error) {
        console.error('Error updating role:', error);
        req.flash('error', 'Failed to update role');
        res.redirect(`/admin/roles/${req.params.id}/edit`);
    }
};

// Delete role (Web POST)
const deleteRoleWeb = async (req, res) => {
    try {
        const role = await Role.findByPk(req.params.id);

        if (!role) {
            req.flash('error', 'Role not found');
            return res.redirect('/admin/roles');
        }

        // Prevent deletion of system roles
        if (['admin', 'supervisor', 'guard'].includes(role.name.toLowerCase())) {
            req.flash('error', 'Cannot delete system roles');
            return res.redirect('/admin/roles');
        }

        // Check if users are assigned to this role
        const userCount = await User.count({ where: { roleId: role.id } });
        if (userCount > 0) {
            req.flash('error', `Cannot delete role with ${userCount} assigned users`);
            return res.redirect('/admin/roles');
        }

        await role.destroy();
        req.flash('success', `Role "${role.name}" deleted successfully`);
        res.redirect('/admin/roles');
    } catch (error) {
        console.error('Error deleting role:', error);
        req.flash('error', 'Failed to delete role');
        res.redirect('/admin/roles');
    }
};

// ==================== API CONTROLLERS (Reused) ====================

const { roleController: apiRoleController, permissionController } = require('./rbacController');

module.exports = {
    // Web Views
    listRolesPage,
    createRolePage,
    editRolePage,

    // Web Actions
    createRoleWeb,
    updateRoleWeb,
    deleteRoleWeb,

    // API Controllers (for API routes)
    ...apiRoleController,
    permissionController
};
