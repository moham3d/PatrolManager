const cron = require('node-cron');
const { Shift, Attendance, Alert, sequelize } = require('../models');
const db = require('../models'); 
const { Op } = require('sequelize');

const startMonitoring = (io) => {
    console.log('🕰️  Attendance Monitor Started');

    // Run every 15 minutes
    cron.schedule('*/15 * * * *', async () => {
        try {
            const now = new Date();
            const fifteenMinsAgo = new Date(now.getTime() - 15 * 60000);
            const oneHourAgo = new Date(now.getTime() - 60 * 60000);

            // 1. Find LATE shifts (Started > 15 mins ago, no clock-in)
            const lateShifts = await Shift.findAll({
                where: {
                    startTime: { [Op.lt]: fifteenMinsAgo },
                    endTime: { [Op.gt]: now },
                    status: 'scheduled'
                },
                include: ['user', 'site']
            });

            for (const shift of lateShifts) {
                // Check for alert persistence
                const existingAlert = await Alert.findOne({
                    where: {
                        type: 'late_arrival',
                        status: 'new',
                        'metadata.shiftId': shift.id
                    }
                });

                if (existingAlert) continue; // Alert already exists

                console.log(`⚠️  LATE ALERT: ${shift.user.name} at ${shift.site.name}`);

                // Persist Alert
                await Alert.create({
                    type: 'late_arrival',
                    message: `Guard ${shift.user.name} has not clocked in for their shift at ${shift.site.name}.`,
                    siteId: shift.siteId,
                    userId: shift.userId,
                    metadata: { shiftId: shift.id, expectedStartTime: shift.startTime }
                });

                io.to(`site_${shift.siteId}`).to('admin').emit('late_arrival_alert', {
                    message: `Late Arrival: ${shift.user.name}`,
                    site: shift.site.name,
                    expectedTime: shift.startTime
                });
            }

            // 2. Escalation Logic (Unresolved alerts > 1 hour)
            const oldAlerts = await Alert.findAll({
                where: {
                    status: 'new',
                    createdAt: { [Op.lt]: oneHourAgo }
                }
            });

            for (const alert of oldAlerts) {
                console.log(`🔥 ESCALATING ALERT: ${alert.id}`);
                // Notify all managers/admins
                io.to('manager').to('admin').emit('alert_escalation', {
                    alertId: alert.id,
                    type: alert.type,
                    message: `ESCALATION: ${alert.message}`
                });
            }

        } catch (err) {
            console.error('Error in Attendance Monitor:', err);
        }
    });
};

module.exports = startMonitoring;
