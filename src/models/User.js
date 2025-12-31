const bcrypt = require('bcryptjs');

module.exports = (sequelize, DataTypes) => {
    const User = sequelize.define('User', {
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
            allowNull: false,
            unique: true,
            validate: {
                isEmail: true
            }
        },
        password: {
            type: DataTypes.STRING,
            allowNull: false
        },
        managerId: {
            type: DataTypes.INTEGER,
            allowNull: true
        },
        roleId: {
            type: DataTypes.INTEGER,
            allowNull: true
        },
        isActive: {
            type: DataTypes.BOOLEAN,
            defaultValue: true
        }
    }, {
        hooks: {
            beforeCreate: async (user) => {
                if (user.password) {
                    const salt = await bcrypt.genSalt(10);
                    user.password = await bcrypt.hash(user.password, salt);
                }
            },
            beforeUpdate: async (user) => {
                if (user.changed('password')) {
                    const salt = await bcrypt.genSalt(10);
                    user.password = await bcrypt.hash(user.password, salt);
                }
            }
        }
    });

    User.prototype.validPassword = async function (password) {
        return await bcrypt.compare(password, this.password);
    };

    User.associate = (models) => {
        User.belongsTo(models.Role, { foreignKey: 'roleId' });
        // Hierarchy
        User.belongsTo(models.User, { as: 'manager', foreignKey: 'managerId' });
        User.hasMany(models.User, { as: 'subordinates', foreignKey: 'managerId' });
        // Site Assignment
        User.belongsToMany(models.Site, { through: 'SiteAssignments', as: 'assignedSites', foreignKey: 'userId' });
    };

    return User;
};
