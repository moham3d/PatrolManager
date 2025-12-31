const express = require('express');
const router = express.Router();
const patrolController = require('../controllers/patrolController');
const { ensureAuth } = require('../middleware/auth');

// Live Data
router.get('/live-patrols', ensureAuth, patrolController.livePatrols);

module.exports = router;
