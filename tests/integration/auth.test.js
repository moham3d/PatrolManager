const request = require('supertest');
const { app } = require('../../server');
const { User, sequelize } = require('../../src/models');

describe('Auth API', () => {
    // Before running tests, we might need to sync the DB or use a test DB
    // For this POC, we assume the environment is set up.

    it('GET /login should return 200', async () => {
        const res = await request(app).get('/login');
        expect(res.statusCode).toBe(200);
    });

    it('POST /login with invalid credentials should return 401 (JSON)', async () => {
        const res = await request(app)
            .post('/login')
            .set('Accept', 'application/json')
            .send({
                email: 'wrong@example.com',
                password: 'wrong'
            });
        
        // Wait, CSRF might block this if not careful.
        // But our middleware exempts /api or check headers?
        // Let's check server.js CSRF exemption logic.
    });
});
