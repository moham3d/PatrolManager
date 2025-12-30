const express = require('express');
const router = express.Router();
const { ensureAuth } = require('../middleware/auth');

const { Site, Incident, Shift, User, Role } = require('../models');
const { Op } = require('sequelize');

router.get('/', ensureAuth, async (req, res) => {
    try {
        const role = req.user.Role ? req.user.Role.name.toLowerCase() : 'guard';

        if (role === 'admin') {
            const stats = {
                sites: await Site.count(),
                users: await User.count(),
                shifts: await Shift.count({ where: { status: 'active' } }),
                incidents: await Incident.count({ where: { status: 'new' } })
            };
            const recentIncidents = await Incident.findAll({
                limit: 5,
                order: [['createdAt', 'DESC']],
                include: [{ model: User, as: 'reporter' }, { model: Site }]
            });
            return res.render('dashboard/admin', { title: 'Admin Dashboard', stats, recentIncidents });
        }

        else if (role === 'manager') {
            // Find sites managed by this user
            // using the assignedSites association
            const user = await User.findByPk(req.user.id, {
                include: [{ model: Site, as: 'assignedSites' }]
            });

            const siteIds = user.assignedSites.map(s => s.id);

            const stats = {
                mySites: siteIds.length,
                activeShifts: await Shift.count({
                    where: {
                        status: 'active',
                        siteId: siteIds.length ? { [Op.in]: siteIds } : -1
                    }
                }),
                pendingIncidents: await Incident.count({
                    where: {
                        status: 'new',
                        siteId: siteIds.length ? { [Op.in]: siteIds } : -1
                    }
                })
            };

            const recentIncidents = await Incident.findAll({
                where: { siteId: siteIds.length ? { [Op.in]: siteIds } : -1 },
                limit: 5,
                order: [['createdAt', 'DESC']],
                include: [{ model: User, as: 'reporter' }, { model: Site }]
            });

            return res.render('dashboard/manager', { title: 'Manager Dashboard', stats, recentIncidents, sites: user.assignedSites });
        }

        else {
            // Guard
            const activeShift = await Shift.findOne({
                where: { userId: req.user.id, status: 'active' },
                include: [{ model: Site, as: 'site' }]
            });

            const nextShift = await Shift.findOne({
                where: {
                    userId: req.user.id,
                    status: 'scheduled',
                    startTime: { [Op.gt]: new Date() }
                },
                order: [['startTime', 'ASC']],
                include: [{ model: Site, as: 'site' }]
            });

            return res.render('dashboard/guard', { title: 'Guard Dashboard', activeShift, nextShift });
        }
    } catch (err) {
        console.error(err);
        res.render('dashboard/live', { title: 'Dashboard', error: 'Error loading dashboard' });
    }
});

module.exports = router;
