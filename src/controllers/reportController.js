const { PatrolRun, PatrolTemplate, CheckpointVisit, Incident, User, Site, Shift } = require('../models');
const { Op } = require('sequelize');

exports.index = async (req, res) => {
    res.format({
        'text/html': () => res.render('reports/index', { title: 'Analytics Dashboard' }),
        'application/json': () => res.json({ message: 'Analytics API' })
    });
};

exports.getMissedCheckpoints = async (req, res) => {
    try {
        const runs = await PatrolRun.findAll({
            where: {
                status: ['completed', 'incomplete'] // Only finished runs
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

            // Find counts
            const totalExpected = expectedIds.length;
            const totalVisited = new Set(visitedIds).size; // Unique visits

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
        }).filter(r => r.completionPercent < 100); // Only return imperfect runs

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
            // Count Type
            byType[inc.type] = (byType[inc.type] || 0) + 1;
            // Count Priority
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
            // Metric 1: Hours by Site
            if (shift.site) {
                const durationMs = new Date(shift.endTime) - new Date(shift.startTime);
                const durationHours = durationMs / (1000 * 60 * 60);
                siteHours[shift.site.name] = (siteHours[shift.site.name] || 0) + durationHours;
            }

            // Metric 2: Guard Performance
            const guardName = shift.user ? shift.user.name : 'Unknown';
            if (!guardPerformance[guardName]) {
                guardPerformance[guardName] = { shifts: 0, patrols: 0 };
            }
            guardPerformance[guardName].shifts += 1;
            guardPerformance[guardName].patrols += shift.patrolRuns.length;
        });

        // Round hours
        Object.keys(siteHours).forEach(site => {
            siteHours[site] = Math.round(siteHours[site] * 10) / 10;
        });

        res.json({ siteHours, guardPerformance });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};
