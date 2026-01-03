module.exports = (sequelize, DataTypes) => {
    const AuditLog = sequelize.define('AuditLog', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        userId: {
            type: DataTypes.INTEGER,
            allowNull: true,
            references: {
                model: 'Users',
                key: 'id'
            }
        },
        action: {
            type: DataTypes.STRING, // e.g., 'CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'SOCKET_EVENT'
            allowNull: false
        },
        entity: {
            type: DataTypes.STRING, // e.g., 'Site', 'User', 'Incident', 'PatrolRun'
            allowNull: true
        },
        entityId: {
            type: DataTypes.INTEGER,
            allowNull: true
        },
        details: {
            type: DataTypes.JSON, // Stores old/new values or event metadata
            allowNull: true
        },
        ipAddress: {
            type: DataTypes.STRING,
            allowNull: true
        },
        userAgent: {
            type: DataTypes.STRING,
            allowNull: true
        },
        timestamp: {
            type: DataTypes.DATE,
            defaultValue: DataTypes.NOW
        }
    });

    AuditLog.associate = (models) => {
        AuditLog.belongsTo(models.User, { foreignKey: 'userId', as: 'user' });
    };

    return AuditLog;
};
