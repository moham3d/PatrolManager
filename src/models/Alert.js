module.exports = (sequelize, DataTypes) => {
    const Alert = sequelize.define('Alert', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        type: {
            type: DataTypes.ENUM('late_arrival', 'missed_patrol', 'panic', 'geofence_breach'),
            allowNull: false
        },
        status: {
            type: DataTypes.ENUM('new', 'acknowledged', 'resolved'),
            defaultValue: 'new'
        },
        message: {
            type: DataTypes.TEXT,
            allowNull: false
        },
        siteId: {
            type: DataTypes.INTEGER,
            allowNull: true
        },
        userId: {
            type: DataTypes.INTEGER,
            allowNull: true
        },
        metadata: {
            type: DataTypes.JSON,
            allowNull: true
        },
        acknowledgedBy: {
            type: DataTypes.INTEGER,
            allowNull: true
        },
        resolvedAt: {
            type: DataTypes.DATE,
            allowNull: true
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

    Alert.associate = (models) => {
        Alert.belongsTo(models.Site, { foreignKey: 'siteId', as: 'site' });
        Alert.belongsTo(models.User, { foreignKey: 'userId', as: 'user' });
        Alert.belongsTo(models.User, { foreignKey: 'acknowledgedBy', as: 'acknowledgedByUser' });
    };

    return Alert;
};
