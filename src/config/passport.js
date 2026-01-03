const LocalStrategy = require('passport-local').Strategy;
const JwtStrategy = require('passport-jwt').Strategy;
const ExtractJwt = require('passport-jwt').ExtractJwt;
const { User, Role, Permission } = require('../models');

const MAX_ATTEMPTS = 5;
const LOCKOUT_TIME = 15 * 60 * 1000; // 15 minutes
const failedAttempts = new Map();

module.exports = function (passport) {
    // 1. Local Strategy (for Web Sessions)
    passport.use(new LocalStrategy({ usernameField: 'email' }, async (email, password, done) => {
        const key = `login_${email}`;
        const attempts = failedAttempts.get(key) || 0;

        const lockoutEnd = failedAttempts.get(`${key}_locked_until`);
        if (lockoutEnd && Date.now() < lockoutEnd) {
            const remainingTime = Math.ceil((lockoutEnd - Date.now()) / 60000);
            return done(null, false, {
                message: `Account locked. Try again in ${remainingTime} minutes.`
            });
        }

        try {
            const user = await User.findOne({
                where: { email },
                include: [{
                    model: Role,
                    include: [Permission]
                }]
            });
            if (!user) {
                failedAttempts.set(key, attempts + 1);
                if (attempts + 1 >= MAX_ATTEMPTS) {
                    failedAttempts.set(`${key}_locked_until`, Date.now() + LOCKOUT_TIME);
                }
                return done(null, false, { message: 'That email is not registered' });
            }

            // The User model defined 'validPassword' on prototype.
            const isMatch = await user.validPassword(password);

            if (isMatch) {
                failedAttempts.delete(key);
                failedAttempts.delete(`${key}_locked_until`);
                return done(null, user);
            } else {
                failedAttempts.set(key, attempts + 1);
                if (attempts + 1 >= MAX_ATTEMPTS) {
                    failedAttempts.set(`${key}_locked_until`, Date.now() + LOCKOUT_TIME);
                }
                return done(null, false, { message: 'Password incorrect' });
            }
        } catch (err) {
            return done(err);
        }
    }));

    // 2. JWT Strategy (for Mobile API)
    const opts = {
        jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
        secretOrKey: process.env.JWT_SECRET
    };

    passport.use(new JwtStrategy(opts, async (jwt_payload, done) => {
        try {
            const user = await User.findByPk(jwt_payload.id, {
                include: [{
                    model: Role,
                    include: [Permission]
                }]
            });
            if (user) return done(null, user);
            return done(null, false);
        } catch (err) {
            return done(err, false);
        }
    }));

    passport.serializeUser(function (user, done) {
        done(null, user.id);
    });

    passport.deserializeUser(async function (id, done) {
        try {
            const user = await User.findByPk(id, {
                include: [{
                    model: Role,
                    include: [Permission]
                }]
            });
            done(null, user);
        } catch (err) {
            done(err, null);
        }
    });
};
