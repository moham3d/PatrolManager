const express = require('express');
const router = express.Router();
const roleController = require('../controllers/roleController');
const { ensureAdmin } = require('../middleware/auth');

// Apply Admin check to all routes
router.use(ensureAdmin());

router.get('/', roleController.index);
router.get('/create', roleController.edit);
router.get('/edit/:id', roleController.edit);
router.post('/save', roleController.save);
router.post('/delete/:id', roleController.delete);

module.exports = router;
