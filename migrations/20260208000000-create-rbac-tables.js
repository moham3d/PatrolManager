'use strict';

module.exports = {
  up: async (queryInterface, Sequelize) => {
    // 1. Create Permissions Table
    await queryInterface.createTable('Permissions', {
      id: {
        allowNull: false,
        autoIncrement: true,
        primaryKey: true,
        type: Sequelize.INTEGER
      },
      name: {
        type: Sequelize.STRING,
        allowNull: false
      },
      slug: { // e.g. "SITE_CREATE"
        type: Sequelize.STRING,
        allowNull: false,
        unique: true
      },
      description: {
        type: Sequelize.STRING,
        allowNull: true
      },
      createdAt: {
        allowNull: false,
        type: Sequelize.DATE,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP')
      },
      updatedAt: {
        allowNull: false,
        type: Sequelize.DATE,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP')
      }
    });

    // 2. Create RolePermissions (Junction) Table
    await queryInterface.createTable('RolePermissions', {
      roleId: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        references: {
          model: 'Roles', // Assumes table name is "Roles"
          key: 'id'
        },
        onDelete: 'CASCADE',
        onUpdate: 'CASCADE'
      },
      permissionId: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        references: {
          model: 'Permissions',
          key: 'id'
        },
        onDelete: 'CASCADE',
        onUpdate: 'CASCADE'
      },
      createdAt: {
        allowNull: false,
        type: Sequelize.DATE,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP')
      },
      updatedAt: {
        allowNull: false,
        type: Sequelize.DATE,
        defaultValue: Sequelize.literal('CURRENT_TIMESTAMP')
      }
    });
  },

  down: async (queryInterface, Sequelize) => {
    await queryInterface.dropTable('RolePermissions');
    await queryInterface.dropTable('Permissions');
  }
};
