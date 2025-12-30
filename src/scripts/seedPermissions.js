const { Role, Permission, User } = require('../models');

async function seedPermissions() {
    console.log('--- Seeding Permissions ---');

    // 1. Define Permissions
    const permissions = [
        { name: 'user_view', description: 'View list of users' },
        { name: 'user_create', description: 'Create new users' },
        { name: 'user_edit', description: 'Edit existing users' },
        { name: 'user_delete', description: 'Delete users' },

        { name: 'site_view', description: 'View sites' },
        { name: 'site_manage', description: 'Create/Edit/Delete sites' },

        { name: 'patrol_view', description: 'View patrols' },
        { name: 'patrol_manage', description: 'Create/Edit/Delete patrols' },
    ];

    const permInstances = {};

    for (const p of permissions) {
        // Find or create
        const [instance, created] = await Permission.findOrCreate({
            where: { name: p.name },
            defaults: p
        });
        permInstances[p.name] = instance;
        if (created) console.log(`Created Permission: ${p.name}`);
    }

    // 2. Define Roles
    const roles = [
        { name: 'Admin', description: 'Super Administrator' },
        { name: 'Manager', description: 'Operations Manager' },
        { name: 'Guard', description: 'Security Guard (Mobile)' }
    ];

    const roleInstances = {};

    for (const r of roles) {
        const [instance, created] = await Role.findOrCreate({
            where: { name: r.name },
            defaults: r
        });
        roleInstances[r.name] = instance;
        if (created) console.log(`Created Role: ${r.name}`);
    }

    // 3. Assign Permissions to Roles

    // Admin gets ALL permissions
    await roleInstances['Admin'].setPermissions(Object.values(permInstances));
    console.log('Assigned ALL permissions to Admin');

    // Manager gets User View/Edit (not delete), Site Manage, Patrol Manage
    const managerPerms = [
        permInstances['user_view'],
        permInstances['user_create'],
        permInstances['user_edit'],
        permInstances['site_view'],
        permInstances['site_manage'],
        permInstances['patrol_view'],
        permInstances['patrol_manage']
    ];
    await roleInstances['Manager'].setPermissions(managerPerms);
    console.log('Assigned Operations permissions to Manager');

    // Guard gets Read Only or specific mobile perms (will add later)
    // For now, Guard has no web admin permissions
    await roleInstances['Guard'].setPermissions([]);
    console.log('Assigned Clean permissions to Guard');

    // 4. Create Default Admin User
    // Check if exists
    const adminEmail = 'admin@example.com';
    const existingAdmin = await User.findOne({ where: { email: adminEmail } });

    if (!existingAdmin) {
        await User.create({
            name: 'System Admin',
            email: adminEmail,
            password: 'password123', // Will be hashed by hook
            roleId: roleInstances['Admin'].id
        });
        console.log(`Created Admin User: ${adminEmail}`);
    }

    console.log('--- Seeding Complete ---');
}

// Allow running directly: node src/scripts/seedPermissions.js
if (require.main === module) {
    const db = require('../config/database');
    // Force sync to ensure clean schema (fixes unique constraint issues on fresh dev)
    db.sync({ force: true })
        .then(() => seedPermissions())
        .then(() => process.exit(0))
        .catch(err => {
            console.error(err);
            process.exit(1);
        });
}

module.exports = seedPermissions;
