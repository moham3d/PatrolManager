const rateLimit = require('express-rate-limit');

// Strict limit for auth routes
exports.authRateLimit = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 100, // 100 attempts (Dev mode relaxed)
  message: {
    success: false,
    message: 'Too many login attempts. Please try again later.'
  },
  skipSuccessfulRequests: true,
  standardHeaders: true,
  legacyHeaders: false,
});

// Panic button - allow fewer attempts
exports.panicRateLimit = rateLimit({
  windowMs: 60 * 1000 * 15, // 15 minutes
  max: 3, // 3 panic alerts per 15 min
  message: {
    success: false,
    message: 'Too many panic alerts. Please contact dispatch directly.'
  }
});

// Standard API limit
exports.apiRateLimit = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 100, // 100 requests
  message: {
    success: false,
    message: 'Too many requests. Please slow down.'
  }
});

// General limit for all other routes
exports.generalRateLimit = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 1000, // 1000 requests per minute
  message: {
    success: false,
    message: 'Too many requests from this IP, please try again after a minute'
  }
});