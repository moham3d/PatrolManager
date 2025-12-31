module.exports = (sequelize, DataTypes) => {
    const RolePermission = sequelize.define('RolePermission', {
        RoleId: {
            type: DataTypes.INTEGER,
            allowNull: false
        },
        PermissionId: {
            type: DataTypes.INTEGER,
            allowNull: false
        }
    }, {
        timestamps: true,
        indexes: [
            {
                unique: true,
                fields: ['RoleId', 'PermissionId']
            }
        ]
    });

    return RolePermission;
};
