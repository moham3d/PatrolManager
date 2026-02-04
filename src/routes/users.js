const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');
const { ensureAuth } = require('../middleware/auth');
const { hasPermission } = require('../middleware/rbac');
const { apiRateLimit } = require('../middleware/rateLimiter');
const { body } = require('express-validator');
const { validateRequest } = require('../middleware/validator');
const upload = require('../middleware/userUpload');

router.use(apiRateLimit);

// Apply auth middleware to all user routes
router.use(ensureAuth);

router.get('/', hasPermission('user_view'), userController.index);
router.get('/create', hasPermission('user_create'), userController.create);
router.post('/', hasPermission('user_create'), upload.single('profilePicture'), [
    body('name').trim().notEmpty().withMessage('Name is required'),
    body('email').isEmail().withMessage('Valid email is required'),
    body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters'),
    body('roleId').isInt().withMessage('Role is required'),
    body('nationalId').optional().isString(),
    body('phoneNumber').optional().isString()
], validateRequest, userController.store);
router.get('/:id/edit', hasPermission('user_edit'), userController.edit);
router.post('/:id', hasPermission('user_edit'), upload.single('profilePicture'), [
    body('name').trim().notEmpty().withMessage('Name is required'),
    body('email').isEmail().withMessage('Valid email is required'),
    body('roleId').isInt().withMessage('Role is required'),
    body('nationalId').optional().isString(),
    body('phoneNumber').optional().isString()
], validateRequest, userController.update);
router.post('/:id/delete', hasPermission('user_delete'), userController.destroy);

module.exports = router;
