const { Sequelize, DataTypes } = require('sequelize');
const path = require('path');
require('dotenv').config();

const sequelize = new Sequelize({
    dialect: 'sqlite',
    storage: process.env.DB_STORAGE || 'database.sqlite',
    logging: console.log
});

async function addColumn() {
    try {
        const queryInterface = sequelize.getQueryInterface();
        await queryInterface.addColumn('PatrolRuns', 'siteId', {
            type: DataTypes.INTEGER,
            allowNull: true,
            references: {
                model: 'Sites',
                key: 'id'
            },
            onUpdate: 'CASCADE',
            onDelete: 'SET NULL'
        });
        console.log('✅ Column siteId added to PatrolRuns successfully.');
    } catch (error) {
        console.error('❌ Error adding column:', error.message);
    } finally {
        await sequelize.close();
    }
}

addColumn();
