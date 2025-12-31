const express = require('express');
const router = express.Router();
const patrolController = require('../controllers/patrolController');
const { ensureAuth } = require('../middleware/auth');

// Mobile API
// Note: ensureAuth handles JWT for these too
router.get('/my-schedule', ensureAuth, patrolController.myPatrols);
router.post('/start', ensureAuth, patrolController.startPatrol);
router.post('/scan', ensureAuth, patrolController.scanCheckpoint);
router.post('/end', ensureAuth, patrolController.endPatrol);
router.post('/heartbeat', ensureAuth, patrolController.heartbeat);

// Web Interface
router.get('/', ensureAuth, patrolController.index);
router.get('/create', ensureAuth, patrolController.create);
router.post('/', ensureAuth, patrolController.store);
router.get('/:id/edit', ensureAuth, patrolController.edit);
router.post('/:id', ensureAuth, patrolController.update);
router.get('/:id', ensureAuth, patrolController.show);

module.exports = router;
