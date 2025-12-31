const { Sequelize, DataTypes } = require('sequelize');
const db = require('../src/models');
require('dotenv').config();

const args = process.argv.slice(2);
const command = args[0];

async function syncDB(force = false) {
    try {
        console.log(`🔄 Syncing database (Force: ${force})...`);
        await db.sequelize.sync({ force: force, alter: !force });
        console.log('✅ Database synced successfully.');
    } catch (err) {
        console.error('❌ Error syncing database:', err);
    }
}

async function addSiteIdColumn() {
    try {
        console.log('🛠 Adding siteId column to PatrolRuns...');
        const queryInterface = db.sequelize.getQueryInterface();
        await queryInterface.addColumn('PatrolRuns', 'siteId', {
            type: DataTypes.INTEGER,
            allowNull: true,
            references: {
                model: 'Sites',
                key: 'id'
            },
            onUpdate: 'CASCADE',
            onDelete: 'SET NULL'
        });
        console.log('✅ Column siteId added to PatrolRuns successfully.');
    } catch (error) {
        console.error('❌ Error adding column:', error.message);
    }
}

async function main() {
    if (command === 'reset') {
        await syncDB(true);
    } else if (command === 'sync') {
        await syncDB(false);
    } else if (command === 'migrate:siteid') {
        await addSiteIdColumn();
    } else {
        console.log(`
Usage: node scripts/db_manager.js <command>

Commands:
  sync             Sync database schema (SAFE, uses alter)
  reset            Drop and recreate database schema (DESTRUCTIVE)
  migrate:siteid   Add siteId column to PatrolRuns
        `);
    }
    await db.sequelize.close();
}

main();
