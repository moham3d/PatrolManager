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

router.get('/', hasPermission('USER_VIEW'), userController.index);
router.get('/create', hasPermission('USER_CREATE'), userController.create);
router.post('/', hasPermission('USER_CREATE'), upload.single('profilePicture'), [
    body('name').trim().notEmpty().withMessage('Name is required'),
    body('email').isEmail().withMessage('Valid email is required'),
    body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters'),
    body('roleId').isInt().withMessage('Role is required'),
    body('nationalId').optional().isString(),
    body('phoneNumber').optional().isString()
], validateRequest, userController.store);
router.get('/:id/edit', hasPermission('USER_EDIT'), userController.edit);
router.post('/:id', hasPermission('USER_EDIT'), upload.single('profilePicture'), [
    body('name').trim().notEmpty().withMessage('Name is required'),
    body('email').isEmail().withMessage('Valid email is required'),
    body('roleId').isInt().withMessage('Role is required'),
    body('nationalId').optional().isString(),
    body('phoneNumber').optional().isString()
], validateRequest, userController.update);
router.post('/:id/delete', hasPermission('USER_DELETE'), userController.destroy);

module.exports = router;
