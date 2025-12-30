const LocalStrategy = require('passport-local').Strategy;
const JwtStrategy = require('passport-jwt').Strategy;
const ExtractJwt = require('passport-jwt').ExtractJwt;
const { User, Role, Permission } = require('../models');

module.exports = function (passport) {
    // 1. Local Strategy (for Web Sessions)
    passport.use(new LocalStrategy({ usernameField: 'email' }, async (email, password, done) => {
        try {
            const user = await User.findOne({
                where: { email },
                include: [{
                    model: Role,
                    include: [Permission]
                }]
            });
            if (!user) return done(null, false, { message: 'That email is not registered' });

            // The User model defined 'validPassword' on prototype.
            const isMatch = await user.validPassword(password);

            if (isMatch) {
                return done(null, user);
            } else {
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
