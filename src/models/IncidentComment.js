module.exports = (sequelize, DataTypes) => {
    const IncidentComment = sequelize.define('IncidentComment', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true
        },
        incidentId: {
            type: DataTypes.INTEGER,
            allowNull: false,
            references: {
                model: 'Incidents',
                key: 'id'
            }
        },
        userId: {
            type: DataTypes.INTEGER,
            allowNull: false,
            references: {
                model: 'Users',
                key: 'id'
            }
        },
        comment: {
            type: DataTypes.TEXT,
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

    IncidentComment.associate = (models) => {
        IncidentComment.belongsTo(models.Incident, { foreignKey: 'incidentId', as: 'incident' });
        IncidentComment.belongsTo(models.User, { foreignKey: 'userId', as: 'user' });
    };

    return IncidentComment;
};
