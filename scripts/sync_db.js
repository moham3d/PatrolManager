const db = require('../src/models');

async function syncDB() {
    try {
        console.log('Syncing database...');
        await db.sequelize.sync({ alter: true }); // Use alter to add new tables/columns without dropping
        console.log('Database synced successfully.');
        process.exit(0);
    } catch (err) {
        console.error('Error syncing database:', err);
        process.exit(1);
    }
}

syncDB();
