module.exports = (sequelize, DataTypes) => {
    const Zone = sequelize.define('Zone', {
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
            type: DataTypes.STRING,
            allowNull: true
        }
    });

    Zone.associate = (models) => {
        Zone.belongsTo(models.Site, { foreignKey: 'siteId' });
        Zone.hasMany(models.Checkpoint, { foreignKey: 'zoneId', as: 'checkpoints' });
    };

    return Zone;
};
