module.exports = (sequelize, DataTypes) => {
    const PatrolTemplate = sequelize.define('PatrolTemplate', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        name: {
            type: DataTypes.STRING,
            allowNull: false
        },
        description: {
            type: DataTypes.TEXT,
            allowNull: true
        },
        // Store ordered list of checkpoint IDs as JSON for simplicity, 
        // or effectively efficient for read-heavy operations.
        // E.g. [12, 15, 18, 9]
        checkpointsList: {
            type: DataTypes.JSONB,
            allowNull: false,
            defaultValue: []
        },
        // Type of route enforcement
        type: {
            type: DataTypes.ENUM('ordered', 'random', 'freeroam'),
            defaultValue: 'ordered'
        },
        estimatedDurationMinutes: {
            type: DataTypes.INTEGER,
            defaultValue: 30
        },
        siteId: {
            type: DataTypes.INTEGER,
            allowNull: false
        }
    });

    PatrolTemplate.associate = (models) => {
        PatrolTemplate.belongsTo(models.Site, { foreignKey: 'siteId' });
        PatrolTemplate.hasMany(models.PatrolRun, { foreignKey: 'templateId' });
    };

    return PatrolTemplate;
};
