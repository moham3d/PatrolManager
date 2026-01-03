module.exports = (sequelize, DataTypes) => {
    const ReportSchedule = sequelize.define('ReportSchedule', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        userId: {
            type: DataTypes.INTEGER,
            allowNull: false
        },
        reportType: {
            type: DataTypes.ENUM('incident', 'patrol', 'shift', 'visitor'),
            allowNull: false
        },
        frequency: {
            type: DataTypes.ENUM('daily', 'weekly', 'monthly'),
            allowNull: false
        },
        email: {
            type: DataTypes.STRING,
            allowNull: false
        },
        lastRun: {
            type: DataTypes.DATE,
            allowNull: true
        },
        isActive: {
            type: DataTypes.BOOLEAN,
            defaultValue: true
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

    ReportSchedule.associate = (models) => {
        ReportSchedule.belongsTo(models.User, { foreignKey: 'userId', as: 'user' });
    };

    return ReportSchedule;
};
