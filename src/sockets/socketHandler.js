const { Server } = require("socket.io");
const { Shift, Op } = require('../models');

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

    io.on("connection", (socket) => {
        console.log("New client connected", socket.id);

        socket.on("join_room", (room) => {
            socket.join(room);
        });

        // User Identity & Tracking
        socket.on('register_user', (userData) => {
            console.log(`📡 Register User: ${userData.name} (${userData.role}) [${socket.id}]`);
            // userData: { userId, name, role }
            onlineUsers.set(socket.id, { ...userData, lastUpdate: new Date() });
            io.to('command_center').emit('user_connected', userData);
        });

        socket.on('update_location', async (loc) => {
            // loc: { lat, lng }
            const user = onlineUsers.get(socket.id);
            if (user) {
                // [AUDIT FIX] Privacy: Only allow updates if active shift exists
                try {
                    // Assuming 'Shift' model is available and imported
                    // Check for active shift
                    // We need to require Op from sequelize if we use it, but here we can just check 'active' status
                    // Note: In a real high-throughput scenario, we might cache this shift status
                    const activeShift = await Shift.findOne({
                        where: {
                            userId: user.userId,
                            status: 'active'
                        }
                    });

                    if (!activeShift) {
                        // console.warn(`Privacy Block: Ignored location from off-duty user ${user.name}`);
                        return; // Silent fail to preserve privacy
                    }

                    user.lat = loc.lat;
                    user.lng = loc.lng;
                    user.lastUpdate = new Date();

                    // Broadcast to command center
                    io.to('command_center').emit('user_location_update', {
                        userId: user.userId,
                        name: user.name,
                        role: user.role,
                        lat: user.lat,
                        lng: user.lng
                    });
                } catch (err) {
                    console.error("Error verifying shift for location update:", err);
                }
            }
        });

        socket.on("disconnect", () => {
            const user = onlineUsers.get(socket.id);
            if (user) {
                io.to('command_center').emit('user_disconnected', user.userId);
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
