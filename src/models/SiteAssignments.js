module.exports = (sequelize, DataTypes) => {
    const SiteAssignments = sequelize.define('SiteAssignments', {
        userId: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            references: {
                model: 'Users',
                key: 'id'
            }
        },
        siteId: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            references: {
                model: 'Sites',
                key: 'id'
            }
        }
    });

    return SiteAssignments;
};
