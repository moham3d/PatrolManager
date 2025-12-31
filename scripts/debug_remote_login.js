const http = require('http');

// Configuration
const HOST = 'app.digitexc.com';
const PORT = 80; // Changed to 80 for HTTP
const PATH = '/login';

// 1. Simulate Android Payload
const payload = JSON.stringify({
    email: 'admin@patrol.eg',
    password: 'password123'
});

const options = {
    hostname: HOST,
    port: PORT,
    path: PATH,
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json', // Android sends this
        'Content-Length': Buffer.byteLength(payload)
    }
};

console.log(`📡 Sending POST to http://${HOST}${PATH}...`);
console.log('📦 Payload:', payload);

const req = http.request(options, (res) => {
    console.log(`\n⬅️  Response Status: ${res.statusCode} ${res.statusMessage}`);
    console.log('Start Headers ----------------');
    console.log(res.headers);
    console.log('End Headers ------------------');

    let responseData = '';

    res.on('data', (chunk) => {
        responseData += chunk;
    });

    res.on('end', () => {
        console.log('\n📄 Body Preview:');
        console.log(responseData.substring(0, 500)); // Show first 500 chars

        console.log('\n🕵️  ANALYSIS:');
        if (res.statusCode === 302) {
            console.log('❌ SERVER IS REDIRECTING! This means the "auth.js" fix is NOT deployed.');
            console.log('   The server is trying to send you to the Dashboard HTML page.');
        } else if (responseData.trim().startsWith('<')) {
            console.log('❌ SERVER SENT HTML! This means the "auth.js" fix is NOT deployed.');
            console.log('   The Android app expected JSON but got an HTML page.');
        } else {
            try {
                JSON.parse(responseData);
                console.log('✅ SERVER SENT VALID JSON. The issue might be resolved.');
            } catch (e) {
                console.log('⚠️  SERVER SENT INVALID JSON:', e.message);
                console.log('   This explains the "Expected BEGIN_OBJECT but was STRING" error.');
            }
        }
    });
});

req.on('error', (e) => {
    console.error(`❌ Connection Error: ${e.message}`);
});

req.write(payload);
req.end();
