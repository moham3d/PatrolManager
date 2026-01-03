const { PatrolRun, PatrolTemplate, CheckpointVisit, Incident, User, Site, Shift, ReportSchedule } = require('../models');
const { Op } = require('sequelize');
const { Parser } = require('json2csv');
const ExcelJS = require('exceljs');

// Helpers
const renderOrJson = (res, view, data) => {
    res.format({
        'text/html': () => res.render(view, data),
        'application/json': () => res.json(data)
    });
};

exports.index = async (req, res) => {
    try {
        const schedules = await ReportSchedule.findAll({
            where: { userId: req.user.id }
        });
        res.render('reports/index', { title: 'Analytics Dashboard', schedules });
    } catch (err) {
        console.error(err);
        res.status(500).send('Server Error');
    }
};

exports.createSchedule = async (req, res) => {
    try {
        const { reportType, frequency, email } = req.body;
        await ReportSchedule.create({
            userId: req.user.id,
            reportType,
            frequency,
            email,
            createdBy: req.user.id
        });
        req.flash('success', 'Report scheduled successfully');
        res.redirect('/reports');
    } catch (err) {
        console.error(err);
        req.flash('error', 'Error scheduling report');
        res.redirect('/reports');
    }
};

exports.deleteSchedule = async (req, res) => {
    try {
        const schedule = await ReportSchedule.findByPk(req.params.id);
        if (schedule && (schedule.userId === req.user.id || req.user.Role.name === 'admin')) {
            await schedule.destroy();
            req.flash('success', 'Schedule deleted');
        }
        res.redirect('/reports');
    } catch (err) {
        console.error(err);
        res.redirect('/reports');
    }
};

exports.exportIncidents = async (req, res) => {
    try {
        const { format, startDate, endDate } = req.query;
        let where = {};
        if (startDate && endDate) {
            where.createdAt = { [Op.between]: [new Date(startDate), new Date(endDate)] };
        }

        const incidents = await Incident.findAll({
            where,
            include: [{ model: User, as: 'reporter' }, { model: Site }],
            order: [['createdAt', 'DESC']]
        });

        const data = incidents.map(inc => ({
            ID: inc.id,
            Type: inc.type,
            Priority: inc.priority,
            Status: inc.status,
            Site: inc.Site ? inc.Site.name : 'Unknown',
            Reporter: inc.reporter ? inc.reporter.name : 'Unknown',
            Date: inc.createdAt.toISOString(),
            Description: inc.description
        }));

        if (format === 'csv') {
            const json2csvParser = new Parser();
            const csv = json2csvParser.parse(data);
            res.header('Content-Type', 'text/csv');
            res.attachment('incidents_report.csv');
            return res.send(csv);
        } else if (format === 'xlsx') {
            const workbook = new ExcelJS.Workbook();
            const worksheet = workbook.addWorksheet('Incidents');
            
            worksheet.columns = [
                { header: 'ID', key: 'ID', width: 10 },
                { header: 'Type', key: 'Type', width: 15 },
                { header: 'Priority', key: 'Priority', width: 10 },
                { header: 'Status', key: 'Status', width: 15 },
                { header: 'Site', key: 'Site', width: 20 },
                { header: 'Reporter', key: 'Reporter', width: 20 },
                { header: 'Date', key: 'Date', width: 25 },
                { header: 'Description', key: 'Description', width: 40 }
            ];

            worksheet.addRows(data);

            res.header('Content-Type', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
            res.attachment('incidents_report.xlsx');
            await workbook.xlsx.write(res);
            return res.end();
        }

        res.status(400).json({ error: 'Invalid format' });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};

exports.getPatrolAnalytics = async (req, res) => {
    try {
        const runs = await PatrolRun.findAll({
            where: { status: ['completed', 'incomplete'] },
            attributes: ['completionPercentage', 'siteId', 'createdAt'],
            include: [{ model: Site }]
        });

        const siteStats = {};
        runs.forEach(run => {
            const siteName = run.Site ? run.Site.name : 'Unknown';
            if (!siteStats[siteName]) {
                siteStats[siteName] = { total: 0, sum: 0 };
            }
            siteStats[siteName].total++;
            siteStats[siteName].sum += (run.completionPercentage || 0);
        });

        const completionBySite = {};
        Object.keys(siteStats).forEach(site => {
            completionBySite[site] = Math.round(siteStats[site].sum / siteStats[site].total);
        });

        res.json({ completionBySite });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};

exports.getIncidentTrends = async (req, res) => {
    try {
        const incidents = await Incident.findAll({
            attributes: ['createdAt']
        });

        const hourlyTrends = Array(24).fill(0);
        incidents.forEach(inc => {
            const hour = new Date(inc.createdAt).getHours();
            hourlyTrends[hour]++;
        });

        res.json({ hourlyTrends });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};

exports.getMissedCheckpoints = async (req, res) => {
    try {
        const runs = await PatrolRun.findAll({
            where: {
                status: ['completed', 'incomplete']
            },
            include: [
                { model: PatrolTemplate, as: 'template' },
                { model: User, as: 'guard' },
                { model: CheckpointVisit, as: 'visits' }
            ],
            order: [['endTime', 'DESC']],
            limit: 50
        });

        const data = runs.map(run => {
            const expectedIds = (run.template && run.template.checkpointsList) ? run.template.checkpointsList : [];
            const visitedIds = run.visits.map(v => v.checkpointId);

            const totalExpected = expectedIds.length;
            const totalVisited = new Set(visitedIds).size;

            const completionPercent = totalExpected > 0 ? Math.round((totalVisited / totalExpected) * 100) : 0;

            return {
                runId: run.id,
                guard: run.guard.name,
                template: run.template.name,
                date: run.endTime,
                totalExpected,
                totalVisited,
                completionPercent,
                status: run.status
            };
        }).filter(r => r.completionPercent < 100);

        res.json(data);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};

exports.getIncidentSummary = async (req, res) => {
    try {
        const incidents = await Incident.findAll({
            attributes: ['type', 'priority']
        });

        const byType = {};
        const byPriority = {};

        incidents.forEach(inc => {
            byType[inc.type] = (byType[inc.type] || 0) + 1;
            byPriority[inc.priority] = (byPriority[inc.priority] || 0) + 1;
        });

        res.json({ byType, byPriority });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};

exports.getShiftAnalytics = async (req, res) => {
    try {
        const shifts = await Shift.findAll({
            where: {
                status: 'completed',
                endTime: { [Op.ne]: null }
            },
            include: [
                { model: Site, as: 'site' },
                {
                    model: PatrolRun,
                    as: 'patrolRuns',
                    required: false
                },
                { model: User, as: 'user' }
            ],
            limit: 100,
            order: [['endTime', 'DESC']]
        });

        const siteHours = {};
        const guardPerformance = {};

        shifts.forEach(shift => {
            if (shift.site) {
                const durationMs = new Date(shift.endTime) - new Date(shift.startTime);
                const durationHours = durationMs / (1000 * 60 * 60);
                siteHours[shift.site.name] = (siteHours[shift.site.name] || 0) + durationHours;
            }

            const guardName = shift.user ? shift.user.name : 'Unknown';
            if (!guardPerformance[guardName]) {
                guardPerformance[guardName] = { shifts: 0, patrols: 0 };
            }
            guardPerformance[guardName].shifts += 1;
            guardPerformance[guardName].patrols += shift.patrolRuns.length;
        });

        Object.keys(siteHours).forEach(site => {
            siteHours[site] = Math.round(siteHours[site] * 10) / 10;
        });

        res.json({ siteHours, guardPerformance });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};