const cron = require('node-cron');
const { Shift, User, Site } = require('../models');
const { Op } = require('sequelize');

const startReminders = (io) => {
    console.log('⏰ Shift Reminders Started');

    // Run every 5 minutes
    cron.schedule('*/5 * * * *', async () => {
        try {
            const now = new Date();
            const fifteenMinsFromNow = new Date(now.getTime() + 15 * 60000);
            const twentyMinsFromNow = new Date(now.getTime() + 20 * 60000);

            // 1. Remind guards 15 mins BEFORE shift start
            const upcomingShifts = await Shift.findAll({
                where: {
                    startTime: { [Op.between]: [fifteenMinsFromNow, twentyMinsFromNow] },
                    status: 'scheduled'
                },
                include: ['user', 'site']
            });

            for (const shift of upcomingShifts) {
                if (shift.user) {
                    console.log(`🔔 Sending start reminder to ${shift.user.name}`);
                    io.to(`user_${shift.user.id}`).emit('shift_reminder', {
                        type: 'start',
                        siteName: shift.site.name,
                        time: shift.startTime,
                        message: `Your shift at ${shift.site.name} starts in 15 minutes.`
                    });
                }
            }

            // 2. Remind guards 15 mins BEFORE shift ends
            const endingShifts = await Shift.findAll({
                where: {
                    endTime: { [Op.between]: [fifteenMinsFromNow, twentyMinsFromNow] },
                    status: 'active'
                },
                include: ['user', 'site']
            });

            for (const shift of endingShifts) {
                if (shift.user) {
                    console.log(`🔔 Sending end reminder to ${shift.user.name}`);
                    io.to(`user_${shift.user.id}`).emit('shift_reminder', {
                        type: 'end',
                        siteName: shift.site.name,
                        time: shift.endTime,
                        message: `Your shift at ${shift.site.name} ends in 15 minutes. Please complete your tasks.`
                    });
                }
            }

        } catch (err) {
            console.error('Error in Shift Reminders:', err);
        }
    });
};

module.exports = startReminders;
