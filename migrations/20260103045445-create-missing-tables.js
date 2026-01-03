'use strict';

module.exports = {
  up: async (queryInterface, Sequelize) => {
    await queryInterface.createTable('IncidentEvidences', {
      id: { type: Sequelize.INTEGER, primaryKey: true, autoIncrement: true },
      incidentId: { type: Sequelize.INTEGER, references: { model: 'Incidents', key: 'id' }, onDelete: 'CASCADE' },
      type: { type: Sequelize.ENUM('image', 'video', 'audio', 'document'), defaultValue: 'image' },
      filePath: { type: Sequelize.STRING, allowNull: false },
      createdAt: { type: Sequelize.DATE, allowNull: false },
      updatedAt: { type: Sequelize.DATE, allowNull: false },
      deletedAt: { type: Sequelize.DATE }
    });

    await queryInterface.createTable('GPSLogs', {
      id: { type: Sequelize.INTEGER, primaryKey: true, autoIncrement: true },
      userId: { type: Sequelize.INTEGER, references: { model: 'Users', key: 'id' } },
      patrolRunId: { type: Sequelize.INTEGER, references: { model: 'PatrolRuns', key: 'id' } },
      lat: { type: Sequelize.FLOAT, allowNull: false },
      lng: { type: Sequelize.FLOAT, allowNull: false },
      accuracy: { type: Sequelize.FLOAT },
      timestamp: { type: Sequelize.DATE, defaultValue: Sequelize.NOW },
      createdAt: { type: Sequelize.DATE, allowNull: false },
      updatedAt: { type: Sequelize.DATE, allowNull: false }
    });

    await queryInterface.createTable('AuditLogs', {
      id: { type: Sequelize.INTEGER, primaryKey: true, autoIncrement: true },
      userId: { type: Sequelize.INTEGER, references: { model: 'Users', key: 'id' } },
      action: { type: Sequelize.STRING, allowNull: false },
      entity: { type: Sequelize.STRING },
      entityId: { type: Sequelize.INTEGER },
      details: { type: Sequelize.JSON },
      timestamp: { type: Sequelize.DATE, defaultValue: Sequelize.NOW },
      createdAt: { type: Sequelize.DATE, allowNull: false },
      updatedAt: { type: Sequelize.DATE, allowNull: false }
    });

    await queryInterface.createTable('Notifications', {
      id: { type: Sequelize.INTEGER, primaryKey: true, autoIncrement: true },
      userId: { type: Sequelize.INTEGER, references: { model: 'Users', key: 'id' } },
      type: { type: Sequelize.STRING, allowNull: false },
      title: { type: Sequelize.STRING, allowNull: false },
      message: { type: Sequelize.TEXT, allowNull: false },
      isRead: { type: Sequelize.BOOLEAN, defaultValue: false },
      data: { type: Sequelize.JSON },
      createdAt: { type: Sequelize.DATE, allowNull: false },
      updatedAt: { type: Sequelize.DATE, allowNull: false }
    });

    await queryInterface.createTable('Alerts', {
      id: { type: Sequelize.INTEGER, primaryKey: true, autoIncrement: true },
      type: { type: Sequelize.STRING, allowNull: false },
      status: { type: Sequelize.STRING, defaultValue: 'new' },
      message: { type: Sequelize.TEXT, allowNull: false },
      siteId: { type: Sequelize.INTEGER, references: { model: 'Sites', key: 'id' } },
      userId: { type: Sequelize.INTEGER, references: { model: 'Users', key: 'id' } },
      metadata: { type: Sequelize.JSON },
      createdAt: { type: Sequelize.DATE, allowNull: false },
      updatedAt: { type: Sequelize.DATE, allowNull: false }
    });

    await queryInterface.createTable('ReportSchedules', {
      id: { type: Sequelize.INTEGER, primaryKey: true, autoIncrement: true },
      userId: { type: Sequelize.INTEGER, references: { model: 'Users', key: 'id' } },
      reportType: { type: Sequelize.STRING, allowNull: false },
      frequency: { type: Sequelize.STRING, allowNull: false },
      email: { type: Sequelize.STRING, allowNull: false },
      lastRun: { type: Sequelize.DATE },
      isActive: { type: Sequelize.BOOLEAN, defaultValue: true },
      createdAt: { type: Sequelize.DATE, allowNull: false },
      updatedAt: { type: Sequelize.DATE, allowNull: false }
    });
  },

  down: async (queryInterface, Sequelize) => {
    await queryInterface.dropTable('ReportSchedules');
    await queryInterface.dropTable('Alerts');
    await queryInterface.dropTable('Notifications');
    await queryInterface.dropTable('AuditLogs');
    await queryInterface.dropTable('GPSLogs');
    await queryInterface.dropTable('IncidentEvidences');
  }
};