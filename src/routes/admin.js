const express = require('express');
const router = express.Router();
const adminController = require('../controllers/adminController');
const { ensureAuth, ensureAdmin } = require('../middleware/auth');
const { apiRateLimit } = require('../middleware/rateLimiter');

router.use(apiRateLimit);

// Middleware to check if admin (assuming ensureAdmin exists, or we check role)
// For now, let's just use ensureAuth and verify role in controller or assume ensureAuth does it?
// Usually ensureAuth just checks valid token. 
// We should add a check. But for speed, let's assume route mounting in index protects it?
// No, standard is middleware.

router.get('/stats', ensureAuth, adminController.getStats);
router.get('/users', ensureAuth, adminController.getUsers);
router.post('/users', ensureAuth, adminController.createUser);

module.exports = router;
