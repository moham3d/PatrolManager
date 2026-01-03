const cron = require('node-cron');
const { ReportSchedule, Incident, PatrolRun, Site, User } = require('../models');
const { Op } = require('sequelize');
const transporter = require('../config/mail');
const { Parser } = require('json2csv');

const startWeeklyReports = () => {
    console.log('📅 Weekly Reports Job Started');

    // Run every Monday at 7 AM
    cron.schedule('0 7 * * 1', async () => {
        try {
            console.log('--- Generating Weekly Reports ---');
            
            const schedules = await ReportSchedule.findAll({
                where: { frequency: 'weekly', isActive: true }
            });

            for (const schedule of schedules) {
                await processWeeklySchedule(schedule);
            }

        } catch (err) {
            console.error('Error in Weekly Reports Job:', err);
        }
    });
};

const processWeeklySchedule = async (schedule) => {
    const weekAgo = new Date();
    weekAgo.setDate(weekAgo.getDate() - 7);
    weekAgo.setHours(0, 0, 0, 0);
    
    const now = new Date();

    let data = [];
    let filename = `weekly_${schedule.reportType}_${now.toISOString().split('T')[0]}.csv`;

    // Reusing some logic from daily but for a week range
    if (schedule.reportType === 'incident') {
        const incidents = await Incident.findAll({
            where: { createdAt: { [Op.between]: [weekAgo, now] } },
            include: [{ model: Site }]
        });
        data = incidents.map(i => ({
            id: i.id,
            type: i.type,
            priority: i.priority,
            status: i.status,
            site: i.Site ? i.Site.name : 'N/A',
            date: i.createdAt.toDateString()
        }));
    } else if (schedule.reportType === 'patrol') {
        const runs = await PatrolRun.findAll({
            where: { startTime: { [Op.between]: [weekAgo, now] } },
            include: [{ model: Site }, { model: User, as: 'guard' }]
        });
        data = runs.map(r => ({
            id: r.id,
            guard: r.guard ? r.guard.name : 'N/A',
            site: r.Site ? r.Site.name : 'N/A',
            status: r.status,
            date: r.startTime.toDateString()
        }));
    }

    if (data.length > 0) {
        const parser = new Parser();
        const csv = parser.parse(data);

        await transporter.sendMail({
            from: '"PatrolShield Reports" <reports@patrolshield.com>',
            to: schedule.email,
            subject: `Weekly ${schedule.reportType} Summary`,
            text: `Attached is your scheduled weekly ${schedule.reportType} summary report.`,
            attachments: [{ filename, content: csv }]
        });

        await schedule.update({ lastRun: new Date() });
        console.log(`Sent weekly ${schedule.reportType} report to ${schedule.email}`);
    }
};

module.exports = startWeeklyReports;
