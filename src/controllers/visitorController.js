const { Visitor, Site } = require('../models');
const { Op } = require('sequelize');

exports.index = async (req, res) => {
    try {
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const visitors = await Visitor.findAll({
            where: {
                // Show visitors for today or future, OR recently active
                expectedArrivalTime: { [Op.gte]: today }
            },
            include: ['site'],
            order: [['expectedArrivalTime', 'ASC']]
        });

        const sites = await Site.findAll();

        res.format({
            'text/html': () => res.render('visitors/index', { title: 'Visitor Management', visitors, sites }),
            'application/json': () => res.json(visitors)
        });
    } catch (err) {
        console.error(err);
        res.status(500).send('Server Error');
    }
};

exports.preRegister = async (req, res) => {
    try {
        const { name, siteId, hostName, purpose, expectedArrivalTime } = req.body;

        const visitor = await Visitor.create({
            name,
            siteId,
            hostName,
            purpose,
            expectedArrivalTime,
            status: 'expected'
        });

        res.format({
            'text/html': () => res.redirect('/visitors'),
            'application/json': () => res.status(201).json(visitor)
        });
    } catch (err) {
        console.error(err);
        res.status(400).send('Error registering visitor');
    }
};

exports.checkIn = async (req, res) => {
    try {
        const visitor = await Visitor.findByPk(req.params.id);
        if (!visitor) return res.status(404).send('Visitor not found');

        await visitor.update({
            status: 'checked_in',
            checkInTime: new Date()
        });

        res.format({
            'text/html': () => res.redirect('/visitors'),
            'application/json': () => res.json(visitor)
        });
    } catch (err) {
        console.error(err);
        res.status(500).send('Error checking in');
    }
};

exports.checkOut = async (req, res) => {
    try {
        const visitor = await Visitor.findByPk(req.params.id);
        if (!visitor) return res.status(404).send('Visitor not found');

        await visitor.update({
            status: 'checked_out',
            checkOutTime: new Date()
        });

        res.format({
            'text/html': () => res.redirect('/visitors'),
            'application/json': () => res.json(visitor)
        });
    } catch (err) {
        console.error(err);
        res.status(500).send('Error checking out');
    }
};

// GET /api/visitors/today (Mobile Bridge)
exports.getToday = async (req, res) => {
    try {
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 1);

        // TODO: Filter by Guard's Site (Assuming Shift logic links Guard to Site)
        // For MVP: Return all visitors expected today
        const visitors = await Visitor.findAll({
            where: {
                expectedArrivalTime: {
                    [Op.gte]: today,
                    [Op.lt]: tomorrow
                },
                status: ['expected', 'checked_in']
            },
            include: ['site'],
            order: [['expectedArrivalTime', 'ASC']]
        });

        res.json(visitors);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};
