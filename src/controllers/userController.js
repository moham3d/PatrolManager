const { User, Role } = require('../models');

// Helpers for formatted responses
const renderOrJson = (res, view, data) => {
    res.format({
        'text/html': () => res.render(view, data),
        'application/json': () => res.json(data)
    });
};

exports.index = async (req, res) => {
    try {
        const users = await User.findAll({
            include: [{ model: Role }],
            attributes: { exclude: ['password'] } // Don't send passwords in JSON
        });

        renderOrJson(res, 'users/index', {
            title: 'User Management',
            users
        });
    } catch (err) {
        console.error(err);
        res.status(500).send('Server Error');
    }
};

exports.create = async (req, res) => {
    const roles = await Role.findAll();
    // Fetch potential managers
    const managers = await User.findAll({
        include: [{
            model: Role,
            where: { name: ['admin', 'manager'] }
        }]
    });
    res.render('users/form', { title: 'Create User', user: null, roles, managers });
};

exports.store = async (req, res) => {
    try {

        const { name, email, password, roleId, managerId } = req.body;

        // Basic validation
        if (!name || !email || !password || !roleId) {
            throw new Error('All fields are required');
        }

        const user = await User.create({ name, email, password, roleId, managerId: managerId || null });

        res.format({
            'text/html': () => res.redirect('/users'),
            'application/json': () => res.status(201).json(user)
        });
    } catch (err) {
        console.error(err);
        const roles = await Role.findAll();
        res.format({
            'text/html': () => res.render('users/form', {
                title: 'Create User',
                user: req.body,
                roles,
                error: err.message
            }),
            'application/json': () => res.status(400).json({ error: err.message })
        });
    }
};

exports.edit = async (req, res) => {
    try {
        const user = await User.findByPk(req.params.id);
        const roles = await Role.findAll();
        // Fetch potential managers (admins and managers)
        const managers = await User.findAll({
            include: [{
                model: Role,
                where: { name: ['admin', 'manager'] }
            }]
        });

        if (!user) return res.status(404).send('User not found');

        res.render('users/form', { title: 'Edit User', user, roles, managers });
    } catch (err) {
        console.error(err);
        res.status(500).send('Server Error');
    }
};

exports.update = async (req, res) => {
    try {
        const { name, email, password, roleId, managerId } = req.body;
        const user = await User.findByPk(req.params.id);

        if (!user) return res.status(404).send('User not found');

        user.name = name;
        user.email = email;
        user.email = email;
        user.roleId = roleId;
        user.managerId = managerId || null;

        // Only update password if provided
        if (password && password.trim() !== '') {
            user.password = password;
        }

        await user.save();

        res.format({
            'text/html': () => res.redirect('/users'),
            'application/json': () => res.json({ message: 'User Updated', user })
        });
    } catch (err) {
        console.error(err);
        const roles = await Role.findAll();
        res.render('users/form', {
            title: 'Edit User',
            user: { ...req.body, id: req.params.id },
            roles,
            error: err.message
        });
    }
};

exports.destroy = async (req, res) => {
    try {
        await User.destroy({ where: { id: req.params.id } });
        res.format({
            'text/html': () => res.redirect('/users'),
            'application/json': () => res.json({ message: 'User Deleted' })
        });
    } catch (err) {
        console.error(err);
        res.status(500).send('Server Error');
    }
};
