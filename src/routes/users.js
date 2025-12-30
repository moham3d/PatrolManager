const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');
const { ensureAuth } = require('../middleware/auth');
const { hasPermission } = require('../middleware/rbac');

// Apply auth middleware to all user routes
router.use(ensureAuth);

router.get('/', hasPermission('user_view'), userController.index);
router.get('/create', hasPermission('user_create'), userController.create);
router.post('/', hasPermission('user_create'), userController.store);
router.get('/:id/edit', hasPermission('user_edit'), userController.edit);
router.post('/:id', hasPermission('user_edit'), userController.update);
router.post('/:id/delete', hasPermission('user_delete'), userController.destroy);

module.exports = router;
