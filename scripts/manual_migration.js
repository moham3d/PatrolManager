const { Sequelize, DataTypes } = require('sequelize');
const db = require('../src/models');
require('dotenv').config();

async function addMissingColumns() {
    const queryInterface = db.sequelize.getQueryInterface();
    const table = 'Users';

    try {
        console.log('Adding columns...');
        await queryInterface.addColumn(table, 'profilePicture', { type: DataTypes.STRING, allowNull: true });
        console.log('Added profilePicture');
    } catch (e) { console.log('profilePicture might already exist'); }

    try {
        await queryInterface.addColumn(table, 'nationalId', { type: DataTypes.STRING, allowNull: true });
        console.log('Added nationalId');
    } catch (e) { console.log('nationalId might already exist'); }

    try {
        await queryInterface.addColumn(table, 'phoneNumber', { type: DataTypes.STRING, allowNull: true });
        console.log('Added phoneNumber');
    } catch (e) { console.log('phoneNumber might already exist'); }

    try {
        await queryInterface.addColumn(table, 'lastLogin', { type: DataTypes.DATE, allowNull: true });
        console.log('Added lastLogin');
    } catch (e) { console.log('lastLogin might already exist'); }

    console.log('Done.');
    await db.sequelize.close();
}

addMissingColumns();
