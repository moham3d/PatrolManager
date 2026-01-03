const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');
const { ensureAuth } = require('../middleware/auth');

// Show Login Page
router.get('/login', (req, res) => {
    if (req.isAuthenticated()) return res.redirect('/');
    res.render('login', { title: 'Login', error: null, csrfToken: req.csrfToken() });
});

// Handle Login
router.post('/login', authController.login);

// Logout
router.get('/logout', authController.logout);
router.post('/logout', ensureAuth, authController.logout);

// Refresh Token
router.post('/refresh-token', ensureAuth, authController.refreshToken);

module.exports = router;
