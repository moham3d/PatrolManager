const db = require('../src/models');

(async () => {
    try {
        await db.sequelize.authenticate();
        console.log('Database connection established.');
        
        const queryInterface = db.sequelize.getQueryInterface();

        const indexes = [
            { table: 'PatrolRuns', fields: ['siteId'], name: 'patrol_runs_site_id' },
            { table: 'PatrolRuns', fields: ['guardId'], name: 'patrol_runs_guard_id' },
            { table: 'PatrolRuns', fields: ['status'], name: 'patrol_runs_status' },
            { table: 'PatrolRuns', fields: ['startTime'], name: 'patrol_runs_start_time' },
            { table: 'Incidents', fields: ['siteId'], name: 'incidents_site_id' },
            { table: 'Incidents', fields: ['status'], name: 'incidents_status' },
            { table: 'Incidents', fields: ['priority'], name: 'incidents_priority' },
            { table: 'Shifts', fields: ['userId'], name: 'shifts_user_id' },
            { table: 'Shifts', fields: ['siteId'], name: 'shifts_site_id' },
            { table: 'Shifts', fields: ['status'], name: 'shifts_status' },
            { table: 'Checkpoints', fields: ['siteId'], name: 'checkpoints_site_id' },
            { table: 'Checkpoints', fields: ['uid'], name: 'checkpoints_uid' },
            { table: 'AuditLogs', fields: ['userId'], name: 'audit_logs_user_id' },
            { table: 'AuditLogs', fields: ['timestamp'], name: 'audit_logs_timestamp' },
            { table: 'GPSLogs', fields: ['userId'], name: 'gps_logs_user_id' },
            { table: 'GPSLogs', fields: ['patrolRunId'], name: 'gps_logs_patrol_run_id' },
            { table: 'GPSLogs', fields: ['timestamp'], name: 'gps_logs_timestamp' }
        ];

        for (const idx of indexes) {
            try {
                await queryInterface.addIndex(idx.table, idx.fields, { name: idx.name });
                console.log(`Index ${idx.name} added to ${idx.table}`);
            } catch (e) {
                console.log(`Index ${idx.name} already exists or failed: ${e.message}`);
            }
        }
        
        console.log('Index optimization complete.');
        process.exit(0);
    } catch (err) {
        console.error('Optimization failed:', err);
        process.exit(1);
    }
})();
