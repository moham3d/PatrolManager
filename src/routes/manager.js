const express = require('express');
const router = express.Router();
const managerController = require('../controllers/managerController');
const { ensureAuth } = require('../middleware/auth');
const { apiRateLimit } = require('../middleware/rateLimiter');

router.use(apiRateLimit);

// Stats for Dashboard
router.get('/stats', ensureAuth, managerController.getStats);

module.exports = router;
