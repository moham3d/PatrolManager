const cors = require('cors');

const allowedOrigins = process.env.ALLOWED_ORIGINS?.split(',') || [
  'http://localhost:3000',
  'http://localhost:8080' // Android emulator
];

const corsOptions = {
  origin: (origin, callback) => {
    if (!origin) return callback(null, true); // Allow same-origin

    if (allowedOrigins.includes(origin)) {
      callback(null, true);
    } else {
      callback(new Error('Not allowed by CORS'));
    }
  },
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Device-ID'],
  optionsSuccessStatus: 204
};

module.exports = cors(corsOptions);
