'use strict';

module.exports = {
  up: async (queryInterface, Sequelize) => {
    // PatrolRuns
    await queryInterface.addIndex('PatrolRuns', ['siteId']);
    await queryInterface.addIndex('PatrolRuns', ['guardId']);
    await queryInterface.addIndex('PatrolRuns', ['status']);
    await queryInterface.addIndex('PatrolRuns', ['startTime']);

    // Incidents
    await queryInterface.addIndex('Incidents', ['siteId']);
    await queryInterface.addIndex('Incidents', ['status']);
    await queryInterface.addIndex('Incidents', ['priority']);

    // Shifts
    await queryInterface.addIndex('Shifts', ['userId']);
    await queryInterface.addIndex('Shifts', ['siteId']);
    await queryInterface.addIndex('Shifts', ['status']);

    // Checkpoints
    await queryInterface.addIndex('Checkpoints', ['siteId']);
    await queryInterface.addIndex('Checkpoints', ['uid']);

    // AuditLogs
    await queryInterface.addIndex('AuditLogs', ['userId']);
    await queryInterface.addIndex('AuditLogs', ['entity', 'entityId']);
    await queryInterface.addIndex('AuditLogs', ['timestamp']);

    // GPSLogs
    await queryInterface.addIndex('GPSLogs', ['userId']);
    await queryInterface.addIndex('GPSLogs', ['patrolRunId']);
    await queryInterface.addIndex('GPSLogs', ['timestamp']);
  },

  down: async (queryInterface, Sequelize) => {
    await queryInterface.removeIndex('PatrolRuns', ['siteId']);
    await queryInterface.removeIndex('PatrolRuns', ['guardId']);
    await queryInterface.removeIndex('PatrolRuns', ['status']);
    await queryInterface.removeIndex('PatrolRuns', ['startTime']);
    await queryInterface.removeIndex('Incidents', ['siteId']);
    await queryInterface.removeIndex('Incidents', ['status']);
    await queryInterface.removeIndex('Incidents', ['priority']);
    await queryInterface.removeIndex('Shifts', ['userId']);
    await queryInterface.removeIndex('Shifts', ['siteId']);
    await queryInterface.removeIndex('Shifts', ['status']);
    await queryInterface.removeIndex('Checkpoints', ['siteId']);
    await queryInterface.removeIndex('Checkpoints', ['uid']);
    await queryInterface.removeIndex('AuditLogs', ['userId']);
    await queryInterface.removeIndex('AuditLogs', ['entity', 'entityId']);
    await queryInterface.removeIndex('AuditLogs', ['timestamp']);
    await queryInterface.removeIndex('GPSLogs', ['userId']);
    await queryInterface.removeIndex('GPSLogs', ['patrolRunId']);
    await queryInterface.removeIndex('GPSLogs', ['timestamp']);
  }
};