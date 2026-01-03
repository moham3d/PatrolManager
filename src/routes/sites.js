const express = require('express');
const router = express.Router();
const siteController = require('../controllers/siteController');
const { ensureAuth, ensureRole } = require('../middleware/auth');
const { apiRateLimit } = require('../middleware/rateLimiter');
const { body } = require('express-validator');
const { validateRequest, siteValidation } = require('../middleware/validator');

router.use(apiRateLimit);

router.get('/', ensureAuth, siteController.index);
router.get('/:id', ensureAuth, siteController.show);

// Admin & Manager Only
router.get('/create', ensureAuth, ensureRole(['admin', 'manager']), siteController.create);
router.post('/', ensureAuth, ensureRole(['admin', 'manager']), siteValidation, validateRequest, siteController.store);
router.get('/:id/edit', ensureAuth, ensureRole(['admin', 'manager']), siteController.edit);
router.post('/:id', ensureAuth, ensureRole(['admin', 'manager']), siteValidation, validateRequest, siteController.update);
router.post('/:id/delete', ensureAuth, ensureRole(['admin', 'manager']), siteController.destroy);

// Staff management
router.post('/:id/add-staff', ensureAuth, ensureRole(['admin', 'manager']), [
    body('userId').isInt()
], validateRequest, siteController.addStaff);
router.post('/:id/remove-staff', ensureAuth, ensureRole(['admin', 'manager']), [
    body('userId').isInt()
], validateRequest, siteController.removeStaff);

// Associations (Zones & Checkpoints)
router.post('/:id/zones', ensureAuth, ensureRole(['admin', 'manager']), [
    body('name').trim().notEmpty().withMessage('Zone name is required')
], validateRequest, siteController.addZone);
router.post('/:id/checkpoints', ensureAuth, ensureRole(['admin', 'manager']), [
    body('name').trim().notEmpty().withMessage('Checkpoint name is required'),
    body('type').isIn(['nfc', 'qr', 'gps'])
], validateRequest, siteController.addCheckpoint);
router.get('/:id/print-qr', ensureAuth, ensureRole(['admin', 'manager', 'supervisor']), siteController.printQRCodes);

// Zone & Checkpoint Management
router.post('/zones/:id/update', ensureAuth, ensureRole(['admin', 'manager']), [
    body('name').trim().notEmpty().withMessage('Zone name is required')
], validateRequest, siteController.updateZone);
router.post('/zones/:id/delete', ensureAuth, ensureRole(['admin', 'manager']), siteController.deleteZone);
router.post('/checkpoints/:id/update', ensureAuth, ensureRole(['admin', 'manager']), [
    body('name').trim().notEmpty().withMessage('Checkpoint name is required'),
    body('type').isIn(['nfc', 'qr', 'gps'])
], validateRequest, siteController.updateCheckpoint);
router.post('/checkpoints/:id/delete', ensureAuth, ensureRole(['admin', 'manager']), siteController.deleteCheckpoint);

module.exports = router;
