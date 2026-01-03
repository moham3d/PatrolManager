const cron = require('node-cron');
const fs = require('fs').promises;
const path = require('path');
const { Incident, IncidentEvidence } = require('../models');
const { Op } = require('sequelize');

const startCleanup = () => {
    console.log('🧹 Cleanup Job Started');

    // Run daily at 3 AM
    cron.schedule('0 3 * * *', async () => {
        try {
            console.log('--- Starting Daily Cleanup ---');
            
            // 1. Cleanup old evidence (older than 90 days)
            const ninetyDaysAgo = new Date();
            ninetyDaysAgo.setDate(ninetyDaysAgo.getDate() - 90);

            const oldIncidents = await Incident.findAll({
                where: {
                    status: 'resolved',
                    updatedAt: { [Op.lt]: ninetyDaysAgo }
                }
            });

            console.log(`Cleaning up ${oldIncidents.length} old resolved incidents' evidence.`);

            const uploadDir = path.join(__dirname, '../public/uploads/incidents');

            for (const inc of oldIncidents) {
                if (inc.evidencePath) {
                    try {
                        const fullPath = path.join(__dirname, '../public', inc.evidencePath);
                        await fs.unlink(fullPath);
                        console.log(`Deleted: ${fullPath}`);
                        await inc.update({ evidencePath: null });
                    } catch (e) {
                        console.error(`Failed to delete file for incident ${inc.id}: ${e.message}`);
                    }
                }
            }

            // 2. Cleanup orphaned files in uploads directory
            const files = await fs.readdir(uploadDir);
            const dbIncidents = await Incident.findAll({
                attributes: ['evidencePath'],
                where: { evidencePath: { [Op.ne]: null } }
            });
            const activePaths = new Set(dbIncidents.map(i => path.basename(i.evidencePath)));

            let orphanedCount = 0;
            for (const file of files) {
                if (!activePaths.has(file)) {
                    await fs.unlink(path.join(uploadDir, file));
                    orphanedCount++;
                }
            }
            console.log(`Deleted ${orphanedCount} orphaned files.`);

        } catch (err) {
            console.error('Error in Cleanup Job:', err);
        }
    });
};

module.exports = startCleanup;
