const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');
const { ensureAuth } = require('../middleware/auth');

// Show Login Page
router.get('/login', (req, res) => {
    if (req.isAuthenticated()) {
        if (req.xhr || req.headers.accept?.includes('json')) {
            return res.json({ success: true, authenticated: true, user: req.user });
        }
        return res.redirect('/');
    }
    
    if (req.xhr || req.headers.accept?.includes('json')) {
        return res.status(401).json({ success: false, authenticated: false, message: 'Authentication required' });
    }

    res.render('login', { 
        title: 'Login', 
        error: null, 
        csrfToken: req.csrfToken ? req.csrfToken() : null 
    });
});

// Handle Login
router.post('/login', authController.login);

// Logout
router.get('/logout', authController.logout);
router.post('/logout', ensureAuth, authController.logout);

// Refresh Token
router.post('/refresh-token', ensureAuth, authController.refreshToken);

module.exports = router;
