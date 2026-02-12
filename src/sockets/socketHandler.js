const { Server } = require("socket.io");
const { Shift, User, Role, Permission, GPSLog } = require('../models');
const { Op } = require('sequelize');
const jwt = require('jsonwebtoken');

let io;
const onlineUsers = new Map(); // Store { socketId: { userId, name, role, lat, lng, lastUpdate } }
const locationBuffer = new Map(); // Store { siteId: [locationUpdate] }

// Helper: Haversine Distance (Meters)
const getDistance = (lat1, lon1, lat2, lon2) => {
    const R = 6371e3; // Earth radius in meters
    const φ1 = lat1 * Math.PI / 180;
    const φ2 = lat2 * Math.PI / 180;
    const Δφ = (lat2 - lat1) * Math.PI / 180;
    const Δλ = (lon2 - lon1) * Math.PI / 180;

    const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
        Math.cos(φ1) * Math.cos(φ2) *
        Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c;
};

// Batch Location Updates Every 5 Seconds
setInterval(() => {
    if (!io) return;

    for (const [siteId, updates] of locationBuffer.entries()) {
        if (updates.length > 0) {
            const room = siteId === 'global' ? 'command_center' : `site_${siteId}`;
            io.to(room).to('admin').emit('location_batch', updates);
            updates.length = 0; // Clear the buffer
        }
    }
}, 5000);

