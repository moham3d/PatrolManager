const express = require('express');
const router = express.Router();
const shiftController = require('../controllers/shiftController');
const { ensureAuth, ensureRole } = require('../middleware/auth');
const { apiRateLimit } = require('../middleware/rateLimiter');
const { body } = require('express-validator');
const { validateRequest } = require('../middleware/validator');

router.use(apiRateLimit);

router.get('/', ensureAuth, shiftController.index);
router.post('/clock-in', ensureAuth, [
    body('siteId').isInt(),
    body('latitude').isFloat(),
    body('longitude').isFloat()
], validateRequest, shiftController.clockIn);
router.post('/clock-out', ensureAuth, [
    body('latitude').isFloat(),
    body('longitude').isFloat()
], validateRequest, shiftController.clockOut);

// Manager Routes
router.post('/create', ensureAuth, ensureRole(['admin', 'manager', 'supervisor']), [
    body('userId').isInt(),
    body('siteId').isInt(),
    body('date').isISO8601(),
    body('startTime').isTime(),
    body('endTime').isTime()
], validateRequest, shiftController.create);
router.post('/:id/delete', ensureAuth, ensureRole(['admin', 'manager', 'supervisor']), shiftController.destroy);

module.exports = router;
