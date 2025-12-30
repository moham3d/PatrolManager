const express = require('express');
const router = express.Router();
const reportController = require('../controllers/reportController');
const { ensureAuth } = require('../middleware/auth');

router.get('/', ensureAuth, reportController.index);
router.get('/api/missed-checkpoints', ensureAuth, reportController.getMissedCheckpoints);
router.get('/api/incident-summary', ensureAuth, reportController.getIncidentSummary);
router.get('/api/shift-analytics', ensureAuth, reportController.getShiftAnalytics);

module.exports = router;
