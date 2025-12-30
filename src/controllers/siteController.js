const { Site, Zone, Checkpoint, User, Role } = require('../models');

// Helpers for formatted responses
const renderOrJson = (res, view, data) => {
    res.format({
        'text/html': () => res.render(view, data),
        'application/json': () => res.json(data)
    });
};

exports.index = async (req, res) => {
    try {
        let where = {};
        const user = req.user;

        // RBAC Filter
        if (user.Role.name !== 'admin') {
            // Find sites where user is "staff"
            const assignedSites = await user.getAssignedSites(); // BelongsToMany 'SiteAssignments'
            const siteIds = assignedSites.map(s => s.id);

            // If manager/guard has assignments, limit to those. If none, show none.
            where.id = siteIds.length ? siteIds : -1;
        }

        const sites = await Site.findAll({
            where,
            include: [{ model: Zone, as: 'zones' }]
        });

        renderOrJson(res, 'sites/index', {
            title: 'Site Management',
            sites
        });
    } catch (err) {
        console.error(err);
        res.status(500).send('Server Error');
    }
};

exports.show = async (req, res) => {
    try {
        const { Shift, Incident, PatrolRun } = require('../models'); // Ensure import

        const site = await Site.findByPk(req.params.id, {
            include: [
                { model: Zone, as: 'zones' },
                { model: Checkpoint, as: 'checkpoints' },
                { model: User, as: 'staff', include: [Role] },
                {
                    model: Shift,
                    as: 'shifts',
                    required: false,
                    where: {
                        startTime: { [require('sequelize').Op.gte]: new Date() } // Future shifts only
                    },
                    include: [{ model: User, as: 'user' }]
                },
                {
                    model: Incident,
                    as: 'incidents',
                    required: false,
                    limit: 10,
                    order: [['createdAt', 'DESC']],
                    include: [{ model: User, as: 'reporter' }]
                },
                {
                    model: PatrolRun,
                    as: 'patrolRuns',
                    required: false,
                    limit: 10,
                    order: [['startTime', 'DESC']],
                    include: [{ model: User, as: 'guard' }]
                }
            ],
            order: [
                [{ model: Shift, as: 'shifts' }, 'startTime', 'ASC']
            ]
        });

        if (!site) {
            return res.status(404).json({ message: 'Site not found' });
        }

        // RBAC Check
        if (req.user.Role.name !== 'admin') {
            // Check if user is staff of this site
            const isStaff = await site.hasStaff(req.user);
            if (!isStaff) return res.status(403).send('Access Denied');
        }

        // Fetch potential staff (guards and managers)
        const allUsers = await User.findAll({
            include: [{
                model: Role,
                where: { name: ['guard', 'manager'] }
            }],
            order: [['name', 'ASC']]
        });

        renderOrJson(res, 'sites/details', {
            title: site.name,
            site,
            allUsers
        });
    } catch (err) {
        console.error(err);
        res.status(500).send('Server Error');
    }
};

exports.addStaff = async (req, res) => {
    try {
        const { userId } = req.body;
        const site = await Site.findByPk(req.params.id);
        const user = await User.findByPk(userId);

        if (!site || !user) return res.status(404).send('Site or User not found');

        await site.addStaff(user);

        res.redirect('/sites/' + site.id + '?tab=staff');
    } catch (err) {
        console.error(err);
        res.status(500).send('Error adding staff');
    }
};

exports.removeStaff = async (req, res) => {
    try {
        const { userId } = req.body;
        const site = await Site.findByPk(req.params.id);
        const user = await User.findByPk(userId);

        if (!site || !user) return res.status(404).send('Site or User not found');

        await site.removeStaff(user);

        res.redirect('/sites/' + site.id + '?tab=staff');
    } catch (err) {
        console.error(err);
        res.status(500).send('Error removing staff');
    }
};

exports.create = async (req, res) => {
    // Only for Web form view
    res.render('sites/form', { title: 'Create Site', site: null });
};

exports.store = async (req, res) => {
    try {
        const { name, address, details, lat, lng } = req.body;
        const site = await Site.create({ name, address, details, lat, lng });

        res.format({
            'text/html': () => res.redirect('/sites/' + site.id + '?action=add-zone'),
            'application/json': () => res.status(201).json(site)
        });
    } catch (err) {
        console.error(err);
        res.format({
            'text/html': () => res.render('sites/form', { title: 'Create Site', site: req.body, error: err.message }),
            'application/json': () => res.status(400).json({ error: err.message })
        });
    }
};

exports.edit = async (req, res) => {
    try {
        const site = await Site.findByPk(req.params.id);
        if (!site) return res.send(404);
        res.render('sites/form', { title: 'Edit Site', site });
    } catch (err) {
        console.error(err);
        res.send(500);
    }
};

