const { Incident, PanicAlert, Site, User } = require('../models');

// Helpers
const renderOrJson = (res, view, data) => {
    res.format({
        'text/html': () => res.render(view, data),
        'application/json': () => res.json(data)
    });
};

exports.index = async (req, res) => {
    try {
        const incidents = await Incident.findAll({
            include: [
                { model: User, as: 'reporter' },
                { model: User, as: 'assignee' },
                { model: Site }
            ],
            order: [['createdAt', 'DESC']]
        });
        const users = await User.findAll({ order: [['name', 'ASC']] });
        renderOrJson(res, 'incidents/index', { title: 'Incidents', incidents, users });
    } catch (err) {
        console.error(err);
        res.status(500).send('Server Error');
    }
};

exports.create = async (req, res) => {
    const sites = await Site.findAll();
    const zones = await require('../models').Zone.findAll(); // Fetch all zones
    res.render('incidents/create', { title: 'Report Incident', sites, zones });
};

exports.store = async (req, res) => {
    try {
        // Handle file upload here if Multer was set up (not yet in this scope, skipping for now)
        const { type, priority, description, siteId, zoneId, location } = req.body;

        let geom = null;
        if (location && location.lat && location.lng) {
            geom = { type: 'Point', coordinates: [location.lat, location.lng] };
        }

        const evidencePath = req.file ? '/uploads/incidents/' + req.file.filename : null;

        const incident = await Incident.create({
            type,
            priority,
            description,
            siteId,
            zoneId: zoneId || null,
            reporterId: req.user.id,
            location: geom,
            status: 'new',
            evidencePath
        });

        // Emit real-time event
        req.io.emit('new_incident', incident);

        res.format({
            'text/html': () => res.redirect('/incidents'),
            'application/json': () => res.status(201).json(incident)
        });
    } catch (err) {
        console.error(err);
        res.status(400).send('Error creating incident');
    }
};

exports.show = async (req, res) => {
    try {
        const incident = await Incident.findByPk(req.params.id, {
            include: [
                { model: User, as: 'reporter' },
                { model: User, as: 'assignee' },
                { model: Site },
                { model: require('../models').Zone }
            ]
        });

        if (!incident) return res.status(404).send('Incident not found');

        const users = await User.findAll({ order: [['name', 'ASC']] });
        res.render('incidents/show', { title: `Incident #${incident.id}`, incident, users });
    } catch (err) {
        console.error(err);
        res.status(500).send('Server Error');
    }
};

exports.claim = async (req, res) => {
    try {
        const incident = await Incident.findByPk(req.params.id);

        if (!incident) return res.status(404).json({ error: true, message: 'Incident not found' });

        if (incident.assignedTo) {
            return res.status(400).json({ error: true, message: 'Incident is already assigned.' });
        }

        await incident.update({
            assignedTo: req.user.id,
            status: 'investigating'
        });

        // Emit real-time event
        req.io.emit('incident_assigned', { id: incident.id, userId: req.user.id });

        res.format({
            'text/html': () => {
                req.flash('success', 'You have claimed this incident.');
                res.redirect('/incidents');
            },
            'application/json': () => res.json({ message: 'Incident claimed', incident })
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};

// POST /incidents/panic
exports.triggerPanic = async (req, res) => {
    try {
        const { location, patrolRunId } = req.body;

        let geom = null;
        if (location && location.lat && location.lng) {
            geom = { type: 'Point', coordinates: [location.lat, location.lng] };
        }

        const panic = await PanicAlert.create({
            guardId: req.user.id,
            patrolRunId,
            location: geom,
            triggeredAt: new Date(),
            resolved: false
        });

        // Emit Critical Alert!
        req.io.emit('panic_alert', {
            id: panic.id,
            guard: req.user.name,
            location,
            time: panic.triggeredAt
        });

        res.json({ message: 'Panic alert sent', panic });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};

exports.monitor = async (req, res) => {
    res.render('incidents/monitor', { title: 'Live Command Center' });
};

exports.apiList = async (req, res) => {
    try {
        const incidents = await Incident.findAll({
            where: { status: ['new', 'investigating'] },
            include: [
                { model: User, as: 'reporter' },
                { model: User, as: 'assignee' },
                { model: Site }
            ],
            order: [['createdAt', 'DESC']]
        });

        const panics = await PanicAlert.findAll({
            where: { resolved: false },
            order: [['triggeredAt', 'DESC']]
        });

        res.json({ incidents, panics });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};

exports.resolve = async (req, res) => {
    try {
        const { type, notes } = req.body; // 'incident' or 'panic'

        if (type === 'panic') {
            await PanicAlert.update({ resolved: true }, { where: { id: req.params.id } });
            req.io.emit('panic_resolved', { id: req.params.id });
        } else {
            const incident = await Incident.findByPk(req.params.id);
            if (!incident) return res.status(404).json({ error: true, message: 'Incident not found' });

            if (!incident.assignedTo && req.user.Role.name.toLowerCase() === 'guard') {
                // Guards can only resolve if assigned
                return res.format({
                    'text/html': () => {
                        req.flash('error', 'You must be assigned to this incident to resolve it.');
                        res.redirect('/incidents');
                    },
                    'application/json': () => res.status(403).json({ error: true, message: 'Not assigned' })
                });
            }

            const evidencePath = req.file ? '/uploads/incidents/' + req.file.filename : incident.evidencePath;

            await incident.update({
                status: 'resolved',
                resolutionNotes: notes,
                evidencePath: evidencePath
            });

            req.io.emit('incident_resolved', { id: req.params.id });
        }

        res.format({
            'text/html': () => {
                req.flash('success', 'Issue resolved successfully!');
                res.redirect('/incidents');
            },
            'application/json': () => res.json({ success: true })
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};

exports.assign = async (req, res) => {
    try {
        const { userId } = req.body;
        const incident = await Incident.findByPk(req.params.id);

        if (!incident) return res.status(404).json({ error: true, message: 'Incident not found' });

        await incident.update({
            assignedTo: userId,
            status: 'investigating' // Auto-move to investigating
        });

        req.io.emit('incident_assigned', { id: incident.id, userId });

        res.json({ message: 'Incident assigned', incident });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};

exports.resolveApi = async (req, res) => {
    try {
        const { notes } = req.body;
        const evidencePath = req.file ? '/uploads/incidents/' + req.file.filename : null;

        const incident = await Incident.findByPk(req.params.id);
        if (!incident) return res.status(404).json({ error: true, message: 'Incident not found' });

        if (!incident.assignedTo) {
            return res.status(400).json({ error: true, message: 'Incident must be assigned before resolution.' });
        }

        await incident.update({
            status: 'resolved',
            resolutionNotes: notes,
            evidencePath: evidencePath || incident.evidencePath // Keep old if no new
        });

        req.io.emit('incident_resolved', { id: incident.id });

        res.json({ message: 'Incident resolved', incident });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};
