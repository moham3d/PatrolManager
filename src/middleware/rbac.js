module.exports = {
    // Check if user has a specific permission
    hasPermission: (permissionName) => {
        return (req, res, next) => {
            // 1. Check if user is authenticated
            if (!req.user) {
                if (req.xhr || req.headers.accept.indexOf('json') > -1) {
                    return res.status(401).json({ error: true, message: 'Unauthorized' });
                }
                return res.redirect('/login');
            }

            // 2. Check if user has permission
            // Admin role bypasses all checks (optional, but good for dev)
            if (req.user.Role && req.user.Role.name === 'admin') {
                return next();
            }

            const permissions = req.user.Role && req.user.Role.Permissions
                ? req.user.Role.Permissions.map(p => p.name)
                : [];

            if (permissions.includes(permissionName)) {
                return next();
            }

            // 3. User does not have permission
            if (req.xhr || req.headers.accept.indexOf('json') > -1) {
                return res.status(403).json({ error: true, message: 'Forbidden: Insufficient Permissions' });
            }

            res.status(403).render('error', {
                message: 'You do not have permission to perform this action.',
                error: {}
            });
        };
    },

    // Check if user has ANY of the permissions
    hasAnyPermission: (permissionNames) => {
        return (req, res, next) => {
            // ... implemented if needed
            next();
        }
    }
};
