const express = require('express');
const router = express.Router();
const incidentController = require('../controllers/incidentController');
const { ensureAuth, ensureRole } = require('../middleware/auth');
const { apiRateLimit, panicRateLimit } = require('../middleware/rateLimiter');

const upload = require('../middleware/upload');

router.use(apiRateLimit);

router.get('/', ensureAuth, incidentController.index);
router.get('/create', ensureAuth, ensureRole(['manager', 'supervisor', 'guard']), incidentController.create); // Guards can report
router.get('/:id', ensureAuth, incidentController.show);
router.post('/', ensureAuth, upload.single('evidence'), incidentController.store);

// Panic Button
router.post('/panic', ensureAuth, panicRateLimit, incidentController.triggerPanic);

// Monitor & API
router.get('/monitor', ensureAuth, ensureRole(['manager', 'supervisor']), incidentController.monitor);
router.get('/active', ensureAuth, incidentController.apiList);
// Assignment & Mobile Resolution
// Assignment & Resolution
router.post('/:id/claim', ensureAuth, ensureRole(['guard', 'manager', 'supervisor']), incidentController.claim);
router.post('/:id/assign', ensureAuth, ensureRole(['manager', 'supervisor']), incidentController.assign);
router.post('/api/:id/resolve', ensureAuth, upload.single('evidence'), incidentController.resolveApi);
router.post('/:id/resolve', ensureAuth, upload.single('evidence'), incidentController.resolve);

module.exports = router;
