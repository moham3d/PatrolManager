const express = require('express');
const router = express.Router();
const scheduleController = require('../controllers/scheduleController');
const { ensureAuth, ensureRole } = require('../middleware/auth');
const { apiRateLimit } = require('../middleware/rateLimiter');

router.use(apiRateLimit);

router.get('/', ensureAuth, scheduleController.index);
router.post('/', ensureAuth, ensureRole(['admin', 'manager', 'supervisor']), scheduleController.create);
router.post('/:id/delete', ensureAuth, ensureRole(['admin', 'manager', 'supervisor']), scheduleController.delete);

module.exports = router;
