const winston = require('winston');
const { format, transports } = winston;

const customFormat = format.combine(
    format.timestamp(),
    format.errors({ stack: true }),
    format.printf(({ timestamp, level, message, requestId, userId, ...meta }) => {
        return JSON.stringify({
            timestamp,
            level,
            requestId,
            userId,
            message,
            ...meta
        });
    })
);

const logger = winston.createLogger({
    level: process.env.LOG_LEVEL || 'info',
    format: customFormat,
    transports: [
        new transports.Console({
            format: format.combine(
                format.colorize(),
                format.simple()
            )
        }),
        new transports.File({
            filename: 'logs/app.log',
            maxsize: 5242880, // 5MB
            maxFiles: 5,
            tailable: true
        }),
        new transports.File({
            filename: 'logs/error.log',
            level: 'error',
            maxsize: 5242880,
            maxFiles: 10,
            tailable: true
        })
    ]
});

// Request ID Middleware
const requestLoggerMiddleware = (req, res, next) => {
    req.requestId = Math.random().toString(36).substring(2, 15);
    res.on('finish', () => {
        logger.info(req.method, {
            requestId: req.requestId,
            method: req.method,
            url: req.url,
            statusCode: res.statusCode,
            userId: req.user?.id
        });
    });
    next();
};

module.exports = { logger, requestLoggerMiddleware };