exports.update = async (req, res) => {
    try {
        const { name, address, details, lat, lng } = req.body;

        await Site.update({ name, address, details, lat, lng }, {
            where: { id: req.params.id }
        });

        res.format({
            'text/html': () => res.redirect('/sites/' + req.params.id),
            'application/json': () => res.json({ message: 'Updated' })
        });
    } catch (err) {
        console.error(err);
        res.send(500);
    }
};

// --- Associations ---

exports.addZone = async (req, res) => {
    try {
        const { name, description } = req.body;
        const siteId = req.params.id;
        const zone = await Zone.create({ name, description, siteId });

        res.format({
            'text/html': () => res.redirect('/sites/' + siteId + '?action=add-checkpoint'),
            'application/json': () => res.status(201).json(zone)
        });
    } catch (err) {
        console.error(err);
        res.status(500).send('Error adding zone');
    }
};

exports.addCheckpoint = async (req, res) => {
    try {
        const { name, type, uid, zoneId, lat, lng } = req.body;
        const siteId = req.params.id;

        const checkpoint = await Checkpoint.create({
            name,
            type,
            uid: uid || null, // Convert empty string to null to avoid unique constraint violation
            zoneId: zoneId || null,
            siteId,
            lat: lat || null,
            lng: lng || null
        });

        res.format({
            'text/html': () => res.redirect('/sites/' + siteId + '?success=true'),
            'application/json': () => res.status(201).json(checkpoint)
        });
    } catch (err) {
        console.error(err);
        res.status(500).send('Error adding checkpoint');
    }
};

exports.updateZone = async (req, res) => {
    try {
        const { name, description } = req.body;
        await Zone.update({ name, description }, { where: { id: req.params.id } });
        // Redirect back to site from referer ideally, or find siteId
        const zone = await Zone.findByPk(req.params.id);
        res.redirect('/sites/' + zone.siteId);
    } catch (err) {
        console.error(err);
        res.status(500).send('Error updating zone');
    }
};

exports.deleteZone = async (req, res) => {
    try {
        const zone = await Zone.findByPk(req.params.id);
        if (zone) {
            await zone.destroy();
            res.redirect('/sites/' + zone.siteId);
        } else {
            res.status(404).send('Zone not found');
        }
    } catch (err) {
        console.error(err);
        res.status(500).send('Error deleting zone');
    }
};

exports.updateCheckpoint = async (req, res) => {
    try {
        const { name, type, uid, zoneId, lat, lng } = req.body;

        await Checkpoint.update({
            name,
            type,
            uid: uid || null,
            zoneId: zoneId || null,
            lat: lat || null,
            lng: lng || null
        }, {
            where: { id: req.params.id }
        });

        const checkpoint = await Checkpoint.findByPk(req.params.id);
        res.redirect('/sites/' + checkpoint.siteId);
    } catch (err) {
        console.error(err);
        res.status(500).send('Error updating checkpoint');
    }
};

exports.deleteCheckpoint = async (req, res) => {
    try {
        const checkpoint = await Checkpoint.findByPk(req.params.id);
        if (checkpoint) {
            await checkpoint.destroy();
            res.redirect('/sites/' + checkpoint.siteId);
        } else {
            res.status(404).send('Checkpoint not found');
        }
    } catch (err) {
        console.error(err);
        res.status(500).send('Error deleting checkpoint');
    }
};

const QRCode = require('qrcode');

// ... (existing helper)

// ... (existing CRUD methods)

exports.destroy = async (req, res) => {
    try {
        await Site.destroy({ where: { id: req.params.id } });
        res.format({
            'text/html': () => res.redirect('/sites'),
            'application/json': () => res.json({ message: 'Deleted' })
        });
    } catch (err) {
        console.error(err);
        res.send(500);
    }
};

exports.printQRCodes = async (req, res) => {
    try {
        const site = await Site.findByPk(req.params.id, {
            include: [{ model: Checkpoint, as: 'checkpoints' }]
        });

        if (!site) return res.status(404).send('Site not found');

        // Generate QR Data URIs
        // Data Format: "CHECKPOINT:{id}"
        const checkpointsWithQR = await Promise.all(site.checkpoints.map(async (cp) => {
            const qrData = await QRCode.toDataURL(`CHECKPOINT:${cp.id}`);
            return {
                ...cp.toJSON(),
                qrData
            };
        }));

        res.render('sites/print_qrs', {
            site,
            checkpoints: checkpointsWithQR
        });

    } catch (err) {
        console.error(err);
        res.status(500).send('Error generating QR codes');
    }
};

