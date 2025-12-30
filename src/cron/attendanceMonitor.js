const cron = require('node-cron');
const { Shift, Attendance, sequelize } = require('../models');
const db = require('../models'); // Access full db object for raw queries if needed
const { Op } = require('sequelize');

const startMonitoring = (io) => {
    console.log('🕰️  Attendance Monitor Started');

    // Run every 15 minutes
    cron.schedule('*/15 * * * *', async () => {
        try {
            const now = new Date();
            const fifteenMinsAgo = new Date(now.getTime() - 15 * 60000);

            // Find Shifts that started > 15 mins ago, but are NOT "completed"
            // and have NO "clock_in" attendance record.

            const lateShifts = await Shift.findAll({
                where: {
                    startTime: { [Op.lt]: fifteenMinsAgo },
                    endTime: { [Op.gt]: now },
                    status: 'scheduled'
                },
                include: ['user', 'site']
            });

            for (const shift of lateShifts) {
                // Check if they actually clocked in?
                const attendance = await db.Attendance.findOne({
                    where: {
                        shiftId: shift.id,
                        type: 'clock_in'
                    }
                });

                if (attendance) {
                    // They are here, maybe status wasn't updated?
                    // shift.status = 'active'; 
                    // await shift.save();
                    continue;
                }

                // If no clock-in, Emit Alert!
                console.log(`⚠️  LATE ALERT: ${shift.user.name} at ${shift.site.name}`);

                io.emit('late_arrival_alert', {
                    message: `Late Arrival: ${shift.user.name}`,
                    site: shift.site.name,
                    expectedTime: shift.startTime
                });
            }

        } catch (err) {
            console.error('Error in Attendance Monitor:', err);
        }
    });
};

module.exports = startMonitoring;
