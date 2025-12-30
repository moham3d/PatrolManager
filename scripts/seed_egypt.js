const db = require('../src/models');
const bcrypt = require('bcryptjs');

async function seed() {
    try {
        console.log('🔄 Resetting Database...');
        await db.sequelize.sync({ force: true });
        console.log('✅ Database Cleaned & Synced.');

        // ==========================================
        // 0. ROLES & PERMISSIONS
        // ==========================================
        console.log('🛡️ Creating Roles & Permissions...');

        // Define Roles
        const roleAdmin = await db.Role.create({ name: 'admin', description: 'Super User' });
        const roleManager = await db.Role.create({ name: 'manager', description: 'Site Manager' });
        const roleSupervisor = await db.Role.create({ name: 'supervisor', description: 'Field Supervisor' });
        const roleGuard = await db.Role.create({ name: 'guard', description: 'Patrol Officer' });

        // Define Permissions
        const perms = [
            'user_view', 'user_create', 'user_edit', 'user_delete',
            'site_view', 'site_create', 'site_edit', 'site_delete',
            'report_view'
        ];

        for (const p of perms) {
            await db.Permission.create({ name: p });
        }

        // Assign Permissions (Manager gets most, Admin gets implicit all via bypass, Guard gets none/few)
        const allPerms = await db.Permission.findAll();
        await roleManager.addPermissions(allPerms); // Give Manager everything for now

        // ==========================================
        // 1. USERS
        // ==========================================
        console.log('👤 Creating Users...');
        const passwordPlain = 'password123';

        const admin = await db.User.create({
            name: 'Sherif The Admin',
            email: 'admin@patrol.eg',
            password: passwordPlain,
            roleId: roleAdmin.id,
            isActive: true
        });

        const manager = await db.User.create({
            name: 'Tarek The Manager',
            email: 'manager@patrol.eg',
            password: passwordPlain,
            roleId: roleManager.id,
            isActive: true
        });

        const supervisor = await db.User.create({
            name: 'Sameh Supervisor',
            email: 'supervisor@patrol.eg',
            password: passwordPlain,
            roleId: roleSupervisor.id,
            managerId: manager.id,
            isActive: true
        });

        const guardAhmed = await db.User.create({
            name: 'Ahmed Guard',
            email: 'ahmed@patrol.eg',
            password: passwordPlain,
            roleId: roleGuard.id,
            managerId: manager.id, // Hierarchy: Reports to Tarek
            isActive: true
        });

        const guardMahmoud = await db.User.create({
            name: 'Mahmoud Guard',
            email: 'mahmoud@patrol.eg',
            password: passwordPlain,
            roleId: roleGuard.id,
            managerId: manager.id, // Hierarchy: Reports to Tarek
            isActive: true
        });

        // ==========================================
        // 2. SITES & ASSETS
        // ==========================================
        console.log('🏢 Creating Sites & Assets...');

        // Site 1: Cairo Festival City
        const cfc = await db.Site.create({
            name: 'Cairo Festival City',
            address: 'Ring Road, New Cairo, Cairo, Egypt',
            lat: 30.0315,
            lng: 31.4055,
            details: 'Large mall and business district.'
        });

        // Assign Staff to CFC
        await cfc.addStaff([manager, guardAhmed]);

        const cfcZoneA = await db.Zone.create({ name: 'Mall Entrance', siteId: cfc.id });
        const cfcZoneB = await db.Zone.create({ name: 'Parking B2', siteId: cfc.id });

        const cp1 = await db.Checkpoint.create({ name: 'Main Gate Security', type: 'nfc', uid: 'NFC_001', zoneId: cfcZoneA.id, siteId: cfc.id, lat: 30.0316, lng: 31.4056 });
        const cp2 = await db.Checkpoint.create({ name: 'Loading Dock', type: 'qr', zoneId: cfcZoneA.id, siteId: cfc.id });
        const cp3 = await db.Checkpoint.create({ name: 'VIP Parking', type: 'qr', zoneId: cfcZoneB.id, siteId: cfc.id });

        // Site 2: Smart Village
        const smartVillage = await db.Site.create({
            name: 'Smart Village',
            address: 'Cairo-Alex Desert Road, Giza, Egypt',
            lat: 30.0710,
            lng: 31.0213,
            details: 'Technology business park.'
        });

        // Assign Staff to Smart Village
        await smartVillage.addStaff([manager, guardMahmoud]);

        const svZoneServer = await db.Zone.create({ name: 'Server Farm', siteId: smartVillage.id });
        const cpSV1 = await db.Checkpoint.create({ name: 'Rack Row A', type: 'nfc', uid: 'NFC_SV_01', zoneId: svZoneServer.id, siteId: smartVillage.id });

        // ==========================================
        // 3. PATROL TEMPLATES
        // ==========================================
        console.log('📋 Creating Templates...');

        const templateCFC = await db.PatrolTemplate.create({
            name: 'CFC Morning Routine',
            description: 'Check main gates and parking.',
            checkpointsList: [cp1.id, cp2.id, cp3.id],
            estimatedDuration: 30,
            siteId: cfc.id
        });

        const templateSV = await db.PatrolTemplate.create({
            name: 'Server Room Hourly',
            description: 'Temp check and physical lock check.',
            checkpointsList: [cpSV1.id],
            estimatedDuration: 10,
            siteId: smartVillage.id // usage depends on schema, some schemas might not link Template to Site directly but assume checks
        });

        // ==========================================
        // 4. SHIFTS (Unified Workflow)
        // ==========================================
        console.log('ki Creating Completed Shifts...');

        // Helper: Create a shift in the past
        const createPastShift = async (user, site, daysAgo, hoursDuration = 8) => {
            const start = new Date();
            start.setDate(start.getDate() - daysAgo);
            start.setHours(8, 0, 0, 0); // 8 AM

            const end = new Date(start);
            end.setHours(start.getHours() + hoursDuration);

            return db.Shift.create({
                userId: user.id,
                siteId: site.id,
                startTime: start,
                endTime: end,
                status: 'completed',
                startLocation: { lat: site.lat, lng: site.lng },
                endLocation: { lat: site.lat, lng: site.lng }
            });
        };

        // Create 5 Shifts for Ahmed at CFC (Productive)
        for (let i = 5; i >= 1; i--) {
            const shift = await createPastShift(guardAhmed, cfc, i);

            // Create 2 patrols per shift
            const p1 = await db.PatrolRun.create({
                templateId: templateCFC.id,
                guardId: guardAhmed.id,
                shiftId: shift.id,
                siteId: cfc.id,
                startTime: new Date(shift.startTime.getTime() + 1000 * 60 * 30), // 30 mins in
                endTime: new Date(shift.startTime.getTime() + 1000 * 60 * 60),
                status: 'completed'
            });

            // Add visits
            await db.CheckpointVisit.create({ patrolRunId: p1.id, checkpointId: cp1.id, scannedAt: p1.startTime, location: { lat: cfc.lat, lng: cfc.lng } });
            await db.CheckpointVisit.create({ patrolRunId: p1.id, checkpointId: cp2.id, scannedAt: p1.endTime, location: { lat: cfc.lat, lng: cfc.lng } });

            // Second patrol
            await db.PatrolRun.create({
                templateId: templateCFC.id,
                guardId: guardAhmed.id,
                shiftId: shift.id,
                siteId: cfc.id,
                startTime: new Date(shift.startTime.getTime() + 1000 * 60 * 240), // 4 hours in
                endTime: new Date(shift.startTime.getTime() + 1000 * 60 * 270),
                status: 'completed'
            });
        }

        // Create 1 Shift for Mahmoud at Smart Village (Lazy / Incident)
        const svShift = await createPastShift(guardMahmoud, smartVillage, 1);

        // Incomplete Patrol
        const pBad = await db.PatrolRun.create({
            templateId: templateSV.id,
            guardId: guardMahmoud.id,
            shiftId: svShift.id,
            siteId: smartVillage.id,
            startTime: new Date(svShift.startTime.getTime() + 1000 * 60 * 60),
            endTime: new Date(svShift.startTime.getTime() + 1000 * 60 * 120),
            status: 'incomplete', // Missed checkpoints
            notes: 'Guard got distracted.'
        });

        // ==========================================
        // 5. INCIDENTS
        // ==========================================
        console.log('🚨 Creating Incidents...');

        await db.Incident.create({
            type: 'security',
            priority: 'high',
            description: 'Server room door found unlocked!',
            status: 'new', // Incident model default is 'new'
            reporterId: guardMahmoud.id, // Correct Key
            siteId: smartVillage.id,
            zoneId: svZoneServer.id, // Added Zone
            // patrolRunId: pBad.id, // Incident model doesn't explicitly have patrolRunId by default, check Incident.js. 
            // Checking Incident.js earlier, it didn't show patrolRunId in define, but let's check associations?
            // "Incident.associate" didn't show PatrolRun belongsTo. 
            // If strict, remove it unless strict foreign keys are off or it was added in a migration I missed.
            // Safe bet: remove it to avoid error if col doesn't exist, OR check model again. 
            // Model Incident.js showed: reporter, assignee, site, zone. NO patrolRun.
            // So removing patrolRunId is correct.
            createdAt: new Date()
        });

        console.log('✅ SEEDING COMPLETE!');
        console.log(`
        Credentials:
        Admin: admin@patrol.eg / password123
        Admin: admin@patrol.eg / password123
        Manager: manager@patrol.eg / password123
        Supervisor: supervisor@patrol.eg / password123
        Guard: ahmed@patrol.eg / password123
        `);

        process.exit(0);
    } catch (err) {
        console.error('❌ SEEDING FAILED:', err);
        process.exit(1);
    }
}

seed();
