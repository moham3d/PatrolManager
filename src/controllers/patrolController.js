const { PatrolTemplate, PatrolRun, CheckpointVisit, Site, Checkpoint, User, Shift } = require('../models');

// Helpers
const renderOrJson = (res, view, data) => {
    res.format({
        'text/html': () => res.render(view, data),
        'application/json': () => res.json(data)
    });
};

// --- Web Interface: Template Management ---

exports.index = async (req, res) => {
    try {
        const templates = await PatrolTemplate.findAll({
            include: [{ model: Site }]
        });
        renderOrJson(res, 'patrols/index', { title: 'Patrol Templates', templates });
    } catch (err) {
        console.error(err);
        res.status(500).send('Server Error');
    }
};

exports.show = async (req, res) => {
    try {
        const template = await PatrolTemplate.findByPk(req.params.id, {
            include: [{ model: Site }]
        });

        if (!template) return res.status(404).send('Patrol Route not found');

        // Fetch checkpoints maintaining the order stored in template.checkpointsList
        let orderedCheckpoints = [];
        if (template.checkpointsList && template.checkpointsList.length > 0) {
            const checkpoints = await Checkpoint.findAll({
                where: { id: template.checkpointsList }
            });

            // Re-order based on the ID list
            orderedCheckpoints = template.checkpointsList
                .map(id => checkpoints.find(cp => cp.id === id))
                .filter(cp => cp !== undefined); // Remove any that might have been deleted
        }

        renderOrJson(res, 'patrols/details', {
            title: template.name,
            template,
            checkpoints: orderedCheckpoints
        });

    } catch (err) {
        console.error(err);
        res.status(500).send('Server Error');
    }
};

exports.create = async (req, res) => {
    const sites = await Site.findAll();
    res.render('patrols/create', { title: 'Create Patrol Route', sites });
};

exports.store = async (req, res) => {
    try {
        // checkpointsList is expected to be an array of IDs from the form
        // e.g. [1, 2, 3]
        const { name, description, siteId, type, duration, checkpoints } = req.body;

        let checkpointsArr = [];
        if (checkpoints) {
            // Check if it matches comma-separated string from hidden input
            if (typeof checkpoints === 'string' && checkpoints.includes(',')) {
                checkpointsArr = checkpoints.split(',').map(Number);
            } else if (typeof checkpoints === 'string') {
                // Single ID
                checkpointsArr = [Number(checkpoints)];
            } else {
                // Array (fallback or if multiple inputs used)
                checkpointsArr = Array.isArray(checkpoints) ? checkpoints.map(Number) : [Number(checkpoints)];
            }
        }

        const template = await PatrolTemplate.create({
            name,
            description,
            siteId,
            type,
            estimatedDurationMinutes: duration,
            checkpointsList: checkpointsArr
        });

        res.format({
            'text/html': () => res.redirect('/patrols'),
            'application/json': () => res.status(201).json(template)
        });
    } catch (err) {
        console.error(err);
        res.status(400).send('Error creating template');
    }
};

// --- Mobile API: Officer Operations ---

// GET /patrols/my-today (For officer to see assigned patrols - simplified)
exports.myPatrols = async (req, res) => {
    try {
        console.log('GET /patrols/my-schedule hit by user:', req.user.id);
        const templates = await PatrolTemplate.findAll({
            include: [{ model: Site }]
        });

        // Enrich with Checkpoint Data
        const enrichedTemplates = await Promise.all(templates.map(async (tmpl) => {
            const t = tmpl.toJSON();
            if (t.checkpointsList && t.checkpointsList.length > 0) {
                const checkpoints = await Checkpoint.findAll({
                    where: { id: t.checkpointsList }
                });
                // Maintain order
                t.checkpoints = t.checkpointsList
                    .map(id => checkpoints.find(cp => cp.id === id))
                    .filter(cp => cp !== undefined);
            } else {
                t.checkpoints = [];
            }
            return t;
        }));

        console.log('Found templates:', enrichedTemplates.length);
        res.json(enrichedTemplates);
    } catch (err) {
        console.error('Error in myPatrols:', err);
        res.status(500).json({ error: true, message: err.message });
    }
};

