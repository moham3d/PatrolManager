const logger = require('../config/logger').logger;

class CronRegistry {
    constructor() {
        this.jobs = new Map();
        this.status = 'stopped';
    }

    register(name, startFn) {
        this.jobs.set(name, {
            start: startFn,
            lastRun: null,
            lastSuccess: null,
            lastFailure: null,
            failureCount: 0,
            isRunning: false
        });
        logger.info(`Cron job registered: ${name}`);
    }

    async startAll(io) {
        logger.info('Starting all registered cron jobs');
        for (const [name, jobInfo] of this.jobs) {
            try {
                jobInfo.start(io);
                jobInfo.isRunning = true;
                logger.info(`Cron job started: ${name}`);
            } catch (error) {
                logger.error(`Failed to start cron job: ${name}`, { error: error.message });
            }
        }
        this.status = 'running';
    }

    updateStatus(name, status, error = null) {
        const job = this.jobs.get(name);
        if (job) {
            job.lastRun = new Date();
            if (status === 'success') {
                job.lastSuccess = new Date();
                job.failureCount = 0;
            } else {
                job.lastFailure = new Date();
                job.failureCount++;
                job.lastError = error;
            }
        }
    }

    getStatus() {
        return Array.from(this.jobs.entries()).map(([name, info]) => ({
            name,
            lastRun: info.lastRun,
            lastSuccess: info.lastSuccess,
            lastFailure: info.lastFailure,
            failureCount: info.failureCount,
            isRunning: info.isRunning
        }));
    }
}

module.exports = new CronRegistry();
