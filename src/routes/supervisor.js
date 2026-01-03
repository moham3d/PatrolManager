const express = require('express');
const router = express.Router();
const patrolController = require('../controllers/patrolController');
const { ensureAuth } = require('../middleware/auth');
const { apiRateLimit } = require('../middleware/rateLimiter');

router.use(apiRateLimit);

// Live Data
router.get('/live-patrols', ensureAuth, patrolController.livePatrols);

module.exports = router;
