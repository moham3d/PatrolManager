const express = require('express');
const router = express.Router();
const scheduleController = require('../controllers/scheduleController');
const { ensureAuth } = require('../middleware/auth');
const { apiRateLimit } = require('../middleware/rateLimiter');

router.use(apiRateLimit);

router.get('/', ensureAuth, scheduleController.index);
router.post('/', ensureAuth, scheduleController.create);
router.post('/:id/delete', ensureAuth, scheduleController.delete);

module.exports = router;
