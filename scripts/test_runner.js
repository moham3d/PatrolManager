const http = require('http');

// Configuration
const BASE_URL = 'http://localhost:3000';
const ADMIN_EMAIL = 'admin@example.com';
const ADMIN_PASSWORD = 'password123';
let AUTH_COOKIE = '';

// Helper to make HTTP requests
function request(method, path, body = null, headers = {}) {
    return new Promise((resolve, reject) => {
        const options = {
            hostname: 'localhost',
            port: 3000,
            path: path,
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                ...headers
            }
        };

        if (AUTH_COOKIE) {
            options.headers['Cookie'] = AUTH_COOKIE;
        }

        const req = http.request(options, (res) => {
            let data = '';

            // Capture cookies
            if (res.headers['set-cookie']) {
                const cookies = res.headers['set-cookie'].map(c => c.split(';')[0]);
                AUTH_COOKIE = cookies.join('; ');
            }

            res.on('data', (chunk) => data += chunk);
            res.on('end', () => {
                try {
                    const json = data ? JSON.parse(data) : {};
                    resolve({ status: res.statusCode, body: json, headers: res.headers });
                } catch (e) {
                    resolve({ status: res.statusCode, body: data, headers: res.headers });
                }
            });
        });

        req.on('error', reject);

        if (body) {
            req.write(JSON.stringify(body));
        }
        req.end();
    });
}

async function runTests() {
    console.log('--- Starting Automated Tests ---');

    try {
        // 1. Check Server Status
        console.log('\n[TEST 1] Creating Site (Should Fail without Auth)');
        const failSite = await request('POST', '/sites', { name: 'Hidden Base' });
        if (failSite.status === 302 || failSite.status === 401) {
            console.log('✅ Passed: Access denied as expected.');
        } else {
            console.error('❌ Failed: Should have been redirected/unauthorized, got', failSite.status);
        }

        // 2. Login
        console.log('\n[TEST 2] Admin Login');
        // Note: Passport-local usually uses POST /login with username/password
        // But we haven't implemented a POST /login route explicitly in our router plan? 
        // Wait, looking at routes/index.js... I might have missed creating the login route!
        // The implementation plan had it, but did I write it?
        // Let's check if login route exists. If not, this test will reveal a bug!

        // Assuming /login exists or creating it if missing is part of the fix.
        // Let's try to hit /login.
        // Actually, the auth middleware and passport config are there, but the ROUTE handler might be missing.
        // I recall writing 'router.get' for home, sites, patrols, etc. but not explicitly 'router.post("/login")'.
        // This test will likely fail, correctly identifying a bug.

        const loginRes = await request('POST', '/login', { email: ADMIN_EMAIL, password: ADMIN_PASSWORD });
        if (loginRes.status === 200 || loginRes.status === 302) {
            console.log('✅ Passed: Login successful.');
        } else {
            console.log('⚠️  Warning: Login failed (Status ' + loginRes.status + '). Route might be missing.');
            // If login fails, subsequent tests will fail.
        }

        // 3. Create Site (With Session?)
        console.log('\n[TEST 3] Create Site (Authorized)');
        const site = await request('POST', '/sites', { name: 'Test Site', address: '123 Test Lane' });
        if (site.status === 201 || site.status === 302) { // 302 if redirecting to view
            console.log('✅ Passed: Site created.');
        } else {
            console.log('❌ Failed: Could not create site.', site.body);
        }

        // 4. List Sites (JSON)
        console.log('\n[TEST 4] List Sites (JSON)');
        const sitesList = await request('GET', '/sites'); // Content-Neg is JSON by default in helper
        if (sitesList.status === 200 && Array.isArray(sitesList.body) || sitesList.body.sites) {
            console.log('✅ Passed: Retrieved sites list.');
        } else {
            console.log('❌ Failed: Could not list sites.', sitesList.body);
        }

        console.log('\n--- Tests Completed ---');

    } catch (err) {
        console.error('Test Runner Error:', err);
    }
}

// Check if login route needs to be added before running? 
// No, the user asked to Find bugs. If the route is missing, that is a bug.
runTests();
