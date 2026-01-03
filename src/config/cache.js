const NodeCache = require('node-cache');
const cache = new NodeCache({ stdTTL: 600, checkperiod: 120 }); // 10 mins default TTL

module.exports = {
    get: (key) => cache.get(key),
    set: (key, value, ttl) => cache.set(key, value, ttl),
    del: (key) => cache.del(key),
    flush: () => cache.flushAll(),
    
    // Middleware for route caching
    cacheMiddleware: (duration) => (req, res, next) => {
        const key = '__express__' + (req.originalUrl || req.url);
        const cachedResponse = cache.get(key);
        
        if (cachedResponse) {
            return res.send(cachedResponse);
        } else {
            res.sendResponse = res.send;
            res.send = (body) => {
                cache.set(key, body, duration);
                res.sendResponse(body);
            };
            next();
        }
    }
};
