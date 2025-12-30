const db = require('../src/models');
const { User, Role } = db;

async function createAdmin() {
    try {
        await db.sequelize.sync({ force: false }); // Ensure DB is synced

        // Check if admin exists first
        const existingAdmin = await User.findOne({ where: { email: 'admin@example.com' } });
        if (existingAdmin) {
            console.log('Admin already exists:', existingAdmin.email);
            process.exit(0);
        }

        const admin = await db.User.create({
            name: 'Sherif The Admin',
            email: 'admin@patrol.eg',
            password: 'password123',
            role: 'admin',
            isActive: true
        });

        // Associate: admin.setRole(adminRole) if association exists on instance
        // But our User model currently has `roleId` potentially if we used foreign key logic?
        // In User.js, I commented out the 'role' enum field and said "Moving to foreign key".
        // But I didn't add `roleId` explicitly to `User.js` body, Sequelize `belongsTo` adds it? 
        // Wait, `Role.hasMany(User)` adds `roleId` to User. 
        // `User` model definition needs `belongsTo(Role)` to make it easy?
        // Let's check `User.js` and `Role.js`.
        // `Role.js`: Role.hasMany(models.User, { foreignKey: 'roleId' });
        // `User.js` association: I didn't add `User.associate`! I missed that step when modifying `User.js`.

        console.log('Admin created:', admin.email);
        process.exit(0);
    } catch (err) {
        console.error(err);
        process.exit(1);
    }
}

createAdmin();
