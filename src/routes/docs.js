const express = require('express');
const router = express.Router();
const { ensureAuth, ensureRole } = require('../middleware/auth');

// Public Documentation Index
router.get('/', (req, res) => {
    res.redirect('/docs/manual');
});

// General Manual (Public)
router.get('/manual', (req, res) => {
    res.render('docs/manual', { title: 'User Manual' });
});

// Guard Manual (Public)
router.get('/guard', (req, res) => {
    res.render('docs/guard', { title: 'Guard Manual' });
});

// Admin Manual (Public)
router.get('/admin', (req, res) => {
    res.render('docs/admin', { title: 'Administration Manual' });
});

// Supervisor Manual (Public)
router.get('/supervisor', (req, res) => {
    res.render('docs/supervisor', { title: 'Supervisor Manual' });
});

module.exports = router;
