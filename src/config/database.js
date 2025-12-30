const Sequelize = require('sequelize');
const path = require('path');

// SQLite Setup for Development
const db = new Sequelize({
    dialect: 'sqlite',
    storage: path.join(__dirname, '../../database.sqlite'),
    logging: false
});

module.exports = db;
