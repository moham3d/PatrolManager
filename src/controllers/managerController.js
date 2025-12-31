const { PatrolRun, Incident, Site, User } = require('../models');
const { Op } = require('sequelize');

exports.getStats = async (req, res) => {
    try {
        const managerId = req.user.id;

        // 1. Get Sites managed by this user (or all if admin/supermanager)
        // For MVP assuming manager sees all or specific site logic
        // Let's assume manager user has 'siteId' or we just show global for now

        const todayStart = new Date();
        todayStart.setHours(0, 0, 0, 0);

        const totalPatrolsToday = await PatrolRun.count({
            where: {
                startTime: { [Op.gte]: todayStart }
            }
        });

        const incidentsToday = await Incident.count({
            where: {
                createdAt: { [Op.gte]: todayStart }
            }
        });

        const recentIncidents = await Incident.findAll({
            limit: 5,
            order: [['createdAt', 'DESC']],
            include: [{ model: User, as: 'reporter', attributes: ['name'] }]
        });

        res.json({
            patrolsToday: totalPatrolsToday,
            incidentsToday,
            complianceRate: 95, // Dummy for MVP
            recentIncidents
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};
