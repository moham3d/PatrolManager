module.exports = (sequelize, DataTypes) => {
    const CheckpointVisit = sequelize.define('CheckpointVisit', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        scannedAt: {
            type: DataTypes.DATE,
            defaultValue: DataTypes.NOW
        },
        // Geotagging the scan event (to compare with expected location)
        location: {
            type: DataTypes.JSON, // Stores { lat, lng }
            allowNull: true
        },
        // Valid, Invalid (Too far), Skipped
        status: {
            type: DataTypes.ENUM('valid', 'invalid', 'manual_override'),
            defaultValue: 'valid'
        },
        patrolRunId: {
            type: DataTypes.INTEGER,
            allowNull: false
        },
        checkpointId: {
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

    CheckpointVisit.associate = (models) => {
        CheckpointVisit.belongsTo(models.PatrolRun, { foreignKey: 'patrolRunId' });
        CheckpointVisit.belongsTo(models.Checkpoint, { foreignKey: 'checkpointId' }); // The checkpoint that was supposed to be hit
    };

    return CheckpointVisit;
};
