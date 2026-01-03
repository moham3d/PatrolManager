const db = require('../src/models');

(async () => {
    try {
        await db.sequelize.authenticate();
        console.log('Database connection established.');
        
        await db.sequelize.sync({ force: false });
        console.log('Database synchronized.');
        
        process.exit(0);
    } catch (err) {
        console.error('Sync failed:', err);
        process.exit(1);
    }
})();
