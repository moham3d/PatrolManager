module.exports = (sequelize, DataTypes) => {
    const Site = sequelize.define('Site', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        name: {
            type: DataTypes.STRING,
            allowNull: false
        },
        address: {
            type: DataTypes.STRING,
            allowNull: true
        },
        // Coordinates
        lat: {
            type: DataTypes.FLOAT,
            allowNull: true
        },
        lng: {
            type: DataTypes.FLOAT,
            allowNull: true
        },
        details: {
            type: DataTypes.TEXT,
            allowNull: true
        },
        boundaries: {
            type: DataTypes.TEXT, // Store as JSON string or use JSONB if on Postgres
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

    Site.associate = (models) => {
        Site.hasMany(models.Zone, { foreignKey: 'siteId', as: 'zones' });
        Site.hasMany(models.Checkpoint, { foreignKey: 'siteId', as: 'checkpoints' });
        Site.hasMany(models.Shift, { foreignKey: 'siteId', as: 'shifts' });
        Site.hasMany(models.Incident, { foreignKey: 'siteId', as: 'incidents' });
        Site.hasMany(models.PatrolRun, { foreignKey: 'siteId', as: 'patrolRuns' });
        Site.belongsToMany(models.User, { through: 'SiteAssignments', as: 'staff', foreignKey: 'siteId' });
    };

    return Site;
};
