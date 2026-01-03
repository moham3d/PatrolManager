const express = require('express');
const router = express.Router();
const passport = require('passport');
const jwt = require('jsonwebtoken');
const { Shift } = require('../models');

// Show Login Page
router.get('/login', (req, res) => {
    if (req.isAuthenticated()) return res.redirect('/');
    res.render('login', { title: 'Login', error: null });
});

// Handle Login
router.post('/login', (req, res, next) => {
    passport.authenticate('local', (err, user, info) => {
        if (err) return next(err);

        // Web Authentication (Session)
        if (!user) {
            // If requesting JSON, return 401
            if (req.xhr || req.headers.accept.indexOf('json') > -1) {
                return res.status(401).json({ error: true, message: 'Invalid credentials' });
            }
            return res.render('login', { title: 'Login', error: 'Invalid credentials' });
        }

        req.logIn(user, (err) => {
            if (err) return next(err);

            req.session.regenerate((err) => {
                if (err) return next(err);

                // If requesting JSON (Mobile), return JWT
                // Note: Our test runner sends Accept: application/json
                // But we also want to support browser form submit (which wants redirect)

                // Mobile/API always wants JSON
                if (req.xhr || req.headers.accept?.includes('json')) {
                    const token = jwt.sign({ id: user.id }, process.env.JWT_SECRET || 'secret', { expiresIn: '1d' });

                    // Fetch Active Shift
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

                // Browser
                res.redirect('/dashboard');
            });
        });
    })(req, res, next);
});

// Logout
router.get('/logout', (req, res, next) => {
    req.logout((err) => {
        if (err) return next(err);
        res.redirect('/login');
    });
});

module.exports = router;
