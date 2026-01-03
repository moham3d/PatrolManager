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
        },
        siteId: {
            type: DataTypes.INTEGER,
            allowNull: false
        },
        zoneId: {
            type: DataTypes.INTEGER,
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

    Checkpoint.associate = (models) => {
        Checkpoint.belongsTo(models.Site, { foreignKey: 'siteId' });
        Checkpoint.belongsTo(models.Zone, { foreignKey: 'zoneId' });
    };

    return Checkpoint;
};
