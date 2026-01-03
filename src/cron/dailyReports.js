const cron = require('node-cron');
const { ReportSchedule, Incident, PatrolRun, Shift, Site, User } = require('../models');
const { Op } = require('sequelize');
const transporter = require('../config/mail');
const { Parser } = require('json2csv');

const startDailyReports = () => {
    console.log('📊 Daily Reports Job Started');

    // Run daily at 6 AM
    cron.schedule('0 6 * * *', async () => {
        try {
            console.log('--- Generating Daily Reports ---');
            
            const schedules = await ReportSchedule.findAll({
                where: { frequency: 'daily', isActive: true }
            });

            for (const schedule of schedules) {
                await processSchedule(schedule);
            }

        } catch (err) {
            console.error('Error in Daily Reports Job:', err);
        }
    });
};

const processSchedule = async (schedule) => {
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    yesterday.setHours(0, 0, 0, 0);
    
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    let data = [];
    let filename = `report_${schedule.reportType}_${yesterday.toISOString().split('T')[0]}.csv`;

    if (schedule.reportType === 'incident') {
        const incidents = await Incident.findAll({
            where: { createdAt: { [Op.between]: [yesterday, today] } },
            include: [{ model: Site }]
        });
        data = incidents.map(i => ({
            id: i.id,
            type: i.type,
            priority: i.priority,
            status: i.status,
            site: i.Site ? i.Site.name : 'N/A',
            description: i.description
        }));
    } else if (schedule.reportType === 'patrol') {
        const runs = await PatrolRun.findAll({
            where: { startTime: { [Op.between]: [yesterday, today] } },
            include: [{ model: Site }, { model: User, as: 'guard' }]
        });
        data = runs.map(r => ({
            id: r.id,
            guard: r.guard ? r.guard.name : 'N/A',
            site: r.Site ? r.Site.name : 'N/A',
            status: r.status,
            completion: r.completionPercentage + '%'
        }));
    }

    if (data.length > 0) {
        const parser = new Parser();
        const csv = parser.parse(data);

        await transporter.sendMail({
            from: '"PatrolShield Reports" <reports@patrolshield.com>',
            to: schedule.email,
            subject: `Daily ${schedule.reportType} Report - ${yesterday.toDateString()}`,
            text: `Attached is your scheduled daily ${schedule.reportType} report.`,
            attachments: [{ filename, content: csv }]
        });

        await schedule.update({ lastRun: new Date() });
        console.log(`Sent ${schedule.reportType} report to ${schedule.email}`);
    }
};

module.exports = startDailyReports;
