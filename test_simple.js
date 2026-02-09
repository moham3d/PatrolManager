const http = require('http');

// Config
const BASE_URL = 'http://localhost:3000';

async function testHealth() {
    console.log('📡 Testing Health Check...');
    try {
        const res = await fetch(`${BASE_URL}/api/health`);
        const data = await res.json();
        console.log('✅ Health Status:', res.status, data);
    } catch (e) {
        console.error('❌ Health Check Failed:', e.message);
    }
}

async function testLogin() {
    console.log('\n🔑 Testing Login (Guard)...');
    try {
        const res = await fetch(`${BASE_URL}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: 'admin@patrol.com', // Trying a default admin email usually found in seeds
                password: 'admin' 
            })
        });
        
        if (res.status === 200) {
            const data = await res.json();
            console.log('✅ Login Successful!');
            console.log('🎫 Token:', data.token ? 'Received' : 'Missing');
            return data.token;
        } else {
            console.log('⚠️ Login Failed (Status ' + res.status + ')');
            const err = await res.text();
            console.log('   Response:', err);
        }
    } catch (e) {
        console.error('❌ Login Error:', e.message);
    }
}

async function run() {
    await testHealth();
    await testLogin();
}

run();
