const { DataTypes } = require('sequelize');

module.exports = (sequelize) => {
  const Permission = sequelize.define('Permission', {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true
    },
    name: {
      type: DataTypes.STRING,
      allowNull: false,
      unique: true
    },
    slug: {
      type: DataTypes.STRING,
      allowNull: false,
      unique: true
    },
    description: {
      type: DataTypes.TEXT,
      allowNull: true
    },
    category: {
      type: DataTypes.STRING,
      allowNull: true
    },
    createdBy: {
      type: DataTypes.INTEGER,
      allowNull: true,
      references: {
        model: 'Users',
        key: 'id'
      },
      onUpdate: 'CASCADE',
      onDelete: 'SET NULL'
    },
    updatedBy: {
      type: DataTypes.INTEGER,
      allowNull: true,
      references: {
        model: 'Users',
        key: 'id'
      },
      onUpdate: 'CASCADE',
      onDelete: 'SET NULL'
    },
    deletedAt: {
      type: DataTypes.DATE,
      allowNull: true
    },
    createdAt: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW
    },
    updatedAt: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW
    }
  }, {
    paranoid: true, // Enable soft deletes
    defaultScope: {
      where: { deletedAt: null }
    },
    scopes: {
      withDeleted: {
        where: {} // This scope will ignore the defaultScope's where clause
      }
    }
  });

  Permission.associate = (models) => {
    Permission.belongsToMany(models.Role, {
      through: 'RolePermissions',
      foreignKey: 'permissionId',
      otherKey: 'roleId',
      as: 'roles'
    });
    Permission.belongsTo(models.User, { foreignKey: 'createdBy', as: 'creator' });
    Permission.belongsTo(models.User, { foreignKey: 'updatedBy', as: 'updater' });
  };

  return Permission;
};