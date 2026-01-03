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

    io.on("connection", (socket) => {
        console.log("New client connected", socket.id);
        const user = socket.user;
        onlineUsers.set(socket.id, {
            userId: user.id,
            name: user.name,
            role: user.Role.name,
            lastUpdate: new Date()
        });
        socket.join(`user_${user.id}`);
        io.to('command_center').emit('user_connected', {
            userId: user.id,
            name: user.name,
            role: user.Role.name
        });


        socket.on("join_room", (room) => {
            socket.join(room);
        });

        socket.on('update_location', async (loc) => {
            // loc: { lat, lng }
            const onlineUser = onlineUsers.get(socket.id);
            if (onlineUser) {
                // [AUDIT FIX] Privacy: Only allow updates if active shift exists
                try {
                    // Assuming 'Shift' model is available and imported
                    // Check for active shift
                    // We need to require Op from sequelize if we use it, but here we can just check 'active' status
                    // Note: In a real high-throughput scenario, we might cache this shift status
                    const activeShift = await Shift.findOne({
                        where: {
                            userId: onlineUser.userId,
                            status: 'active'
                        }
                    });

                    if (!activeShift) {
                        // console.warn(`Privacy Block: Ignored location from off-duty user ${onlineUser.name}`);
                        return; // Silent fail to preserve privacy
                    }

                    onlineUser.lat = loc.lat;
                    onlineUser.lng = loc.lng;
                    onlineUser.lastUpdate = new Date();

                    // Broadcast to command center
                    io.to('command_center').emit('user_location_update', {
                        userId: onlineUser.userId,
                        name: onlineUser.name,
                        role: onlineUser.role,
                        lat: onlineUser.lat,
                        lng: onlineUser.lng
                    });
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
