const { Server } = require("socket.io");
const { Shift, User, Role, Permission } = require('../models');
const jwt = require('jsonwebtoken');

let io;
const onlineUsers = new Map(); // Store { socketId: { userId, name, role, lat, lng, lastUpdate } }

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
                include: [{
                    model: Role,
                    include: [Permission]
                }]
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
        console.log("New client connected", socket.id);
        const user = socket.user;
        const roleName = user.Role.name.toLowerCase();

        onlineUsers.set(socket.id, {
            userId: user.id,
            name: user.name,
            role: roleName,
            lastUpdate: new Date()
        });

        // 1. Join Personal Room
        socket.join(`user_${user.id}`);

        // 2. Join Role Room
        socket.join(roleName);

        // 3. Join Site Rooms
        const assignedSites = await user.getAssignedSites();
        for (const site of assignedSites) {
            socket.join(`site_${site.id}`);
        }

        io.to('command_center').emit('user_connected', {
            userId: user.id,
            name: user.name,
            role: roleName
        });


        socket.on("join_room", (room) => {
            socket.join(room);
        });

        // --- Emergency Events ---
        socket.on('panic_alert', (data) => {
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
        });

        // --- Incident Events ---
        socket.on('incident_created', (data) => {
            // data: { incidentId, type, priority, siteId }
            io.to(`site_${data.siteId}`).to('admin').emit('incident_created', data);
        });

        socket.on('incident_assigned', (data) => {
            // data: { incidentId, userId, siteId }
            io.to(`user_${data.userId}`).emit('incident_assigned', data);
            io.to(`site_${data.siteId}`).to('admin').emit('incident_assigned', data);
        });

        socket.on('incident_resolved', (data) => {
            // data: { incidentId, siteId }
            io.to(`site_${data.siteId}`).to('admin').emit('incident_resolved', data);
        });

        // --- Patrol Events ---
        socket.on('patrol_started', (data) => {
            // data: { runId, templateId, siteId }
            io.to(`site_${data.siteId}`).to('admin').emit('patrol_started', {
                ...data,
                userId: user.id,
                userName: user.name
            });
        });

        socket.on('patrol_completed', (data) => {
            // data: { runId, siteId, completionPercentage }
            io.to(`site_${data.siteId}`).to('admin').emit('patrol_completed', data);
        });

        socket.on('checkpoint_scanned', (data) => {
            // data: { runId, checkpointId, siteId }
            io.to(`site_${data.siteId}`).to('admin').emit('checkpoint_scanned', data);
        });

        // --- Shift Events ---
        socket.on('shift_started', (data) => {
            // data: { shiftId, siteId }
            io.to(`site_${data.siteId}`).to('admin').emit('shift_started', {
                ...data,
                userId: user.id,
                userName: user.name
            });
        });

        socket.on('shift_ended', (data) => {
            // data: { shiftId, siteId }
            io.to(`site_${data.siteId}`).to('admin').emit('shift_ended', data);
        });

        socket.on('update_location', async (loc) => {
            // loc: { lat, lng, siteId }
            const onlineUser = onlineUsers.get(socket.id);
            if (onlineUser) {
                try {
                    const activeShift = await Shift.findOne({
                        where: {
                            userId: onlineUser.userId,
                            status: 'active'
                        }
                    });

                    if (!activeShift) {
                        return;
                    }

                    onlineUser.lat = loc.lat;
                    onlineUser.lng = loc.lng;
                    onlineUser.lastUpdate = new Date();

                    const payload = {
                        userId: onlineUser.userId,
                        name: onlineUser.name,
                        role: onlineUser.role,
                        lat: onlineUser.lat,
                        lng: onlineUser.lng
                    };

                    // Targeted broadcast to site room and command center (admins)
                    if (loc.siteId) {
                        io.to(`site_${loc.siteId}`).to('command_center').emit('user_location_update', payload);
                    } else {
                        io.to('command_center').emit('user_location_update', payload);
                    }
                } catch (err) {
                    console.error("Error verifying shift for location update:", err);
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
