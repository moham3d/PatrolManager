const { Shift, User, Site, Role } = require('../models');
const { validationResult } = require('express-validator');

// Helper to handle HTML/JSON responses
const renderOrJson = (res, view, data) => {
    res.format({
        'text/html': () => res.render(view, data),
        'application/json': () => res.json(data)
    });
};



// Helper: Haversine Distance
const getDistanceFromLatLonInM = (lat1, lon1, lat2, lon2) => {
    var R = 6371; // Radius of the earth in km
    var dLat = (lat2 - lat1) * (Math.PI / 180);
    var dLon = (lon2 - lon1) * (Math.PI / 180);
    var a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c * 1000; // meters
}

exports.index = async (req, res) => {
    try {
        const shifts = await Shift.findAll({
            include: [
                { model: User, as: 'user' },
                { model: Site, as: 'site' }
            ],
            order: [['startTime', 'DESC']]
        });

        // For dropdowns in modal
        const sites = await Site.findAll();
        const users = await User.findAll({
            include: [{
                model: Role,
                where: { name: 'guard' }
            }]
        }); // Only schedule guards

        renderOrJson(res, 'shifts/index', { title: 'Shift Logs', shifts, sites, users });
    } catch (err) {
        console.error(err);
        res.status(500).send('Server Error');
    }
};

exports.create = async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() });
    }

    const t = await require('../models').sequelize.transaction();

    try {
        const { userId, siteId, date, startTime, endTime, repeatFrequency, repeatUntil } = req.body;

        const shiftsToCreate = [];

        // Base Date
        let currentDate = new Date(date);
        const endDateLimit = repeatUntil ? new Date(repeatUntil) : new Date(date);

        // helper to set time on a date object
        const setTime = (d, timeStr) => {
            const [hours, mins] = timeStr.split(':');
            const newD = new Date(d);
            newD.setHours(hours, mins, 0, 0);
            return newD;
        };

        // Loop Logic
        while (currentDate <= endDateLimit) {
            shiftsToCreate.push({
                userId,
                siteId,
                startTime: setTime(currentDate, startTime),
                endTime: setTime(currentDate, endTime),
                status: 'scheduled'
            });

            if (repeatFrequency === 'daily') {
                currentDate.setDate(currentDate.getDate() + 1);
            } else if (repeatFrequency === 'weekly') {
                currentDate.setDate(currentDate.getDate() + 7);
            } else {
                break; // 'none' or invalid, run once and exit
            }
        }

        const createdShifts = await Shift.bulkCreate(shiftsToCreate, { transaction: t });

        await t.commit();

        res.format({
            'text/html': () => {
                req.flash('success', `${createdShifts.length} shift(s) scheduled successfully`);
                res.redirect(`/sites/${siteId}?tab=schedule`);
            },
            'application/json': () => res.status(201).json({ count: createdShifts.length, shifts: createdShifts })
        });

    } catch (err) {
        await t.rollback();
        console.error(err);
        req.flash('error', 'Error creating shift(s): ' + err.message);
        res.redirect(`/sites/${req.body.siteId}?tab=schedule`);
    }
};

exports.destroy = async (req, res) => {
    try {
        const shift = await Shift.findByPk(req.params.id);
        if (shift) {
            const siteId = shift.siteId;
            await shift.destroy();
            req.flash('success', 'Shift cancelled successfully');

            res.format({
                'text/html': () => res.redirect(`/sites/${siteId}?tab=schedule`),
                'application/json': () => res.json({ message: 'Shift deleted' })
            });
        } else {
            req.flash('error', 'Shift not found');
            res.redirect('back');
        }
    } catch (err) {
        console.error(err);
        req.flash('error', 'Error cancelling shift');
        res.redirect('back');
    }
};

exports.clockIn = async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() });
    }

    try {
        const { siteId, latitude, longitude } = req.body;

        // Check if already active
        const activeShift = await Shift.findOne({
            where: {
                userId: req.user.id,
                status: 'active'
            }
        });

        if (activeShift) {
            return res.format({
                'text/html': () => {
                    req.flash('error', 'You are already clocked in.');
                    res.redirect('/dashboard');
                },
                'application/json': () => res.status(400).json({ error: true, message: 'You are already clocked in.' })
            });
        }

        // Validate Site and Location
        if (!siteId) {
            return res.format({
                'text/html': () => {
                    req.flash('error', 'Site ID is required.');
                    res.redirect('/dashboard');
                },
                'application/json': () => res.status(400).json({ error: true, message: 'Site ID is required.' })
            });
        }

        const site = await Site.findByPk(siteId);
        if (!site) {
            return res.format({
                'text/html': () => {
                    req.flash('error', 'Site not found.');
                    res.redirect('/dashboard');
                },
                'application/json': () => res.status(404).json({ error: true, message: 'Site not found.' })
            });
        }

        let startLocation = null;
        if (latitude && longitude) {
            startLocation = { lat: parseFloat(latitude), lng: parseFloat(longitude) };

            // Check Geofence
            if (site.lat && site.lng) {
                const dist = getDistanceFromLatLonInM(latitude, longitude, site.lat, site.lng);
                const MAX_DIST_METERS = 500; // Configurable

                if (dist > MAX_DIST_METERS) {
                    const msg = `You are too far from the site (${Math.round(dist)}m). Move closer to clock in.`;
                    return res.format({
                        'text/html': () => {
                            req.flash('error', msg);
                            res.redirect('/dashboard');
                        },
                        'application/json': () => res.status(403).json({ error: true, message: msg })
                    });
                }
            }
        } else {
            // Require location for mobile app
            return res.format({
                'text/html': () => {
                    req.flash('error', 'GPS Location is required to clock in.');
                    res.redirect('/dashboard');
                },
                'application/json': () => res.status(400).json({ error: true, message: 'GPS Location is required to clock in.' })
            });
        }

        const shift = await Shift.create({
            userId: req.user.id,
            siteId,
            startTime: new Date(),
            status: 'active',
            startLocation
        });

        res.format({
            'text/html': () => {
                req.flash('success', 'Clocked in successfully!');
                res.redirect('/dashboard');
            },
            'application/json': () => res.json({ message: 'Clocked in successfully', shift })
        });
    } catch (err) {
        console.error(err);
        res.format({
            'text/html': () => {
                req.flash('error', 'Server Error: ' + err.message);
                res.redirect('/dashboard');
            },
            'application/json': () => res.status(500).json({ error: true, message: err.message })
        });
    }
};

exports.clockOut = async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ errors: errors.array() });
    }

    try {
        const { latitude, longitude } = req.body;

        const activeShift = await Shift.findOne({
            where: {
                userId: req.user.id,
                status: 'active'
            }
        });

        if (!activeShift) {
            return res.status(400).json({ error: true, message: 'No active shift found.' });
        }

        activeShift.endTime = new Date();
        activeShift.status = 'completed';

        if (latitude && longitude) {
            activeShift.endLocation = { lat: parseFloat(latitude), lng: parseFloat(longitude) };
        }

        await activeShift.save();

        res.json({ message: 'Clocked out successfully', shift: activeShift });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};
