const express = require('express');
const router = express.Router();
const siteController = require('../controllers/siteController');
const { ensureAuth, ensureRole } = require('../middleware/auth');

router.get('/', ensureAuth, siteController.index);
router.get('/:id', ensureAuth, siteController.show);

// Admin & Manager Only
router.get('/create', ensureAuth, ensureRole(['admin', 'manager']), siteController.create);
router.post('/', ensureAuth, ensureRole(['admin', 'manager']), siteController.store);
router.get('/:id/edit', ensureAuth, ensureRole(['admin', 'manager']), siteController.edit);
router.post('/:id', ensureAuth, ensureRole(['admin', 'manager']), siteController.update);
router.post('/:id/delete', ensureAuth, ensureRole(['admin', 'manager']), siteController.destroy);

// Staff management
router.post('/:id/add-staff', ensureAuth, ensureRole(['admin', 'manager']), siteController.addStaff);
router.post('/:id/remove-staff', ensureAuth, ensureRole(['admin', 'manager']), siteController.removeStaff);

// Associations (Zones & Checkpoints)
router.post('/:id/zones', ensureAuth, ensureRole(['admin', 'manager']), siteController.addZone);
router.post('/:id/checkpoints', ensureAuth, ensureRole(['admin', 'manager']), siteController.addCheckpoint);
router.get('/:id/print-qr', ensureAuth, ensureRole(['admin', 'manager', 'supervisor']), siteController.printQRCodes);

// Zone & Checkpoint Management
router.post('/zones/:id/update', ensureAuth, ensureRole(['admin', 'manager']), siteController.updateZone);
router.post('/zones/:id/delete', ensureAuth, ensureRole(['admin', 'manager']), siteController.deleteZone);
router.post('/checkpoints/:id/update', ensureAuth, ensureRole(['admin', 'manager']), siteController.updateCheckpoint);
router.post('/checkpoints/:id/delete', ensureAuth, ensureRole(['admin', 'manager']), siteController.deleteCheckpoint);

module.exports = router;
