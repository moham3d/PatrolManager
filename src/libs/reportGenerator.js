const { PatrolRun, Incident, Attendance, User, Site } = require('../models');
const { Op } = require('sequelize');

exports.generateSiteReport = async (siteId, date) => {
    const startOfDay = new Date(date);
    startOfDay.setHours(0, 0, 0, 0);

    const endOfDay = new Date(date);
    endOfDay.setHours(23, 59, 59, 999);

    const site = await Site.findByPk(siteId);

    const patrols = await PatrolRun.count({
        where: {
            siteId,
            startTime: { [Op.between]: [startOfDay, endOfDay] },
            status: 'completed'
        }
    });

    const incidents = await Incident.findAll({
        where: {
            siteId,
            createdAt: { [Op.between]: [startOfDay, endOfDay] }
        }
    });

    const attendance = await Attendance.findAll({
        where: {
            siteId,
            timestamp: { [Op.between]: [startOfDay, endOfDay] },
            type: 'clock_in'
        },
        include: ['user']
    });

    // In a real app, we would generate a PDF here using libraries like pdfkit or puppeteer.
    // For this MVP, we return a structured JSON object that the frontend can render or print.

    return {
        meta: {
            generatedAt: new Date(),
            siteName: site.name,
            reportDate: date
        },
        stats: {
            completedPatrols: patrols,
            totalIncidents: incidents.length,
            guardsOnDuty: attendance.length
        },
        incidents: incidents.map(i => ({ type: i.type, status: i.status, description: i.description })),
        attendance: attendance.map(a => ({ guard: a.user.name, time: a.timestamp }))
    };
};