// POST /patrols/start
exports.startPatrol = async (req, res) => {
    try {
        const { templateId } = req.body;
        const guardId = req.user.id;

        // 1. Check for Active Shift
        const activeShift = await Shift.findOne({
            where: {
                userId: guardId,
                status: 'active'
            }
        });

        if (!activeShift) {
            return res.status(403).json({
                error: true,
                message: 'You must Clock In before starting a patrol.'
            });
        }

        const run = await PatrolRun.create({
            templateId,
            guardId,
            shiftId: activeShift.id,
            startTime: new Date(),
            status: 'started'
        });

        res.json({
            message: 'Patrol started',
            runId: run.id,
            run
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};

// POST /patrols/checkpoint-visit
exports.scanCheckpoint = async (req, res) => {
    try {
        const { runId, checkpointId, location } = req.body; // location expected as { lat, lng }

        // 1. Verify Run
        const run = await PatrolRun.findByPk(runId, {
            include: [{ model: PatrolTemplate }]
        });

        if (!run || run.status !== 'started') {
            return res.status(400).json({ error: true, message: 'Invalid or completed patrol run' });
        }

        const template = run.PatrolTemplate;

        // 2. Verify Checkpoint Exists & Belongs to Template
        // Note: For basic robustness, we should check if checkpointId is IN the template list.
        if (template.checkpointsList && !template.checkpointsList.includes(parseInt(checkpointId))) {
            return res.status(400).json({ error: true, message: 'Checkpoint not part of this patrol route' });
        }

        const checkpoint = await Checkpoint.findByPk(checkpointId);
        if (!checkpoint) return res.status(404).json({ error: true, message: 'Checkpoint not found' });

        // 3. GPS Validation (Anti-Cheat)
        // Helper to calculate Haversine distance
        const getDistanceFromLatLonInM = (lat1, lon1, lat2, lon2) => {
            var R = 6371; // Radius of the earth in km
            var dLat = deg2rad(lat2 - lat1);
            var dLon = deg2rad(lon2 - lon1);
            var a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
            var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            var d = R * c; // Distance in km
            return d * 1000; // meters
        }
        const deg2rad = (deg) => deg * (Math.PI / 180);

        if (location && location.lat && location.lng && checkpoint.lat && checkpoint.lng) {
            const dist = getDistanceFromLatLonInM(location.lat, location.lng, checkpoint.lat, checkpoint.lng);
            const TOLERANCE_METERS = 500; // Liberal tolerance for demo, production should be 50m

            if (dist > TOLERANCE_METERS) {
                return res.status(400).json({
                    error: true,
                    message: `GPS Mismatch. You are ${Math.round(dist)}m away from the checkpoint (Max ${TOLERANCE_METERS}m).`
                });
            }
        }

        // 4. Sequence Validation (Ordered Patrols)
        if (template.type === 'ordered') {
            // Find last scan for this run
            const lastVisit = await CheckpointVisit.findOne({
                where: { patrolRunId: runId },
                order: [['createdAt', 'DESC']]
            });

            const expectedIndex = lastVisit ?
                template.checkpointsList.indexOf(lastVisit.checkpointId) + 1 :
                0; // Start at 0 if no visits

            const scannedIndex = template.checkpointsList.indexOf(parseInt(checkpointId));

            if (scannedIndex !== expectedIndex) {
                return res.status(400).json({
                    error: true,
                    message: `Out of order. Expected checkpoint #${expectedIndex + 1}, but scanned #${scannedIndex + 1}.`
                });
            }
        }

        // 5. Log Visit
        let geom = null;
        if (location && location.lat && location.lng) {
            geom = { type: 'Point', coordinates: [location.lat, location.lng] };
        }

        const visit = await CheckpointVisit.create({
            patrolRunId: runId,
            checkpointId,
            scannedAt: new Date(),
            location: geom,
            status: 'valid'
        });

        res.json({
            message: 'Checkpoint scanned',
            visit
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: true, message: err.message });
    }
};

// POST /patrols/end
exports.endPatrol = async (req, res) => {
    try {
        const { runId } = req.body;
        const run = await PatrolRun.findByPk(runId);

        run.endTime = new Date();
        run.status = 'completed';
        run.completionPercentage = 100; // Simplified
        await run.save();

        res.json({ message: 'Patrol completed' });
    } catch (err) {
        res.status(500).json({ error: true, message: err.message });
    }
};
