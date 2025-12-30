const { Server } = require("socket.io");

let io;

exports.init = (httpServer) => {
    io = new Server(httpServer, {
        cors: {
            origin: "*",
            methods: ["GET", "POST"]
        }
    });

    const onlineUsers = new Map(); // Store { socketId: { userId, name, role, lat, lng, lastUpdate } }

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

        socket.on('update_location', (loc) => {
            // loc: { lat, lng }
            const user = onlineUsers.get(socket.id);
            if (user) {
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

exports.getIO = () => {
    if (!io) {
        throw new Error("Socket.io not initialized!");
    }
    return io;
};
