const { body, param, query, validationResult } = require('express-validator');

// Validation rules
const siteValidation = [
  body('name')
    .trim()
    .isLength({ min: 3, max: 100 })
    .withMessage('Site name must be 3-100 characters'),
  body('address')
    .trim()
    .notEmpty()
    .withMessage('Address is required'),
  body('lat')
    .isFloat({ min: -90, max: 90 })
    .withMessage('Latitude must be between -90 and 90'),
  body('lng')
    .isFloat({ min: -180, max: 180 })
    .withMessage('Longitude must be between -180 and 180')
];

const incidentValidation = [
  body('type')
    .isIn(['theft', 'vandalism', 'fire', 'maintenance', 'other'])
    .withMessage('Invalid incident type'),
  body('priority')
    .isIn(['low', 'medium', 'high', 'critical'])
    .withMessage('Invalid priority'),
  body('description')
    .trim()
    .isLength({ max: 5000 })
    .withMessage('Description too long (max 5000 chars)'),
  body('location.lat')
    .isFloat({ min: -90, max: 90 })
    .withMessage('Invalid latitude'),
  body('location.lng')
    .isFloat({ min: -180, max: 180 })
    .withMessage('Invalid longitude')
];

const patrolValidation = [
  body('name')
    .trim()
    .isLength({ min: 3, max: 100 })
    .withMessage('Patrol name must be 3-100 characters'),
  body('duration')
    .optional()
    .isInt({ min: 1 })
    .withMessage('Duration must be a positive number'),
  body('siteId')
    .isInt()
    .withMessage('Valid Site ID is required')
];

const validateRequest = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({
      success: false,
      message: 'Validation failed',
      errors: errors.array()
    });
  }
  next();
};

module.exports = {
  siteValidation,
  incidentValidation,
  patrolValidation,
  validateRequest
};