exports.init = (httpServer) => {
    io = new Server(httpServer, {
        cors: {
            origin: "*",
            methods: ["GET", "POST"]
        }
    });

    io.use(async (socket, next) => {
        try {
            const token = socket.handshake.auth.token;
            if (!token) {
                return next(new Error('Authentication error: No token provided'));
            }

            const decoded = jwt.verify(token, process.env.JWT_SECRET);
            const user = await User.findByPk(decoded.id, {
                include: [Role]
            });

            if (!user) {
                return next(new Error('Authentication error: User not found'));
            }

            socket.user = user;
            next();
        } catch (error) {
            next(new Error(`Authentication error: ${error.message}`));
        }
    });

    io.on("connection", async (socket) => {
        const user = socket.user;
        const roleName = user.Role.name.toLowerCase();

        console.log(`[Socket] User Connected: ${user.name} (Role: ${roleName}, ID: ${socket.id})`);

        onlineUsers.set(socket.id, {
            userId: user.id,
            name: user.name,
            role: roleName,
            lastUpdate: new Date()
        });

        // 1. Join Personal Room
        socket.join(`user_${user.id}`);

        // 2. Join Role Room (Ensure lowercase for consistency)
        socket.join(roleName);
        console.log(`[Socket] ${user.name} joined room: ${roleName}`);

        // 3. Join Site Rooms
        const assignedSites = await user.getAssignedSites();
        for (const site of assignedSites) {
            socket.join(`site_${site.id}`);
            console.log(`[Socket] ${user.name} joined room: site_${site.id}`);
        }

        // 4. Join Global Voice Room
        socket.join('global_voice');

        io.to('command_center').emit('user_connected', {
            userId: user.id,
            name: user.name,
            role: roleName
        });


        socket.on("join_room", (room) => {
            socket.join(room);
            console.log(`[Socket] ${user.name} manually joined room: ${room}`);
        });

        // --- Walkie Talkie (PTT) ---
        socket.on('voice_message', async (data) => {
            const audio = data.audio || data.audioBlob;
            let siteId = data.siteId;

            if (!audio) return;

            // Auto-detect site if missing
            if (!siteId) {
                const activeShift = await Shift.findOne({
                    where: { userId: user.id, status: 'active' }
                });
                if (activeShift) siteId = activeShift.siteId;
            }

            const payload = {
                userId: user.id,
                userName: user.name,
                audio: audio,
                siteId: siteId || 'global'
            };

            if (siteId) {
                console.log(`[PTT] Voice from ${user.name} to site_${siteId} + admin`);
                // Broadcast to site AND all admins
                socket.to(`site_${siteId}`).to('admin').emit('voice_message', payload);
            } else {
                console.log(`[PTT] Voice from ${user.name} to global_voice + admin`);
                // Broadcast to global AND all admins
                socket.to('global_voice').to('admin').emit('voice_message', payload);
            }
        });

        // --- Emergency Events ---
        socket.on('panic_alert', (data, callback) => {
            if (!data || !data.location) {
                const err = { event: 'panic_alert', message: 'Missing location' };
                socket.emit('error', err);
                if (callback) callback({ success: false, ...err });
                return;
            }
            // data: { location, patrolRunId, siteId }
            const siteId = data.siteId;
            const payload = {
                ...data,
                userId: user.id,
                userName: user.name,
                timestamp: new Date()
            };

            // Broadcast to site supervisors and managers, and all admins
            if (siteId) {
                io.to(`site_${siteId}`).to('admin').emit('panic_alert', payload);
            } else {
                io.to('admin').to('manager').to('supervisor').emit('panic_alert', payload);
            }
            if (callback) callback({ success: true, message: 'Panic alert broadcasted' });
        });

        // --- Incident Events ---
        socket.on('incident_created', (data, callback) => {
            if (!data || !data.incidentId || !data.siteId) {
                const err = { event: 'incident_created', message: 'Missing required fields' };
                socket.emit('error', err);
                if (callback) callback({ success: false, ...err });
                return;
            }
            // data: { incidentId, type, priority, siteId }
            io.to(`site_${data.siteId}`).to('admin').emit('incident_created', data);
            if (callback) callback({ success: true, message: 'Incident notification sent' });
        });

        socket.on('incident_assigned', (data, callback) => {
            if (!data || !data.incidentId || !data.userId || !data.siteId) {
                const err = { event: 'incident_assigned', message: 'Missing required fields' };
                socket.emit('error', err);
                if (callback) callback({ success: false, ...err });
                return;
            }
            // data: { incidentId, userId, siteId }
            io.to(`user_${data.userId}`).emit('incident_assigned', data);
            io.to(`site_${data.siteId}`).to('admin').emit('incident_assigned', data);
            if (callback) callback({ success: true, message: 'Assignment notification sent' });
        });

        socket.on('incident_resolved', (data, callback) => {
            if (!data || !data.incidentId || !data.siteId) {
                const err = { event: 'incident_resolved', message: 'Missing required fields' };
                socket.emit('error', err);
                if (callback) callback({ success: false, ...err });
                return;
            }
            // data: { incidentId, siteId }
            io.to(`site_${data.siteId}`).to('admin').emit('incident_resolved', data);
            if (callback) callback({ success: true, message: 'Resolution notification sent' });
        });

        // --- Patrol Events ---
        socket.on('patrol_started', (data, callback) => {
            if (!data || !data.runId || !data.siteId) {
                const err = { event: 'patrol_started', message: 'Missing required fields' };
                socket.emit('error', err);
                if (callback) callback({ success: false, ...err });
                return;
            }
            // data: { runId, templateId, siteId }
            io.to(`site_${data.siteId}`).to('admin').emit('patrol_started', {
                ...data,
                userId: user.id,
                userName: user.name
            });
            if (callback) callback({ success: true, message: 'Patrol started notification sent' });
        });

        socket.on('patrol_completed', (data, callback) => {
            if (!data || !data.runId || !data.siteId) {
                const err = { event: 'patrol_completed', message: 'Missing required fields' };
                socket.emit('error', err);
                if (callback) callback({ success: false, ...err });
                return;
            }
            // data: { runId, siteId, completionPercentage }
            io.to(`site_${data.siteId}`).to('admin').emit('patrol_completed', data);
            if (callback) callback({ success: true, message: 'Patrol completed notification sent' });
        });

        socket.on('checkpoint_scanned', (data, callback) => {
            if (!data || !data.runId || !data.checkpointId || !data.siteId) {
                const err = { event: 'checkpoint_scanned', message: 'Missing required fields' };
                socket.emit('error', err);
                if (callback) callback({ success: false, ...err });
                return;
            }
            // data: { runId, checkpointId, siteId }
            io.to(`site_${data.siteId}`).to('admin').emit('checkpoint_scanned', data);
            if (callback) callback({ success: true, message: 'Scan notification sent' });
        });

        // --- Shift Events ---
        socket.on('shift_started', (data, callback) => {
            if (!data || !data.shiftId || !data.siteId) {
                const err = { event: 'shift_started', message: 'Missing required fields' };
                socket.emit('error', err);
                if (callback) callback({ success: false, ...err });
                return;
            }
            // data: { shiftId, siteId }
            io.to(`site_${data.siteId}`).to('admin').emit('shift_started', {
                ...data,
                userId: user.id,
                userName: user.name
            });
            if (callback) callback({ success: true, message: 'Shift started notification sent' });
        });

        socket.on('shift_ended', (data, callback) => {
            if (!data || !data.shiftId || !data.siteId) {
                const err = { event: 'shift_ended', message: 'Missing required fields' };
                socket.emit('error', err);
                if (callback) callback({ success: false, ...err });
                return;
            }
            // data: { shiftId, siteId }
            io.to(`site_${data.siteId}`).to('admin').emit('shift_ended', data);
            if (callback) callback({ success: true, message: 'Shift ended notification sent' });
        });

        socket.on('update_location', async (loc, callback) => {
            if (!loc || !loc.lat || !loc.lng) {
                const err = { event: 'update_location', message: 'Invalid location data' };
                socket.emit('error', err);
                if (callback) callback({ success: false, ...err });
                return;
            }
            const onlineUser = onlineUsers.get(socket.id);
            if (onlineUser) {
                try {
                    // Check for ACTIVE shift OR SCHEDULED shift starting soon
                    const now = new Date();
                    const oneHourFromNow = new Date(now.getTime() + 60 * 60 * 1000);

                    const validShift = await Shift.findOne({
                        where: {
                            userId: onlineUser.userId,
                            [Op.or]: [
                                { status: 'active' },
                                {
                                    status: 'scheduled',
                                    startTime: { [Op.lte]: oneHourFromNow }, // Starts within 1 hour
                                    endTime: { [Op.gt]: now } // Hasn't ended yet
                                }
                            ]
                        }
                    });

                    if (!validShift) {
                        if (callback) callback({ success: false, message: 'No active or upcoming shift' });
                        return;
                    }

                    // Update memory state
                    onlineUser.lat = loc.lat;
                    onlineUser.lng = loc.lng;
                    onlineUser.lastUpdate = new Date();

                    // Persist to Database
                    await GPSLog.create({
                        userId: onlineUser.userId,
                        lat: loc.lat,
                        lng: loc.lng,
                        accuracy: loc.accuracy || null,
                        timestamp: new Date()
                    });

                    const payload = {
                        userId: onlineUser.userId,
                        name: onlineUser.name,
                        role: onlineUser.role,
                        status: validShift.status, // Include shift status
                        lat: onlineUser.lat,
                        lng: onlineUser.lng,
                        timestamp: onlineUser.lastUpdate
                    };

                    // Add to buffer
                    const siteId = loc.siteId || 'global';
                    if (!locationBuffer.has(siteId)) {
                        locationBuffer.set(siteId, []);
                    }
                    locationBuffer.get(siteId).push(payload);

                    if (callback) callback({ success: true, message: 'Location updated' });
                } catch (err) {
                    console.error("Error verifying shift for location update:", err);
                    if (callback) callback({ success: false, message: err.message });
                }
            }
        });

        socket.on("disconnect", () => {
            const onlineUser = onlineUsers.get(socket.id);
            if (onlineUser) {
                io.to('command_center').emit('user_disconnected', onlineUser.userId);
                onlineUsers.delete(socket.id);
            }
            console.log("Client disconnected", socket.id);
        });

        // Send initial state to new command center clients
        socket.on('request_active_users', () => {
            const users = Array.from(onlineUsers.values()).filter(u => u.lat && u.lng);
            socket.emit('active_users_list', users);
        });
    });

    return io;
};

exports.getNearestGuards = (lat, lng, limit = 3) => {
    const guards = [];
    for (const [socketId, user] of onlineUsers.entries()) {
        if (user.role === 'guard' && user.lat && user.lng) {
            const distance = getDistance(lat, lng, user.lat, user.lng);
            guards.push({ socketId, ...user, distance });
        }
    }
    // Sort by distance ASC
    return guards.sort((a, b) => a.distance - b.distance).slice(0, limit);
};

exports.getIO = () => {
    if (!io) {
        throw new Error("Socket.io not initialized!");
    }
    return io;
};
