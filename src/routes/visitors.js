const express = require('express');
const router = express.Router();
const visitorController = require('../controllers/visitorController');
const { ensureAuth } = require('../middleware/auth');
const { apiRateLimit } = require('../middleware/rateLimiter');
const { body } = require('express-validator');
const { validateRequest } = require('../middleware/validator');

router.get('/', ensureAuth, visitorController.index);
router.get('/api/today', apiRateLimit, ensureAuth, visitorController.getToday); // Mobile Bridge
router.post('/register', ensureAuth, [
    body('name').trim().notEmpty().withMessage('Visitor name is required'),
    body('siteId').isInt().withMessage('Site is required'),
    body('hostName').trim().notEmpty().withMessage('Host name is required'),
    body('expectedArrivalTime').isISO8601().withMessage('Valid arrival time is required')
], validateRequest, visitorController.preRegister);
router.post('/:id/check-in', ensureAuth, visitorController.checkIn);
router.post('/:id/check-out', ensureAuth, visitorController.checkOut);

// API for Mobile
router.get('/today', apiRateLimit, ensureAuth, visitorController.getToday);

module.exports = router;
