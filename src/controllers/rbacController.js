const { Role, Permission, User } = require('../models');

// Role Management
const roleController = {
    // List all roles
    listRoles: async (req, res) => {
        try {
            const roles = await Role.findAll({
                include: [{ model: Permission }],
                order: [['name', 'ASC']]
            });
            res.json(roles);
        } catch (error) {
            res.status(500).json({ error: true, message: error.message });
        }
    },

    // Get single role with permissions
    getRole: async (req, res) => {
        try {
            const role = await Role.findByPk(req.params.id, {
                include: [{ model: Permission }]
            });
            if (!role) {
                return res.status(404).json({ error: true, message: 'Role not found' });
            }
            res.json(role);
        } catch (error) {
            res.status(500).json({ error: true, message: error.message });
        }
    },

    // Create new role
    createRole: async (req, res) => {
        try {
            const { name, description, permissionIds } = req.body;
            const role = await Role.create({
                name: name.toLowerCase(),
                description,
                createdBy: req.user?.id
            });

            // Assign permissions if provided
            if (permissionIds && permissionIds.length > 0) {
                await role.setPermissions(permissionIds);
            }

            res.status(201).json(role);
        } catch (error) {
            res.status(400).json({ error: true, message: error.message });
        }
    },

    // Update role
    updateRole: async (req, res) => {
        try {
            const role = await Role.findByPk(req.params.id);
            if (!role) {
                return res.status(404).json({ error: true, message: 'Role not found' });
            }

            const { name, description, permissionIds } = req.body;

            // Update basic fields
            await role.update({
                name: name ? name.toLowerCase() : role.name,
                description,
                updatedBy: req.user?.id
            });

            // Update permissions if provided
            if (permissionIds !== undefined) {
                await role.setPermissions(permissionIds);
            }

            // Return updated role with permissions
            const updatedRole = await Role.findByPk(role.id, {
                include: [{ model: Permission }]
            });

            res.json(updatedRole);
        } catch (error) {
            res.status(400).json({ error: true, message: error.message });
        }
    },

    // Delete role
    deleteRole: async (req, res) => {
        try {
            const role = await Role.findByPk(req.params.id);
            if (!role) {
                return res.status(404).json({ error: true, message: 'Role not found' });
            }

            // Prevent deletion of system roles
            if (['admin', 'supervisor', 'guard'].includes(role.name.toLowerCase())) {
                return res.status(403).json({
                    error: true,
                    message: 'Cannot delete system roles'
                });
            }

            // Check if users are assigned to this role
            const userCount = await User.count({ where: { roleId: role.id } });
            if (userCount > 0) {
                return res.status(409).json({
                    error: true,
                    message: `Cannot delete role with ${userCount} assigned users`
                });
            }

            await role.destroy();
            res.json({ message: 'Role deleted successfully' });
        } catch (error) {
            res.status(500).json({ error: true, message: error.message });
        }
    },

    // List users by role
    listRoleUsers: async (req, res) => {
        try {
            const role = await Role.findByPk(req.params.id, {
                include: [{ model: User }]
            });
            if (!role) {
                return res.status(404).json({ error: true, message: 'Role not found' });
            }
            res.json(role.Users);
        } catch (error) {
            res.status(500).json({ error: true, message: error.message });
        }
    }
};

// Permission Management
const permissionController = {
    // List all permissions
    listPermissions: async (req, res) => {
        try {
            const permissions = await Permission.findAll({
                order: [['slug', 'ASC']]
            });
            res.json(permissions);
        } catch (error) {
            res.status(500).json({ error: true, message: error.message });
        }
    },

    // Get single permission
    getPermission: async (req, res) => {
        try {
            const permission = await Permission.findByPk(req.params.id);
            if (!permission) {
                return res.status(404).json({ error: true, message: 'Permission not found' });
            }
            res.json(permission);
        } catch (error) {
            res.status(500).json({ error: true, message: error.message });
        }
    },

    // Create permission
    createPermission: async (req, res) => {
        try {
            const { name, slug, description } = req.body;
            const permission = await Permission.create({
                name,
                slug: slug.toUpperCase(),
                description,
                createdBy: req.user?.id
            });
            res.status(201).json(permission);
        } catch (error) {
            res.status(400).json({ error: true, message: error.message });
        }
    },

    // Update permission
    updatePermission: async (req, res) => {
        try {
            const permission = await Permission.findByPk(req.params.id);
            if (!permission) {
                return res.status(404).json({ error: true, message: 'Permission not found' });
            }

            const { name, slug, description } = req.body;

            await permission.update({
                name,
                slug: slug ? slug.toUpperCase() : permission.slug,
                description,
                updatedBy: req.user?.id
            });

            res.json(permission);
        } catch (error) {
            res.status(400).json({ error: true, message: error.message });
        }
    },

    // Delete permission
    deletePermission: async (req, res) => {
        try {
            const permission = await Permission.findByPk(req.params.id);
            if (!permission) {
                return res.status(404).json({ error: true, message: 'Permission not found' });
            }

            // Check if any roles use this permission
            const roleCount = await permission.countRoles();
            if (roleCount > 0) {
                return res.status(409).json({
                    error: true,
                    message: `Cannot delete permission used by ${roleCount} roles`
                });
            }

            await permission.destroy();
            res.json({ message: 'Permission deleted successfully' });
        } catch (error) {
            res.status(500).json({ error: true, message: error.message });
        }
    }
};

module.exports = { roleController, permissionController };
