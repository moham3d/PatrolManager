module.exports = {
    // Check if user has a specific permission
    hasPermission: (permissionSlug) => {
        return (req, res, next) => {
            // 1. Check if user is authenticated
            if (!req.user) {
                if (req.xhr || req.headers.accept?.indexOf('json') > -1) {
                    return res.status(401).json({ error: true, message: 'Unauthorized' });
                }
                return res.redirect('/login');
            }

            // 2. Check if user has permission
            // Admin role bypasses all checks
            if (req.user.Role && req.user.Role.name.toLowerCase() === 'admin') {
                return next();
            }

            const permissions = req.user.Role && req.user.Role.permissions
                ? req.user.Role.permissions.map(p => p.slug.toUpperCase())
                : [];

            if (permissions.includes(permissionSlug.toUpperCase())) {
                return next();
            }

            // 3. User does not have permission
            if (req.xhr || req.headers.accept?.indexOf('json') > -1) {
                return res.status(403).json({ error: true, message: 'Forbidden: Insufficient Permissions' });
            }

            res.status(403).render('error', {
                message: 'You do not have permission to perform this action.',
                error: {}
            });
        };
    },

    // Check if user has ANY of the permissions
    hasAnyPermission: (permissionSlugs) => {
        return (req, res, next) => {
            if (!req.user) {
                if (req.xhr || req.headers.accept?.indexOf('json') > -1) {
                    return res.status(401).json({ error: true, message: 'Unauthorized' });
                }
                return res.redirect('/login');
            }

            if (req.user.Role && req.user.Role.name.toLowerCase() === 'admin') {
                return next();
            }

            const userPermissions = req.user.Role && req.user.Role.permissions
                ? req.user.Role.permissions.map(p => p.slug.toUpperCase())
                : [];

            const slugs = Array.isArray(permissionSlugs) ? permissionSlugs : [permissionSlugs];
            const hasAny = slugs.some(s => userPermissions.includes(s.toUpperCase()));

            if (hasAny) {
                return next();
            }

            if (req.xhr || req.headers.accept?.indexOf('json') > -1) {
                return res.status(403).json({ error: true, message: 'Forbidden: Insufficient Permissions' });
            }

            res.status(403).render('error', {
                message: 'You do not have permission to perform this action.',
                error: {}
            });
        }
    }
};
