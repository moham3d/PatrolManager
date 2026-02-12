'use strict';

module.exports = {
  up: async (queryInterface, Sequelize) => {
    await queryInterface.addColumn('Sites', 'layoutImage', {
      type: Sequelize.STRING,
      allowNull: true,
      comment: 'URL or path to the floor plan image'
    });
    await queryInterface.addColumn('Checkpoints', 'layoutX', {
      type: Sequelize.FLOAT,
      allowNull: true,
      comment: 'X percentage position (0-100) on the layout image'
    });
    await queryInterface.addColumn('Checkpoints', 'layoutY', {
      type: Sequelize.FLOAT,
      allowNull: true,
      comment: 'Y percentage position (0-100) on the layout image'
    });
  },

  down: async (queryInterface, Sequelize) => {
    await queryInterface.removeColumn('Sites', 'layoutImage');
    await queryInterface.removeColumn('Checkpoints', 'layoutX');
    await queryInterface.removeColumn('Checkpoints', 'layoutY');
  }
};