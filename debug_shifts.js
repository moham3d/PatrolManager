const { Shift, User, Site } = require('./src/models');
const { Op } = require('sequelize');

async function debugShifts() {
    try {
        // Fetch all shifts
        const shifts = await Shift.findAll({
            include: ['user', 'site'],
            order: [['startTime', 'DESC']]
        });

        console.log(`Found ${shifts.length} total shifts.`);
        
        shifts.forEach(s => {
            console.log(`\n--- Shift #${s.id} ---`);
            console.log(`User: ${s.user ? s.user.name : 'Unknown'} (ID: ${s.userId})`);
            console.log(`Site: ${s.site ? s.site.name : 'Unknown'}`);
            console.log(`Status: ${s.status}`);
            console.log(`Start Time: ${s.startTime.toISOString()} (${s.startTime.toLocaleString()})`);
            console.log(`End Time: ${s.endTime ? s.endTime.toISOString() : 'N/A'}`);
            
            const now = new Date();
            console.log(`Current Server Time: ${now.toISOString()}`);
            
            if (s.status === 'active') {
                console.log('=> ACTIVE SHIFT');
            } else if (s.status === 'scheduled') {
                if (s.startTime > now) {
                    console.log('=> UPCOMING (Scheduled in future)');
                } else {
                    console.log('=> PAST DUE (Scheduled but start time passed)');
                }
            }
        });

    } catch (err) {
        console.error("Error:", err);
    }
}

debugShifts();
