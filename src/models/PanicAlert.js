module.exports = (sequelize, DataTypes) => {
    const PanicAlert = sequelize.define('PanicAlert', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        resolved: {
            type: DataTypes.BOOLEAN,
            defaultValue: false
        },
        triggeredAt: {
            type: DataTypes.DATE,
            defaultValue: DataTypes.NOW
        },
        lat: {
            type: DataTypes.FLOAT,
            allowNull: true
        },
        lng: {
            type: DataTypes.FLOAT,
            allowNull: true
        },
        guardId: {
            type: DataTypes.INTEGER,
            allowNull: false
        },
        patrolRunId: {
            type: DataTypes.INTEGER,
            allowNull: true
        }
    });

    PanicAlert.associate = (models) => {
        PanicAlert.belongsTo(models.User, { foreignKey: 'guardId', as: 'guard' });
        PanicAlert.belongsTo(models.PatrolRun, { foreignKey: 'patrolRunId' }); // Optional, if during a patrol
    };

    return PanicAlert;
};
