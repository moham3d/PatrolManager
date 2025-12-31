const { User, Role } = require('../models');

exports.getStats = async (req, res) => {
    try {
        const totalUsers = await User.count();
        const activeUsers = await User.count({ where: { isActive: true } });
        const usersByRole = await User.findAll({
            include: [{ model: Role, attributes: ['name'] }]
        });

        // Group by role manually or use SQL group (manual is easier for MVP)
        const roleCounts = {};
        usersByRole.forEach(u => {
            const role = u.Role ? u.Role.name : 'unknown';
            roleCounts[role] = (roleCounts[role] || 0) + 1;
        });

        res.json({
            totalUsers,
            activeUsers,
            roleCounts
        });
    } catch (err) {
        res.status(500).json({ error: true, message: err.message });
    }
};

exports.getUsers = async (req, res) => {
    try {
        const users = await User.findAll({
            include: [{ model: Role, attributes: ['name'] }],
            order: [['name', 'ASC']]
        });
        res.json(users);
    } catch (err) {
        res.status(500).json({ error: true, message: err.message });
    }
};

exports.createUser = async (req, res) => {
    try {
        const { name, email, password, roleId } = req.body;
        // Basic validation
        if (!email || !password || !roleId) {
            return res.status(400).json({ error: true, message: 'Missing fields' });
        }

        const newUser = await User.create({
            name,
            email,
            password, // Password hook handles hashing
            roleId,
            isActive: true
        });

        res.json({ message: 'User created', user: newUser });
    } catch (err) {
        res.status(500).json({ error: true, message: err.message });
    }
};
