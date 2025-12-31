const express = require('express');
const router = express.Router();
const shiftController = require('../controllers/shiftController');
const { ensureAuth, ensureRole } = require('../middleware/auth');

router.get('/', ensureAuth, shiftController.index);
router.post('/clock-in', ensureAuth, shiftController.clockIn);
router.post('/clock-out', ensureAuth, shiftController.clockOut);

// Manager Routes
router.post('/create', ensureAuth, ensureRole(['admin', 'manager', 'supervisor']), shiftController.create);
router.post('/:id/delete', ensureAuth, ensureRole(['admin', 'manager', 'supervisor']), shiftController.destroy);

module.exports = router;
