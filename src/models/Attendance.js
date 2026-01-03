module.exports = (sequelize, DataTypes) => {
    const Attendance = sequelize.define('Attendance', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        type: {
            type: DataTypes.ENUM('clock_in', 'clock_out'),
            allowNull: false
        },
        timestamp: {
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
        method: {
            type: DataTypes.STRING, // gps, bio, manual
            defaultValue: 'gps'
        },
        userId: {
            type: DataTypes.INTEGER,
            allowNull: false
        },
        siteId: {
            type: DataTypes.INTEGER,
            allowNull: false
        },
        shiftId: {
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

    Attendance.associate = (models) => {
        Attendance.belongsTo(models.User, { foreignKey: 'userId', as: 'user' });
        Attendance.belongsTo(models.Site, { foreignKey: 'siteId', as: 'site' });
        Attendance.belongsTo(models.Shift, { foreignKey: 'shiftId', as: 'shift' });
    };

    return Attendance;
};
