const passport = require('passport');
const jwt = require('jsonwebtoken');
const { Shift } = require('../models');

exports.login = (req, res, next) => {
    passport.authenticate('local', (err, user, info) => {
        if (err) return next(err);

        if (!user) {
            if (req.xhr || req.headers.accept.indexOf('json') > -1) {
                return res.status(401).json({ error: true, message: info.message });
            }
            return res.render('login', { title: 'Login', error: info.message });
        }

        // Regenerate session BEFORE logging in to prevent session fixation
        // but preserve the session if needed. For Passport, we just need to ensure
        // req.logIn happens on the NEW session.
        req.session.regenerate((err) => {
            if (err) return next(err);

            req.logIn(user, (err) => {
                if (err) return next(err);

                if (req.xhr || req.headers.accept?.includes('json')) {
                    const token = jwt.sign({ id: user.id }, process.env.JWT_SECRET || 'secret', { expiresIn: '1d' });

                    Shift.findOne({
                        where: { userId: user.id, status: 'active' }
                    }).then(activeShift => {
                        return res.json({ message: 'Logged in', token, user, activeShift });
                    }).catch(err => {
                        console.error('Error fetching active shift', err);
                        return res.json({ message: 'Logged in', token, user, activeShift: null });
                    });
                    return;
                }

                res.redirect('/dashboard');
            });
        });
    })(req, res, next);
};

exports.logout = (req, res, next) => {
    req.logout((err) => {
        if (err) return next(err);
        res.redirect('/login');
    });
};

exports.refreshToken = (req, res) => {
    const token = jwt.sign({ id: req.user.id }, process.env.JWT_SECRET || 'secret', { expiresIn: '1d' });
    res.json({ token });
};
