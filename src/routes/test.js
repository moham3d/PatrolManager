const express = require('express');
const router = express.Router();
const { ensureAuth } = require('../middleware/auth');

// Simple test endpoint to check permissions without JWT
router.get('/test', ensureAuth, async (req, res) => {
    try {
        const { Role, Permission } = require('../models');

        const user = await Role.findByPk(req.user.roleId, {
            include: [{ model: Permission }]
        });

        res.json({
            user: req.user.email,
            role: req.user.Role.name,
            permissions: user.Permissions.map(p => p.slug)
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

module.exports = router;
