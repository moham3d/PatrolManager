const http = require('http');

// Config
const BASE_URL = 'http://localhost:3000';

async function testHealth() {
    console.log('📡 Testing Health Check...');
    try {
        const res = await fetch(`${BASE_URL}/api/health`);
        const data = await res.json();
        console.log('✅ Health Status:', res.status, data.status);
    } catch (e) {
        console.error('❌ Health Check Failed:', e.message);
    }
}

async function testLogin() {
    console.log('\n🔑 Testing Login (Endpoint: /login)...');
    try {
        const res = await fetch(`${BASE_URL}/login`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Accept': 'application/json' // Crucial for getting JSON back
            },
            body: JSON.stringify({
                email: 'admin@patrol.com', // Need to guess correct credentials
                password: 'password123'
            })
        });
        
        if (res.status === 200) {
            const data = await res.json();
            console.log('✅ Login Response:', data);
        } else {
            console.log('⚠️ Login Failed (Status ' + res.status + ')');
            // If it returns HTML (login page), print snippet
            const text = await res.text();
            if (text.includes('<!DOCTYPE html>')) {
                console.log('   Response: HTML Page (Check if JSON was accepted)');
            } else {
                console.log('   Response:', text.substring(0, 200));
            }
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
