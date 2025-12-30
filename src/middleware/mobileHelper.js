module.exports = (req, res, next) => {
    // Check for Custom Mobile Headers
    const deviceId = req.header('X-Device-ID');

    if (deviceId) {
        req.isMobile = true;
        // Potential logic: Log device access, validate device ID against whitelist
    } else {
        req.isMobile = false;
    }
    next();
};
