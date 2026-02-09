const { Permission, Role, RolePermission, sequelize } = require('../src/models');

const PERMISSIONS = [
    // Sites
    { name: 'View Sites', slug: 'SITE_VIEW', description: 'View sites list', category: 'Site Management' },
    { name: 'Manage Sites', slug: 'SITE_MANAGE', description: 'Create/Edit/Delete sites', category: 'Site Management' },
    // Users
    { name: 'View Users', slug: 'USER_VIEW', description: 'View users list', category: 'User Management' },
    { name: 'Manage Users', slug: 'USER_MANAGE', description: 'Create/Edit/Delete users', category: 'User Management' },
    // Reports
    { name: 'View Reports', slug: 'REPORT_VIEW', description: 'View reports', category: 'Reporting' },
    { name: 'Export Reports', slug: 'REPORT_EXPORT', description: 'Export CSV/PDF', category: 'Reporting' },
    // Roles
    { name: 'Manage Roles', slug: 'ROLE_MANAGE', description: 'Manage dynamic roles', category: 'Role Management' },
    // Patrols
    { name: 'View Patrols', slug: 'PATROL_VIEW', description: 'View patrol runs', category: 'Patrol Management' },
    { name: 'Manage Patrols', slug: 'PATROL_MANAGE', description: 'Create/Edit patrol templates', category: 'Patrol Management' },
    // Incidents
    { name: 'View Incidents', slug: 'INCIDENT_VIEW', description: 'View incidents', category: 'Incident Management' },
    { name: 'Manage Incidents', slug: 'INCIDENT_MANAGE', description: 'Resolve/Escalate incidents', category: 'Incident Management' },
    // Schedules
    { name: 'View Schedules', slug: 'SCHEDULE_VIEW', description: 'View shift schedules', category: 'Schedule Management' },
    { name: 'Manage Schedules', slug: 'SCHEDULE_MANAGE', description: 'Create/Edit shifts', category: 'Schedule Management' },
    // Zones & Checkpoints
    { name: 'View Zones', slug: 'ZONE_VIEW', description: 'View zones', category: 'Zone & Checkpoint Management' },
    { name: 'Manage Zones', slug: 'ZONE_MANAGE', description: 'Create/Edit zones and checkpoints', category: 'Zone & Checkpoint Management' }
];

async function seedRBAC() {
    try {
        console.log('🌱 Seeding RBAC...');

        // 1. Create Permissions
        for (const perm of PERMISSIONS) {
            const [permission, created] = await Permission.findOrCreate({
                where: { slug: perm.slug },
                defaults: perm
            });
            if (!created) {
                console.log(`ℹ️ Permission "${perm.slug}" already exists`);
            }
        }
        console.log('✅ Permissions created/verified.');

        // 2. Assign ALL to admin
        const adminRole = await Role.findOne({ where: { name: 'admin' } });
        if (adminRole) {
            const allPerms = await Permission.findAll();
            await adminRole.addPermissions(allPerms); // Magic method from belongsToMany
            console.log(`✅ Admin granted ${allPerms.length} permissions.`);
        } else {
            console.warn('⚠️ Admin role not found. Skipping assignment.');
        }

        // 3. Assign Limited to supervisor
        const supervisorRole = await Role.findOne({ where: { name: 'supervisor' } });
        if (supervisorRole) {
            const supervisorPerms = await Permission.findAll({
                where: { slug: [
                    'SITE_VIEW',
                    'USER_VIEW',
                    'REPORT_VIEW',
                    'PATROL_VIEW',
                    'INCIDENT_VIEW',
                    'SCHEDULE_VIEW',
                    'ZONE_VIEW',
                    'INCIDENT_MANAGE'
                ] }
            });
            await supervisorRole.addPermissions(supervisorPerms);
            console.log(`✅ Supervisor granted ${supervisorPerms.length} permissions.`);
        }

        // 4. Assign Guard Permissions
        const guardRole = await Role.findOne({ where: { name: 'guard' } });
        if (guardRole) {
            const guardPerms = await Permission.findAll({
                where: { slug: [
                    'SITE_VIEW',
                    'PATROL_VIEW',
                    'INCIDENT_VIEW',
                    'INCIDENT_MANAGE'
                ] }
            });
            await guardRole.addPermissions(guardPerms);
            console.log(`✅ Guard granted ${guardPerms.length} permissions.`);
        }

        console.log('✅ RBAC Seeding Complete!');
    } catch (error) {
        console.error('❌ Seeding Failed:', error);
    } finally {
        process.exit();
    }
}

seedRBAC();
