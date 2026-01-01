const passport = require('passport');

module.exports = {
    // Check if user is authenticated (Web Session)
    isAuthenticated: (req, res, next) => {
        if (req.isAuthenticated()) {
            const roleName = req.user.Role ? req.user.Role.name.toLowerCase() : '';
            if (['guard', 'supervisor'].includes(roleName)) {
                return req.logout((err) => {
                    if (err) return next(err);
                    if (req.flash) req.flash('error', 'Access Denied: Field roles restricted to Mobile App.');
                    res.redirect('/login');
                });
            }
            return next();
        }
        res.redirect('/login');
    },

    // Check if user is authenticated (Web or Mobile)
    // For specific API routes that need to support both
    ensureAuth: (req, res, next) => {
        // 1. Check Session
        if (req.isAuthenticated()) {
            const roleName = req.user.Role ? req.user.Role.name.toLowerCase() : '';
            if (['guard', 'supervisor'].includes(roleName)) {
                return req.logout((err) => {
                    if (err) return next(err);
                    if (req.accepts('html') && !req.is('json') && !req.path.startsWith('/api')) {
                        if (req.flash) req.flash('error', 'Access Denied: Field roles restricted to Mobile App.');
                        return res.redirect('/login');
                    }
                    return res.status(403).json({ error: true, message: 'Access Denied: Field roles restricted to Mobile App' });
                });
            }
            return next();
        }

        // 2. Check JWT (Mobile)
        passport.authenticate('jwt', { session: false }, (err, user, info) => {
            if (err) return next(err);
            if (!user) {
                // If asking for HTML (Browser), redirect
                if (req.accepts('html') && !req.is('json') && !req.path.startsWith('/api')) {
                    return res.redirect('/login');
                }
                // Otherwise return 401 JSON
                return res.status(401).json({ error: true, message: 'Unauthorized' });
            }
            req.user = user;
            next();
        })(req, res, next);
    },

    // Check for specific roles
    ensureRole: (roles) => {
        return (req, res, next) => {
            if (!req.user) return res.redirect('/login');

            // Allow single role string or array
            const allowedRoles = Array.isArray(roles) ? roles.map(r => r.toLowerCase()) : [roles.toLowerCase()];

            if (req.user.Role && allowedRoles.includes(req.user.Role.name.toLowerCase())) {
                return next();
            }

            // Access Denied
            if (req.accepts('html') && !req.is('json') && !req.path.startsWith('/api')) {
                req.flash('error', 'Access Denied: You do not have permission to view this resource.');
                return res.redirect('/');
            }
            res.status(403).json({ error: true, message: 'Access Denied: Insufficient permissions' });
        };
    }
};
