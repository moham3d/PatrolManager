const Sequelize = require('sequelize');
const db = require('../config/database');

const models = {
    User: require('./User')(db, Sequelize),
    Role: require('./Role')(db, Sequelize),
    Permission: require('./Permission')(db, Sequelize),
    Site: require('./Site')(db, Sequelize),
    Zone: require('./Zone')(db, Sequelize),
    Checkpoint: require('./Checkpoint')(db, Sequelize),
    PatrolTemplate: require('./PatrolTemplate')(db, Sequelize),
    PatrolRun: require('./PatrolRun')(db, Sequelize),
    CheckpointVisit: require('./CheckpointVisit')(db, Sequelize),
    Incident: require('./Incident')(db, Sequelize),
    PanicAlert: require('./PanicAlert')(db, Sequelize),
    Shift: require('./Shift')(db, Sequelize),
    Attendance: require('./Attendance')(db, Sequelize),
    Visitor: require('./Visitor')(db, Sequelize),
};

// Associate Models
Object.keys(models).forEach(modelName => {
    if (models[modelName].associate) {
        models[modelName].associate(models);
    }
});

models.sequelize = db;
models.Sequelize = Sequelize;

module.exports = models;
