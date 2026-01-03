module.exports = (sequelize, DataTypes) => {
    const DeviceRegistration = sequelize.define('DeviceRegistration', {
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
        deviceId: {
            type: DataTypes.STRING,
            allowNull: false,
            unique: true
        },
        deviceFingerprint: {
            type: DataTypes.TEXT,
            allowNull: true
        },
        deviceModel: {
            type: DataTypes.STRING,
            allowNull: true
        },
        osVersion: {
            type: DataTypes.STRING,
            allowNull: true
        },
        lastLogin: {
            type: DataTypes.DATE,
            allowNull: true
        },
        isActive: {
            type: DataTypes.BOOLEAN,
            defaultValue: true
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

    DeviceRegistration.associate = (models) => {
        DeviceRegistration.belongsTo(models.User, { foreignKey: 'userId', as: 'user' });
    };

    return DeviceRegistration;
};
