const express = require('express');
const router = express.Router();
const visitorController = require('../controllers/visitorController');
const { ensureAuth } = require('../middleware/auth');
const { apiRateLimit } = require('../middleware/rateLimiter');

router.get('/', ensureAuth, visitorController.index);
router.get('/api/today', apiRateLimit, ensureAuth, visitorController.getToday); // Mobile Bridge
router.post('/register', ensureAuth, visitorController.preRegister);
router.post('/:id/check-in', ensureAuth, visitorController.checkIn);
router.post('/:id/check-out', ensureAuth, visitorController.checkOut);

// API for Mobile
router.get('/today', apiRateLimit, ensureAuth, visitorController.getToday);

module.exports = router;
