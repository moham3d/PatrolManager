const express = require('express');
const router = express.Router();
const incidentController = require('../controllers/incidentController');
const { ensureAuth, ensureRole } = require('../middleware/auth');
const { apiRateLimit, panicRateLimit } = require('../middleware/rateLimiter');
const { body, param } = require('express-validator');
const { validateRequest, incidentValidation } = require('../middleware/validator');

const upload = require('../middleware/upload');

router.use(apiRateLimit);

router.get('/', ensureAuth, incidentController.index);
router.get('/create', ensureAuth, ensureRole(['manager', 'supervisor', 'guard']), incidentController.create); // Guards can report
router.get('/:id', ensureAuth, incidentController.show);
router.post('/', ensureAuth, upload.single('evidence'), incidentValidation, validateRequest, incidentController.store);

// Panic Button
router.post('/panic', ensureAuth, panicRateLimit, [
    body('location').optional(),
    body('patrolRunId').optional().isInt()
], validateRequest, incidentController.triggerPanic);

// Monitor & API
router.get('/monitor', ensureAuth, ensureRole(['manager', 'supervisor']), incidentController.monitor);
router.get('/active', ensureAuth, incidentController.apiList);
// Assignment & Mobile Resolution
// Assignment & Resolution
router.post('/:id/claim', ensureAuth, ensureRole(['guard', 'manager', 'supervisor']), [
    param('id').isInt()
], validateRequest, incidentController.claim);
router.post('/:id/assign', ensureAuth, ensureRole(['manager', 'supervisor']), incidentController.assign);
router.post('/:id/comments', ensureAuth, incidentController.addComment);
router.patch('/:id/status', ensureAuth, ensureRole(['manager', 'supervisor', 'admin']), [
    param('id').isInt(),
    body('status').isIn(['new', 'investigating', 'resolved', 'closed'])
], validateRequest, incidentController.updateStatus);
router.post('/api/:id/resolve', ensureAuth, upload.single('evidence'), incidentController.resolveApi);
router.post('/:id/resolve', ensureAuth, upload.single('evidence'), incidentController.resolve);

module.exports = router;
