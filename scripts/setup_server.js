const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const dbPath = path.join(__dirname, '../database.sqlite');

console.log('🚀 Starting Clean Server Setup...');

// 1. Remove existing database
if (fs.existsSync(dbPath)) {
    console.log('🗑 Deleting existing database.sqlite...');
    fs.unlinkSync(dbPath);
} else {
    console.log('ℹ️ No existing database found.');
}

try {
    // 2. Sync Database
    console.log('🔄 Syncing Database Structure...');
    execSync('node scripts/db_manager.js sync', { stdio: 'inherit' });

    // 3. Seed Data
    console.log('🌱 Seeding Initial Data...');
    execSync('node scripts/seeder.js', { stdio: 'inherit' });

    console.log('\n✅ Setup Complete! You can now run: npm start');
} catch (error) {
    console.error('\n❌ Setup Failed:', error.message);
    process.exit(1);
}
