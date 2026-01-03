module.exports = (sequelize, DataTypes) => {
    const Schedule = sequelize.define('Schedule', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        startTime: {
            type: DataTypes.DATE,
            allowNull: false
        },
        endTime: {
            type: DataTypes.DATE,
            allowNull: false
        },
        // 'scheduled', 'attended', 'missed'
        status: {
            type: DataTypes.ENUM('scheduled', 'attended', 'missed'),
            defaultValue: 'scheduled'
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

    Schedule.associate = (models) => {
        Schedule.belongsTo(models.User, { foreignKey: 'userId', as: 'user' });
        Schedule.belongsTo(models.Site, { foreignKey: 'siteId', as: 'site' });
    };

    return Schedule;
};
