const db = require('../src/models');
const { Role, Permission, User, Site, Zone, Checkpoint, PatrolTemplate, PatrolRun, CheckpointVisit, Incident } = db;
const bcrypt = require('bcryptjs');

const args = process.argv.slice(2);
const mode = args[0] || 'full'; // 'full', 'admin', 'basic'

async function seed() {
    try {
        console.log(`🌱 Seeding Database (Mode: ${mode})...`);

        // ========================
        // 0. COMMON SETUP (Roles)
        // ========================
        // Ensure Roles exist
        const roleAdmin = await Role.findOrCreate({ where: { name: 'admin' }, defaults: { description: 'Super User' } }).then(r => r[0]);
        const roleManager = await Role.findOrCreate({ where: { name: 'manager' }, defaults: { description: 'Site Manager' } }).then(r => r[0]);
        const roleSupervisor = await Role.findOrCreate({ where: { name: 'supervisor' }, defaults: { description: 'Field Supervisor' } }).then(r => r[0]);
        const roleGuard = await Role.findOrCreate({ where: { name: 'guard' }, defaults: { description: 'Patrol Officer' } }).then(r => r[0]);

        // Permissions
        const perms = [
            'user_view', 'user_create', 'user_edit', 'user_delete',
            'site_view', 'site_create', 'site_edit', 'site_delete',
            'report_view'
        ];
        // Only add if not exist slightly complex, let's just loop create
        for (const p of perms) {
            await Permission.findOrCreate({ where: { name: p } });
        }

        // Assign perms to manager
        const allPerms = await Permission.findAll();
        await roleManager.addPermissions(allPerms);

        // ========================
        // 1. ADMIN USER
        // ========================
        const passwordPlain = 'password123';
        const [admin] = await User.findOrCreate({
            where: { email: 'admin@patrol.eg' },
            defaults: {
                name: 'Sherif The Admin',
                password: passwordPlain,
                roleId: roleAdmin.id,
                isActive: true
            }
        });
        // Ensure password matches (in case it existed)
        admin.password = passwordPlain;
        await admin.save();
        console.log('👤 Admin Ready: admin@patrol.eg');

        if (mode === 'admin') {
            console.log('✅ Admin verification complete.');
            process.exit(0);
        }

        // ========================
        // 2. FULL DATASET (Egypt Scenario)
        // ========================

        // Managers & Guards
        const [manager] = await User.findOrCreate({
            where: { email: 'manager@patrol.eg' },
            defaults: {
                name: 'Tarek The Manager',
                password: passwordPlain,
                roleId: roleManager.id,
                isActive: true
            }
        });

        const [supervisor] = await User.findOrCreate({
            where: { email: 'supervisor@patrol.eg' },
            defaults: {
                name: 'Sameh Supervisor',
                password: passwordPlain,
                roleId: roleSupervisor.id,
                managerId: manager.id,
                isActive: true
            }
        });

        const [guardAhmed] = await User.findOrCreate({
            where: { email: 'ahmed@patrol.eg' },
            defaults: {
                name: 'Ahmed Guard',
                password: passwordPlain,
                roleId: roleGuard.id,
                managerId: manager.id, // Hierarchy: Reports to Tarek
                isActive: true
            }
        });

        const [guardMahmoud] = await User.findOrCreate({
            where: { email: 'mahmoud@patrol.eg' },
            defaults: {
                name: 'Mahmoud Guard',
                password: passwordPlain,
                roleId: roleGuard.id,
                managerId: manager.id, // Hierarchy: Reports to Tarek
                isActive: true
            }
        });

        // Sites
        const [cfc] = await Site.findOrCreate({
            where: { name: 'Cairo Festival City' },
            defaults: {
                address: 'Ring Road, New Cairo, Cairo, Egypt',
                lat: 30.0315,
                lng: 31.4055,
                details: 'Large mall and business district.'
            }
        });
        await cfc.addStaff([manager, guardAhmed]);

        const [smartVillage] = await Site.findOrCreate({
            where: { name: 'Smart Village' },
            defaults: {
                address: 'Cairo-Alex Desert Road, Giza, Egypt',
                lat: 30.0710,
                lng: 31.0213,
                details: 'Technology business park.'
            }
        });
        await smartVillage.addStaff([manager, guardMahmoud]);

        // Zones & Checkpoints
        const [cfcZoneA] = await Zone.findOrCreate({ where: { name: 'Mall Entrance', siteId: cfc.id } });
        const [cfcZoneB] = await Zone.findOrCreate({ where: { name: 'Parking B2', siteId: cfc.id } });

        const [cp1] = await Checkpoint.findOrCreate({
            where: { uid: 'NFC_001' },
            defaults: { name: 'Main Gate Security', type: 'nfc', zoneId: cfcZoneA.id, siteId: cfc.id, lat: 30.0316, lng: 31.4056 }
        });
        const [cp2] = await Checkpoint.findOrCreate({
            where: { name: 'Loading Dock' },
            defaults: { type: 'qr', zoneId: cfcZoneA.id, siteId: cfc.id }
        });
        const [cp3] = await Checkpoint.findOrCreate({
            where: { name: 'VIP Parking' },
            defaults: { type: 'qr', zoneId: cfcZoneB.id, siteId: cfc.id }
        });

        // Patrol Templates
        const [templateCFC] = await PatrolTemplate.findOrCreate({
            where: { name: 'CFC Morning Routine' },
            defaults: {
                description: 'Check main gates and parking.',
                checkpointsList: [cp1.id, cp2.id, cp3.id],
                estimatedDurationMinutes: 30,
                siteId: cfc.id
            }
        });

        console.log('✅ Seeding Complete!');
        process.exit(0);

    } catch (err) {
        console.error('❌ Seeding Failed:', err);
        process.exit(1);
    }
}

seed();
