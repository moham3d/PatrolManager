const passport = require('passport');
const { Role, Permission, Sequelize } = require('../models'); // Import models
const { Op } = Sequelize;

// Helper to check permission(s)
const checkPermission = (permissionSlugs) => {
    return async (req, res, next) => {
        try {
            if (!req.user || !req.user.Role) {
                // If checking API/JSON
                if (req.xhr || req.headers.accept?.includes('json') || req.path.startsWith('/api')) {
                    return res.status(401).json({ error: true, message: 'Unauthorized' });
                }
                return res.redirect('/login');
            }

            // Super Admin bypass
            if (req.user.Role.name.toLowerCase() === 'admin') {
                return next();
            }

            const slugs = Array.isArray(permissionSlugs) ? permissionSlugs : [permissionSlugs];
            
            // Try to use pre-loaded permissions first (efficiency)
            if (req.user.Role.permissions) {
                const userPermissionSlugs = req.user.Role.permissions.map(p => p.slug);
                const hasPerm = slugs.some(s => userPermissionSlugs.includes(s));
                if (hasPerm) return next();
            } else {
                // Fallback to DB query if not loaded
                const roleWithPermissions = await Role.findByPk(req.user.roleId, {
                    include: [{
                        model: Permission,
                        as: 'permissions',
                        where: { slug: { [Op.in]: slugs } },
                        required: true
                    }]
                });
                if (roleWithPermissions) return next();
            }

            // Access Denied
            if (req.accepts('html') && !req.is('json') && !req.path.startsWith('/api')) {
                req.flash('error', 'Access Denied: Insufficient permissions.');
                return res.redirect('/');
            }
            res.status(403).json({ error: true, message: 'Access Denied: Insufficient permissions' });

        } catch (err) {
            console.error('RBAC Error:', err);
            res.status(500).json({ error: true, message: 'Internal Server Error' });
        }
    };
};

// Legacy Role Check (Kept for backward compatibility)
const checkRole = (roles) => {
    return (req, res, next) => {
        const allowedRoles = Array.isArray(roles) ? roles.map(r => r.toLowerCase()) : [roles.toLowerCase()];
        if (req.user && req.user.Role && allowedRoles.includes(req.user.Role.name.toLowerCase())) {
            return next();
        }
        if (req.accepts('html') && !req.is('json') && !req.path.startsWith('/api')) {
            req.flash('error', 'Access Denied: You do not have permission to view this resource.');
            return res.redirect('/');
        }
        res.status(403).json({ error: true, message: 'Access Denied: Insufficient permissions' });
    };
};

module.exports = {
    // Basic Auth
    isAuthenticated: (req, res, next) => {
        if (req.isAuthenticated()) return next();
        res.redirect('/login');
    },

    // Hybrid Auth (Web + JWT)
    ensureAuth: (req, res, next) => {
        if (req.isAuthenticated()) return next();
        passport.authenticate('jwt', { session: false }, (err, user) => {
            if (err) return next(err);
            if (!user) {
                if (req.accepts('html') && !req.is('json') && !req.path.startsWith('/api')) return res.redirect('/login');
                return res.status(401).json({ error: true, message: 'Unauthorized' });
            }
            req.user = user;
            next();
        })(req, res, next);
    },

    // Legacy Role Check
    ensureRole: (roles) => {
        return (req, res, next) => {
            if (req.isAuthenticated()) return checkRole(roles)(req, res, next);
            passport.authenticate('jwt', { session: false }, (err, user) => {
                if (user) { req.user = user; return checkRole(roles)(req, res, next); }
                res.status(401).json({ error: true, message: 'Unauthorized' });
            })(req, res, next);
        };
    },

    // New Permission Check
    ensurePermission: (permission) => {
        return (req, res, next) => {
            // Check Session
            if (req.isAuthenticated()) {
                return checkPermission(permission)(req, res, next);
            }
            // Check JWT
            passport.authenticate('jwt', { session: false }, (err, user) => {
                if (err) return next(err);
                if (user) { req.user = user; return checkPermission(permission)(req, res, next); }
                res.status(401).json({ error: true, message: 'Unauthorized' });
            })(req, res, next);
        };
    },

    ensureAdmin: () => checkRole('admin') // Helper alias
};
