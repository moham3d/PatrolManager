'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up (queryInterface, Sequelize) {
    await queryInterface.addColumn('Permissions', 'category', {
      type: Sequelize.STRING,
      allowNull: true,
      after: 'description'
    });
    await queryInterface.addColumn('Permissions', 'createdBy', {
      type: Sequelize.INTEGER,
      allowNull: true,
      references: {
        model: 'Users',
        key: 'id'
      },
      onUpdate: 'CASCADE',
      onDelete: 'SET NULL',
      after: 'category'
    });
    await queryInterface.addColumn('Permissions', 'updatedBy', {
      type: Sequelize.INTEGER,
      allowNull: true,
      references: {
        model: 'Users',
        key: 'id'
      },
      onUpdate: 'CASCADE',
      onDelete: 'SET NULL',
      after: 'createdBy'
    });
    await queryInterface.addColumn('Permissions', 'deletedAt', {
      type: Sequelize.DATE,
      allowNull: true,
      after: 'updatedBy'
    });
  },

  async down (queryInterface, Sequelize) {
    await queryInterface.removeColumn('Permissions', 'deletedAt');
    await queryInterface.removeColumn('Permissions', 'updatedBy');
    await queryInterface.removeColumn('Permissions', 'createdBy');
    await queryInterface.removeColumn('Permissions', 'category');
  }
};
