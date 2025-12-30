module.exports = (sequelize, DataTypes) => {
    const Visitor = sequelize.define('Visitor', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        name: {
            type: DataTypes.STRING,
            allowNull: false
        },
        email: {
            type: DataTypes.STRING,
            allowNull: true
        },
        phone: {
            type: DataTypes.STRING,
            allowNull: true
        },
        hostName: {
            type: DataTypes.STRING,
            allowNull: false
        },
        purpose: {
            type: DataTypes.STRING,
            allowNull: true
        },
        expectedArrivalTime: {
            type: DataTypes.DATE,
            allowNull: false
        },
        checkInTime: {
            type: DataTypes.DATE,
            allowNull: true
        },
        checkOutTime: {
            type: DataTypes.DATE,
            allowNull: true
        },
        status: {
            type: DataTypes.ENUM('expected', 'checked_in', 'checked_out', 'cancelled', 'banned'),
            defaultValue: 'expected'
        }
    });

    Visitor.associate = (models) => {
        Visitor.belongsTo(models.Site, { foreignKey: 'siteId', as: 'site' });
    };

    return Visitor;
};
