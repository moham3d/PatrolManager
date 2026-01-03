const express = require('express');
const router = express.Router();
const db = require('../models');

router.get('/', async (req, res) => {
    const health = {
        status: 'ok',
        timestamp: new Date(),
        uptime: process.uptime(),
        database: 'unknown',
        services: {
            socket: 'unknown'
        }
    };

    // Check database
    try {
        await db.sequelize.authenticate();
        health.database = 'connected';
    } catch (error) {
        health.database = 'disconnected';
        health.status = 'degraded';
    }

    // Check Socket.IO (Assume it's attached to req.io)
    if (req.io) {
        health.services.socket = 'running';
    } else {
        health.services.socket = 'disconnected';
        health.status = 'degraded';
    }

    const statusCode = health.status === 'ok' ? 200 : 503;
    res.status(statusCode).json(health);
});

module.exports = router;
