const express = require('express');
const router = express.Router();

// Routes
router.use('/', (req, res, next) => {
    if (req.path === '/') return next();
    next();
});

const { ensureAuth } = require('../middleware/auth');

// Home Route
router.get('/', ensureAuth, (req, res) => {
    // Redirect to Dashboard
    res.redirect('/dashboard');
});


// Auth Routes
router.use('/', require('./auth'));

// Protected Routes (handled by ensureAuth inside them, or apply middleware here if preferred)
router.use('/sites', require('./sites'));
router.use('/patrols', require('./patrols'));
router.use('/incidents', require('./incidents'));
router.use('/dashboard', require('./dashboard'));
router.use('/shifts', require('./shifts'));
router.use('/reports', require('./reports'));
router.use('/visitors', require('./visitors'));
router.use('/users', require('./users'));
router.use('/docs', require('./docs'));
module.exports = router;
