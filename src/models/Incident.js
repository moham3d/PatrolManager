module.exports = (sequelize, DataTypes) => {
    const Incident = sequelize.define('Incident', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        type: {
            type: DataTypes.STRING, // e.g., 'Theft', 'Fire', 'Maintenance'
            allowNull: false
        },
        priority: {
            type: DataTypes.ENUM('low', 'medium', 'high', 'critical'),
            defaultValue: 'medium'
        },
        description: {
            type: DataTypes.TEXT,
            allowNull: true
        },
        status: {
            type: DataTypes.ENUM('new', 'investigating', 'resolved', 'closed'),
            defaultValue: 'new'
        },
        // Location of the incident
        lat: {
            type: DataTypes.FLOAT,
            allowNull: true
        },
        lng: {
            type: DataTypes.FLOAT,
            allowNull: true
        },
        // Path to attached evidence (image/audio)
        evidencePath: {
            type: DataTypes.STRING,
            allowNull: true
        },
        assignedTo: {
            type: DataTypes.INTEGER,
            allowNull: true
        },
        resolutionNotes: {
            type: DataTypes.TEXT,
            allowNull: true
        },
        zoneId: {
            type: DataTypes.INTEGER,
            allowNull: true
        },
        reporterId: {
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

    Incident.associate = (models) => {
        Incident.belongsTo(models.User, { foreignKey: 'reporterId', as: 'reporter' });
        Incident.belongsTo(models.User, { foreignKey: 'assignedTo', as: 'assignee' });
        Incident.belongsTo(models.Site, { foreignKey: 'siteId' });
        Incident.belongsTo(models.Zone, { foreignKey: 'zoneId' });
        Incident.hasMany(models.IncidentEvidence, { foreignKey: 'incidentId', as: 'evidence' });
    };

    return Incident;
};
