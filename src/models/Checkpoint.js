module.exports = (sequelize, DataTypes) => {
    const Checkpoint = sequelize.define('Checkpoint', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        name: {
            type: DataTypes.STRING,
            allowNull: false
        },
        type: {
            type: DataTypes.ENUM('nfc', 'qr', 'gps'),
            defaultValue: 'nfc'
        },
        // Unique identifier on the tag itself
        uid: {
            type: DataTypes.STRING,
            allowNull: true,
            unique: true
        },
        lat: {
            type: DataTypes.FLOAT,
            allowNull: true
        },
        lng: {
            type: DataTypes.FLOAT,
            allowNull: true
        }
    });

    Checkpoint.associate = (models) => {
        Checkpoint.belongsTo(models.Site, { foreignKey: 'siteId' });
        Checkpoint.belongsTo(models.Zone, { foreignKey: 'zoneId' });
    };

    return Checkpoint;
};
