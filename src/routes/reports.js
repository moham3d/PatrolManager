const express = require('express');
const router = express.Router();
const reportController = require('../controllers/reportController');
const { ensureAuth } = require('../middleware/auth');
const { apiRateLimit } = require('../middleware/rateLimiter');

router.get('/', ensureAuth, reportController.index);
router.get('/api/missed-checkpoints', apiRateLimit, ensureAuth, reportController.getMissedCheckpoints);
router.get('/api/incident-summary', apiRateLimit, ensureAuth, reportController.getIncidentSummary);
router.get('/api/shift-analytics', apiRateLimit, ensureAuth, reportController.getShiftAnalytics);

module.exports = router;
