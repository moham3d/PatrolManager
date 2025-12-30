const { Schedule, Site, User } = require('../models');

exports.index = async (req, res) => {
    try {
        const schedules = await Schedule.findAll({
            include: ['site', 'user'],
            order: [['startTime', 'ASC']]
        });
        const sites = await Site.findAll();
        const users = await User.findAll({ where: { isActive: true } });

        res.format({
            'text/html': () => res.render('schedules/index', { title: 'Rostering', schedules, sites, users }),
            'application/json': () => res.json(schedules)
        });
    } catch (err) {
        console.error(err);
        res.status(500).send('Server Error');
    }
};

exports.create = async (req, res) => {
    try {
        const { userId, siteId, date, startTime, endTime } = req.body;

        // Combine date and time
        const start = new Date(`${date}T${startTime}`);
        const end = new Date(`${date}T${endTime}`);

        // Handle overnight shifts simply (add +1 day if end < start)
        if (end < start) {
            end.setDate(end.getDate() + 1);
        }

        const schedule = await Schedule.create({
            userId,
            siteId,
            startTime: start,
            endTime: end,
            status: 'scheduled'
        });

        res.format({
            'text/html': () => res.redirect('/schedules'),
            'application/json': () => res.status(201).json(schedule)
        });
    } catch (err) {
        console.error(err);
        res.status(400).send('Error creating schedule');
    }
};

exports.delete = async (req, res) => {
    try {
        await Schedule.destroy({ where: { id: req.params.id } });
        res.format({
            'text/html': () => res.redirect('/schedules'),
            'application/json': () => res.json({ message: 'Deleted' })
        });
    } catch (err) {
        res.status(500).send('Error');
    }
};
