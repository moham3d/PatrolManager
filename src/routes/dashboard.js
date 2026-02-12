const express = require('express');
const router = express.Router();
const { ensureAuth } = require('../middleware/auth');

const db = require('../models');
const { Site, Incident, Shift, User, Role, PatrolRun, PatrolTemplate } = db;
const sequelize = db.sequelize;
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

            // Guard Performance (Top 5 by completed patrols)
            const topGuards = await PatrolRun.findAll({
                where: { status: 'completed' },
                attributes: ['guardId', [sequelize.fn('COUNT', sequelize.col('PatrolRun.id')), 'count']],
                group: ['guardId', 'guard.id'],
                order: [[sequelize.literal('count'), 'DESC']],
                limit: 5,
                include: [{ model: User, as: 'guard', attributes: ['name', 'id'] }]
            });

            // Site Status (Sites with active incidents)
            const problemSites = await Site.findAll({
                include: [{
                    model: Incident,
                    as: 'incidents',
                    where: { status: { [Op.not]: 'resolved' } },
                    attributes: ['id', 'priority'],
                    required: true
                }],
                limit: 5
            });

            const systemHealth = {
                uptime: process.uptime(),
                dbStatus: 'Connected', 
                lastBackup: new Date(Date.now() - 3600000).toISOString() // Mock: 1 hour ago
            };

            return res.render('dashboard/admin', { 
                title: 'Admin Dashboard', 
                stats, 
                recentIncidents,
                topGuards,
                problemSites,
                systemHealth
            });
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

        else if (role === 'supervisor') {
            // [AUDIT FIX] Supervisor Dashboard
            // Similar to Manager but usually just for their ONE site.
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

            const activePatrols = await PatrolRun.findAll({
                where: { 
                    status: 'active',
                    siteId: siteIds.length ? { [Op.in]: siteIds } : -1 
                },
                include: [
                    { model: User, as: 'guard', attributes: ['name', 'id'] },
                    { model: PatrolTemplate, as: 'template', attributes: ['name'] }
                ]
            });

            return res.render('dashboard/supervisor', { 
                title: 'Supervisor Dashboard', 
                stats, 
                recentIncidents, 
                sites: user.assignedSites,
                activePatrols 
            });
        }

        else {
            // Guard
            console.log(`[Dashboard] Guard ID: ${req.user.id}`);
            
            const activeShift = await Shift.findOne({
                where: { userId: req.user.id, status: 'active' },
                include: [{ model: Site, as: 'site' }]
            });

            // Check for active patrol run
            let activePatrol = null;
            let patrolCheckpoints = [];
            let completedVisits = [];

            if (activeShift) {
                activePatrol = await PatrolRun.findOne({
                    where: { 
                        guardId: req.user.id,
                        status: 'active'
                    },
                    include: [{ model: PatrolTemplate, as: 'template' }]
                });

                if (activePatrol && activePatrol.template && activePatrol.template.checkpointsList) {
                    // Fetch full checkpoint details
                    const { Checkpoint, CheckpointVisit } = db;
                    const cpIds = activePatrol.template.checkpointsList;
                    
                    if (cpIds.length > 0) {
                        patrolCheckpoints = await Checkpoint.findAll({
                            where: { id: { [Op.in]: cpIds } }
                        });
                        
                        // Re-order them according to the list if needed, or just send them
                        // Let's preserve order from JSON list
                        patrolCheckpoints = cpIds.map(id => patrolCheckpoints.find(c => c.id === id)).filter(c => c);
                    }

                    // Fetch completed visits for this run
                    const visits = await CheckpointVisit.findAll({
                        where: { patrolRunId: activePatrol.id }
                    });
                    completedVisits = visits.map(v => v.checkpointId);
                }
            }

            // Look for next scheduled work (including running late)
            // We want scheduled shifts where the END time hasn't passed yet.
            const now = new Date();
            console.log(`[Dashboard] Searching for shifts ending after: ${now.toISOString()}`);
            
            const nextShift = await Shift.findOne({
                where: {
                    userId: req.user.id,
                    status: 'scheduled',
                    endTime: { [Op.gt]: now }
                },
                order: [['startTime', 'ASC']],
                include: [{ model: Site, as: 'site' }]
            });
            
            console.log(`[Dashboard] Found nextShift: ${nextShift ? nextShift.id : 'null'}`);

            return res.render('dashboard/guard', { 
                title: 'Guard Dashboard', 
                activeShift, 
                nextShift,
                activePatrol,
                patrolCheckpoints,
                completedVisits
            });
        }
    } catch (err) {
        next(err);
    }
});

router.get('/live', ensureAuth, (req, res) => {
    res.render('dashboard/live', { title: 'Live Operations Center' });
});

module.exports = router;
