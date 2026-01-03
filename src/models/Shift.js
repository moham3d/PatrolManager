module.exports = (sequelize, DataTypes) => {
    const Shift = sequelize.define('Shift', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        startTime: {
            type: DataTypes.DATE,
            allowNull: false
        },
        endTime: {
            type: DataTypes.DATE,
            allowNull: true
        },
        status: {
            type: DataTypes.ENUM('scheduled', 'active', 'completed'),
            defaultValue: 'scheduled'
        },
        startLocation: {
            type: DataTypes.JSON, // Stores { lat, lng }
            allowNull: true
        },
        endLocation: {
            type: DataTypes.JSON, // Stores { lat, lng }
            allowNull: true
        },
        userId: {
            type: DataTypes.INTEGER,
            allowNull: false
        },
        siteId: {
            type: DataTypes.INTEGER,
            allowNull: false
        },
        createdBy: {
            type: DataTypes.INTEGER,
            allowNull: true
        },
        updatedBy: {
            type: DataTypes.INTEGER,
            allowNull: true
        },
        deletedAt: {
            type: DataTypes.DATE,
            allowNull: true
        }
    });

    Shift.associate = (models) => {
        Shift.belongsTo(models.User, { foreignKey: 'userId', as: 'user' });
        Shift.belongsTo(models.Site, { foreignKey: 'siteId', as: 'site' });
        Shift.hasMany(models.PatrolRun, { foreignKey: 'shiftId', as: 'patrolRuns' });
    };

    return Shift;
};
