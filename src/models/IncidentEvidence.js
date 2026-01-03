module.exports = (sequelize, DataTypes) => {
    const IncidentEvidence = sequelize.define('IncidentEvidence', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        incidentId: {
            type: DataTypes.INTEGER,
            allowNull: false,
            references: {
                model: 'Incidents',
                key: 'id'
            }
        },
        type: {
            type: DataTypes.ENUM('image', 'video', 'audio', 'document'),
            defaultValue: 'image'
        },
        filePath: {
            type: DataTypes.STRING,
            allowNull: false
        },
        fileName: {
            type: DataTypes.STRING,
            allowNull: true
        },
        fileSize: {
            type: DataTypes.INTEGER,
            allowNull: true
        },
        mimeType: {
            type: DataTypes.STRING,
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

    IncidentEvidence.associate = (models) => {
        IncidentEvidence.belongsTo(models.Incident, { foreignKey: 'incidentId', as: 'incident' });
    };

    return IncidentEvidence;
};
