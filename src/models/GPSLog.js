module.exports = (sequelize, DataTypes) => {
    const GPSLog = sequelize.define('GPSLog', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        userId: {
            type: DataTypes.INTEGER,
            allowNull: false,
            references: {
                model: 'Users',
                key: 'id'
            }
        },
        lat: {
            type: DataTypes.FLOAT,
            allowNull: false
        },
        lng: {
            type: DataTypes.FLOAT,
            allowNull: false
        },
        accuracy: {
            type: DataTypes.FLOAT,
            allowNull: true
        },
        timestamp: {
            type: DataTypes.DATE,
            defaultValue: DataTypes.NOW
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

    GPSLog.associate = (models) => {
        GPSLog.belongsTo(models.User, { foreignKey: 'userId', as: 'user' });
    };

    return GPSLog;
};
