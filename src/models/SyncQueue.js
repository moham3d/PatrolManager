module.exports = (sequelize, DataTypes) => {
    const SyncQueue = sequelize.define('SyncQueue', {
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
        action: {
            type: DataTypes.STRING, // e.g., 'scan_checkpoint', 'report_incident', 'clock_in'
            allowNull: false
        },
        payload: {
            type: DataTypes.JSON,
            allowNull: false
        },
        status: {
            type: DataTypes.ENUM('pending', 'synced', 'failed'),
            defaultValue: 'pending'
        },
        attempts: {
            type: DataTypes.INTEGER,
            defaultValue: 0
        },
        lastAttempt: {
            type: DataTypes.DATE,
            allowNull: true
        },
        errorMessage: {
            type: DataTypes.TEXT,
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

    SyncQueue.associate = (models) => {
        SyncQueue.belongsTo(models.User, { foreignKey: 'userId', as: 'user' });
    };

    return SyncQueue;
};
