const express = require('express');
const router = express.Router();
const patrolController = require('../controllers/patrolController');
const { ensureAuth, ensureRole } = require('../middleware/auth');
const { apiRateLimit } = require('../middleware/rateLimiter');

// Mobile API
// Note: ensureAuth handles JWT for these too
router.get('/my-schedule', apiRateLimit, ensureAuth, ensureRole(['guard']), patrolController.myPatrols);
router.post('/start', apiRateLimit, ensureAuth, ensureRole(['guard']), patrolController.startPatrol);
router.post('/scan', apiRateLimit, ensureAuth, ensureRole(['guard']), patrolController.scanCheckpoint);
router.post('/end', apiRateLimit, ensureAuth, ensureRole(['guard']), patrolController.endPatrol);
router.post('/heartbeat', apiRateLimit, ensureAuth, ensureRole(['guard']), patrolController.heartbeat);

// Web Interface
router.get('/', ensureAuth, ensureRole(['admin', 'manager', 'supervisor']), patrolController.index);
router.get('/create', ensureAuth, ensureRole(['admin', 'manager']), patrolController.create);
router.post('/', ensureAuth, ensureRole(['admin', 'manager']), patrolController.store);
router.get('/:id/edit', ensureAuth, ensureRole(['admin', 'manager']), patrolController.edit);
router.post('/:id', ensureAuth, ensureRole(['admin', 'manager']), patrolController.update);
router.get('/:id', ensureAuth, ensureRole(['admin', 'manager', 'supervisor']), patrolController.show);

module.exports = router;
