const express = require('express');
const router = express.Router();
const reportController = require('../controllers/reportController');
const { ensureAuth } = require('../middleware/auth');
const { apiRateLimit } = require('../middleware/rateLimiter');

const { cacheMiddleware } = require('../config/cache');

router.get('/', ensureAuth, reportController.index);
router.post('/schedules', ensureAuth, ensureRole(['admin', 'manager']), reportController.createSchedule);
router.post('/schedules/:id/delete', ensureAuth, ensureRole(['admin', 'manager']), reportController.deleteSchedule);
router.get('/export/incidents', ensureAuth, ensureRole(['admin', 'manager']), reportController.exportIncidents);
router.get('/api/patrol-analytics', apiRateLimit, ensureAuth, reportController.getPatrolAnalytics);
router.get('/api/incident-trends', apiRateLimit, ensureAuth, reportController.getIncidentTrends);
router.get('/api/missed-checkpoints', apiRateLimit, ensureAuth, reportController.getMissedCheckpoints);
router.get('/api/incident-summary', apiRateLimit, ensureAuth, cacheMiddleware(300), reportController.getIncidentSummary);
router.get('/api/shift-analytics', apiRateLimit, ensureAuth, cacheMiddleware(300), reportController.getShiftAnalytics);

module.exports = router;
