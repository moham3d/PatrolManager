const cron = require('node-cron');
const { Incident, User, Site } = require('../models');
const { Op } = require('sequelize');

const startIncidentReminders = (io) => {
    console.log('🚨 Incident Reminders Started');

    // Run every hour
    cron.schedule('0 * * * *', async () => {
        try {
            const now = new Date();
            const fourHoursAgo = new Date(now.getTime() - 4 * 60 * 60000);
            const twentyFourHoursAgo = new Date(now.getTime() - 24 * 60 * 60000);

            // 1. Find unresolved HIGH/CRITICAL incidents older than 4 hours
            const urgentIncidents = await Incident.findAll({
                where: {
                    status: ['new', 'investigating'],
                    priority: ['high', 'critical'],
                    createdAt: { [Op.lt]: fourHoursAgo }
                },
                include: [{ model: Site }]
            });

            for (const inc of urgentIncidents) {
                console.log(`🔥 URGENT INCIDENT REMINDER: #${inc.id} - ${inc.type}`);
                io.to(`site_${inc.siteId}`).to('manager').to('admin').emit('incident_reminder', {
                    type: 'urgent',
                    incidentId: inc.id,
                    incidentType: inc.type,
                    priority: inc.priority,
                    message: `CRITICAL: Incident #${inc.id} remains unresolved after 4 hours.`
                });
            }

            // 2. Find ANY unresolved incidents older than 24 hours
            const staleIncidents = await Incident.findAll({
                where: {
                    status: ['new', 'investigating'],
                    createdAt: { [Op.lt]: twentyFourHoursAgo }
                },
                include: [{ model: Site }]
            });

            for (const inc of staleIncidents) {
                console.log(`⚠️  STALE INCIDENT REMINDER: #${inc.id} - ${inc.type}`);
                io.to(`site_${inc.siteId}`).to('manager').to('admin').emit('incident_reminder', {
                    type: 'stale',
                    incidentId: inc.id,
                    incidentType: inc.type,
                    message: `Reminder: Incident #${inc.id} has been open for more than 24 hours.`
                });
            }

        } catch (err) {
            console.error('Error in Incident Reminders:', err);
        }
    });
};

module.exports = startIncidentReminders;
