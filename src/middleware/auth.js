const passport = require('passport');

const checkRole = (roles) => {
    return (req, res, next) => {
        // Allow single role string or array
        const allowedRoles = Array.isArray(roles) ? roles.map(r => r.toLowerCase()) : [roles.toLowerCase()];

        if (req.user && req.user.Role && allowedRoles.includes(req.user.Role.name.toLowerCase())) {
            return next();
        }

        // Access Denied
        if (req.accepts('html') && !req.is('json') && !req.path.startsWith('/api')) {
            req.flash('error', 'Access Denied: You do not have permission to view this resource.');
            return res.redirect('/');
        }
        res.status(403).json({ error: true, message: 'Access Denied: Insufficient permissions' });
    };
};

module.exports = {
    // Check if user is authenticated (Web Session)
    isAuthenticated: (req, res, next) => {
        if (req.isAuthenticated()) {
            return next();
        }
        res.redirect('/login');
    },

    // Check if user is authenticated (Web or Mobile)
    ensureAuth: (req, res, next) => {
        // 1. Check Session
        if (req.isAuthenticated()) {
            return next();
        }

        // 2. Check JWT (Mobile)
        passport.authenticate('jwt', { session: false }, (err, user, info) => {
            if (err) return next(err);
            if (!user) {
                if (req.accepts('html') && !req.is('json') && !req.path.startsWith('/api')) {
                    return res.redirect('/login');
                }
                return res.status(401).json({ error: true, message: 'Unauthorized' });
            }
            req.user = user;
            next();
        })(req, res, next);
    },

    // Check for specific roles
    ensureRole: (roles) => {
        return (req, res, next) => {
            // 1. Check Session
            if (req.isAuthenticated()) {
                return checkRole(roles)(req, res, next);
            }

            // 2. Check JWT (Mobile)
            passport.authenticate('jwt', { session: false }, (err, user, info) => {
                if (err) return next(err);
                if (user) {
                    req.user = user;
                    return checkRole(roles)(req, res, next);
                }
                
                if (req.xhr || req.headers.accept?.includes('json') || req.path.startsWith('/api')) {
                    return res.status(401).json({ error: true, message: 'Authentication required' });
                }
                return res.redirect('/login');
            })(req, res, next);
        };
    },

    ensureAdmin: () => {
        return (req, res, next) => {
            // 1. Check Session
            if (req.isAuthenticated()) {
                return checkRole('admin')(req, res, next);
            }

            // 2. Check JWT (Mobile)
            passport.authenticate('jwt', { session: false }, (err, user, info) => {
                if (err) return next(err);
                if (user) {
                    req.user = user;
                    return checkRole('admin')(req, res, next);
                }
                
                if (req.xhr || req.headers.accept?.includes('json') || req.path.startsWith('/api')) {
                    return res.status(401).json({ error: true, message: 'Authentication required' });
                }
                return res.redirect('/login');
            })(req, res, next);
        };
    }
};
