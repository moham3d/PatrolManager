const db = require('../src/models');
const migration = require('../migrations/20260208010000-add-permission-slug.js');

async function runMigration() {
    try {
        console.log('🚀 Running migration: add-permission-slug...');
        const queryInterface = db.sequelize.getQueryInterface();
        const Sequelize = db.sequelize.constructor;

        await migration.up(queryInterface, Sequelize);
        console.log('✅ Migration complete!');

        await db.sequelize.close();
        process.exit(0);
    } catch (error) {
        console.error('❌ Migration failed:', error);
        process.exit(1);
    }
}

runMigration();
