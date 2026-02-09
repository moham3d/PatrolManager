const axios = require('axios');
const io = require('socket.io-client');

// Configuration
const BASE_URL = 'http://localhost:3000'; // Assuming local backend
const GUARD_CREDENTIALS = {
    username: 'guard1', // Need to make sure this user exists or create one
    password: 'password123'
};

async function runTest() {
    console.log('🚀 Starting PatrolManager Backend Test...');

    try {
        // 1. Health Check
        console.log('\n📡 Checking Health...');
        try {
            const health = await axios.get(`${BASE_URL}/api/health`);
            console.log('✅ Health Check Passed:', health.data);
        } catch (e) {
            console.error('❌ Health Check Failed. Is server running?', e.message);
            return;
        }

        // 2. Login (Simulate Guard)
        console.log('\n🔑 Attempting Login...');
        let token;
        try {
            // Adjust endpoint based on actual routes (checking src/routes/auth.js later if this fails)
            const login = await axios.post(`${BASE_URL}/api/auth/login`, {
                email: 'guard@example.com', // Trying standard email format
                password: 'password'
            });
            token = login.data.token;
            console.log('✅ Login Successful. Token received.');
        } catch (e) {
            console.log('⚠️ Login failed (Expected if user not seeded).');
            console.log('   Response:', e.response ? e.response.data : e.message);
            console.log('   👉 Suggestion: We need to check database seeds.');
            return; // Stop if we can't login
        }

        // 3. Socket Connection (Real-time tracking)
        if (token) {
            console.log('\n🔌 Connecting to Socket.io...');
            const socket = io(BASE_URL, {
                auth: { token },
                transports: ['websocket']
            });

            socket.on('connect', () => {
                console.log('✅ Socket Connected! ID:', socket.id);
                
                // Simulate Location Update
                console.log('📍 Sending Location Ping...');
                socket.emit('location_update', {
                    lat: 30.0444, 
                    lng: 31.2357, // Cairo coordinates
                    timestamp: Date.now()
                });
            });

            socket.on('connect_error', (err) => {
                console.error('❌ Socket Connection Error:', err.message);
            });

            // Keep alive for a bit
            setTimeout(() => {
                socket.disconnect();
                console.log('👋 Test Finished.');
            }, 3000);
        }

    } catch (error) {
        console.error('💥 Unexpected Error:', error.message);
    }
}

runTest();
