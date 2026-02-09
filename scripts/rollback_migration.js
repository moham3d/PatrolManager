const db = require('../src/models');
const migration = require('../migrations/20260208010000-add-permission-slug.js');

async function rollbackMigration() {
    try {
        console.log('⏪ Rolling back migration: add-permission-slug...');
        const queryInterface = db.sequelize.getQueryInterface();
        const Sequelize = db.sequelize.constructor;

        await migration.down(queryInterface, Sequelize);
        console.log('✅ Rollback complete!');

        await db.sequelize.close();
        process.exit(0);
    } catch (error) {
        console.error('❌ Rollback failed (might not exist):', error.message);
        await db.sequelize.close();
        process.exit(0);
    }
}

rollbackMigration();
