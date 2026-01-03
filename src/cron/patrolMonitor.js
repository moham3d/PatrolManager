const cron = require('node-cron');
const { PatrolRun, PatrolTemplate, User, Site } = require('../models');
const { Op } = require('sequelize');

const startPatrolMonitoring = (io) => {
    console.log('🚶 Patrol Monitor Started');

    // Run every 10 minutes
    cron.schedule('*/10 * * * *', async () => {
        try {
            const now = new Date();
            
            // 1. Find active patrols that have exceeded their estimated duration
            const stalePatrols = await PatrolRun.findAll({
                where: {
                    status: 'started',
                    startTime: { 
                        [Op.lt]: new Date(now.getTime() - 60 * 60000) // Default 1 hour timeout for staleness
                    }
                },
                include: [
                    { model: PatrolTemplate, as: 'template' },
                    { model: User, as: 'guard' },
                    { model: Site, as: 'site' }
                ]
            });

            for (const run of stalePatrols) {
                const duration = run.template ? run.template.estimatedDurationMinutes : 30;
                const timeoutThreshold = duration * 2; // Alert if double the estimated time

                const startTime = new Date(run.startTime);
                const diffMinutes = Math.floor((now - startTime) / 60000);

                if (diffMinutes > timeoutThreshold) {
                    console.log(`⚠️  PATROL TIMEOUT: Run #${run.id} by ${run.guard.name} is taking too long.`);
                    
                    // Alert supervisors of the site
                    io.to(`site_${run.siteId}`).to('supervisor').to('manager').emit('patrol_alert', {
                        type: 'timeout',
                        runId: run.id,
                        guardName: run.guard.name,
                        siteName: run.site.name,
                        message: `Patrol #${run.id} exceeded estimated time by ${diffMinutes - duration} minutes.`
                    });
                }
            }

            // 2. Find missed patrols (templates that should have started but didn't)
            // This would require a more complex scheduling system for templates.
            // For MVP, we'll focus on stale active runs.

        } catch (err) {
            console.error('Error in Patrol Monitor:', err);
        }
    });
};

module.exports = startPatrolMonitoring;
