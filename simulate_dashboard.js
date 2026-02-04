const { Shift, User, Site } = require('./src/models');
const { Op } = require('sequelize');

async function simulateDashboard() {
    try {
        const userId = 4; // Ahmed Guard
        const now = new Date();
        
        console.log(`[Sim] Checking for User ID: ${userId}`);
        console.log(`[Sim] Current Time: ${now.toISOString()}`);

        const nextShift = await Shift.findOne({
            where: {
                userId: userId,
                status: 'scheduled',
                endTime: { [Op.gt]: now }
            },
            order: [['startTime', 'ASC']],
            include: [{ model: Site, as: 'site' }]
        });

        if (nextShift) {
            console.log("✅ Found Shift!");
            console.log(`ID: ${nextShift.id}`);
            console.log(`Start: ${nextShift.startTime.toISOString()}`);
            console.log(`End: ${nextShift.endTime.toISOString()}`);
        } else {
            console.log("❌ No Shift Found.");
        }

    } catch (err) {
        console.error("Error:", err);
    }
}

simulateDashboard();
