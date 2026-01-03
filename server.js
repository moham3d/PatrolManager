const express = require('express');
const app = express();
const path = require('path');
const morgan = require('morgan');
const passport = require('passport');
const session = require('express-session');
const cors = require('cors');
const http = require('http');
const socketIo = require('socket.io');

// Load environment variables
require('dotenv').config();

// Configs
const db = require('./src/config/database');
const socketHandler = require('./src/sockets/socketHandler');

const { generalRateLimit, authRateLimit, apiRateLimit, panicRateLimit } = require('./src/middleware/rateLimiter');

// Initialize Server
const server = http.createServer(app);
const io = socketHandler.init(server);

// Apply general rate limiting to all requests
app.use(generalRateLimit);

// Helmet Security Headers
app.use(require('./src/middleware/helmet'));

// Middleware
app.use(morgan('dev'));
app.use(require('./src/config/cors'));
app.use((req, res, next) => {
    req.io = io;
    next();
});
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, 'src/public')));

// View Engine Setup
app.set('views', path.join(__dirname, 'src/views'));
app.set('view engine', 'ejs');

const flash = require('connect-flash');
// ... other imports

// Session Setup (For Web)
app.use(session({
    secret: process.env.SESSION_SECRET || 'secret',
    resave: false,
    saveUninitialized: false,
    cookie: {
        maxAge: 24 * 60 * 60 * 1000, // 24 hours
        secure: process.env.NODE_ENV === 'production',
        httpOnly: true,
        sameSite: 'strict'
    }
}));

// Flash Messages
app.use(flash());

// Make user and flash available in all views
app.use(passport.initialize());
app.use(passport.session());
require('./src/config/passport')(passport);

const csrfProtection = require('./src/middleware/csrf');
app.use((req, res, next) => {
    if (req.path.startsWith('/api')) {
        return next();
    }
    csrfProtection(req, res, next);
});

// Make user, flash and csrf token available in all views
app.use((req, res, next) => {
    res.locals.user = req.user;
    res.locals.success = req.flash('success');
    res.locals.error = req.flash('error');
    if (req.csrfToken) {
        res.locals.csrfToken = req.csrfToken();
    }
    next();
});

app.get('/debug/user', (req, res) => {
    res.json(req.user || { message: 'No user logged in' });
});

// Routes
app.use('/', require('./src/routes'));
app.use('/reports', require('./src/routes/reports'));
app.use('/schedules', require('./src/routes/schedules'));

// Global Error Handler
app.use((err, req, res, next) => {
    console.error(err.stack);

    res.format({
        'text/html': function () {
            res.status(err.status || 500);
            res.render('error', {
                message: err.message,
                error: process.env.NODE_ENV === 'development' ? err : {}
            });
        },
        'application/json': function () {
            res.status(err.status || 500).json({
                error: true,
                message: err.message
            });
        }
    });
});

// Socket.io
io.on('connection', (socket) => {
    console.log('Client connected');
});

// Start Cron Jobs
require('./src/cron/attendanceMonitor')(io);

// Start Server
const PORT = process.env.PORT || 3000;
// db.authenticate()
//     .then(() => console.log('Database connected...'))
//     .catch(err => console.log('Error: ' + err));

// Sync Database (Dev only - be careful in production)
// Sync Database (Dev only - be careful in production)
// db.sync({ alter: true }).then(() => {
server.listen(PORT, '0.0.0.0', () => {
    console.log(`Server started on http://localhost:${PORT}`);
});
// });

module.exports = { app, io };
