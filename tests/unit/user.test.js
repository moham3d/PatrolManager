const { User } = require('../../src/models');

describe('User Model', () => {
    it('should validate correctly with valid data', async () => {
        const user = User.build({
            name: 'Test User',
            email: 'test@example.com',
            password: 'password123'
        });
        await expect(user.validate()).resolves.not.toThrow();
    });

    it('should fail validation with invalid email', async () => {
        const user = User.build({
            name: 'Test User',
            email: 'not-an-email',
            password: 'password123'
        });
        await expect(user.validate()).rejects.toThrow();
    });
});
