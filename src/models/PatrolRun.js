module.exports = (sequelize, DataTypes) => {
    const PatrolRun = sequelize.define('PatrolRun', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        status: {
            type: DataTypes.ENUM('started', 'completed', 'incomplete'),
            defaultValue: 'started'
        },
        startTime: {
            type: DataTypes.DATE,
            allowNull: false,
            defaultValue: DataTypes.NOW
        },
        endTime: {
            type: DataTypes.DATE,
            allowNull: true
        },
        completionPercentage: {
            type: DataTypes.INTEGER,
            defaultValue: 0
        },
        notes: {
            type: DataTypes.TEXT,
            allowNull: true
        },
        siteId: {
            type: DataTypes.INTEGER,
            allowNull: false
        },
        guardId: {
            type: DataTypes.INTEGER,
            allowNull: false
        },
        templateId: {
            type: DataTypes.INTEGER,
            allowNull: false
        },
        shiftId: {
            type: DataTypes.INTEGER,
            allowNull: true
        }
    });

    PatrolRun.associate = (models) => {
        PatrolRun.belongsTo(models.User, { foreignKey: 'guardId', as: 'guard' });
        PatrolRun.belongsTo(models.Site, { foreignKey: 'siteId', as: 'site' });
        PatrolRun.belongsTo(models.PatrolTemplate, { foreignKey: 'templateId', as: 'template' });
        PatrolRun.belongsTo(models.Shift, { foreignKey: 'shiftId', as: 'shift' });
        PatrolRun.hasMany(models.CheckpointVisit, { foreignKey: 'patrolRunId', as: 'visits' });
    };

    return PatrolRun;
};
