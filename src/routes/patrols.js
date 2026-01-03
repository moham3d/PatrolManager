const express = require('express');
const router = express.Router();
const patrolController = require('../controllers/patrolController');
const { ensureAuth } = require('../middleware/auth');
const { apiRateLimit } = require('../middleware/rateLimiter');

// Mobile API
// Note: ensureAuth handles JWT for these too
router.get('/my-schedule', apiRateLimit, ensureAuth, patrolController.myPatrols);
router.post('/start', apiRateLimit, ensureAuth, patrolController.startPatrol);
router.post('/scan', apiRateLimit, ensureAuth, patrolController.scanCheckpoint);
router.post('/end', apiRateLimit, ensureAuth, patrolController.endPatrol);
router.post('/heartbeat', apiRateLimit, ensureAuth, patrolController.heartbeat);

// Web Interface
router.get('/', ensureAuth, patrolController.index);
router.get('/create', ensureAuth, patrolController.create);
router.post('/', ensureAuth, patrolController.store);
router.get('/:id/edit', ensureAuth, patrolController.edit);
router.post('/:id', ensureAuth, patrolController.update);
router.get('/:id', ensureAuth, patrolController.show);

module.exports = router;
