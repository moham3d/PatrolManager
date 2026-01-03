const { spawn } = require('child_process');
const path = require('path');

const runScript = (scriptName) => {
    return new Promise((resolve, reject) => {
        console.log(`\n🚀 Running: ${scriptName}...`);
        const process = spawn('node', [path.join(__dirname, scriptName)], { stdio: 'inherit' });

        process.on('close', (code) => {
            if (code === 0) {
                resolve();
            } else {
                reject(new Error(`Script ${scriptName} failed with code ${code}`));
            }
        });
    });
};

const init = async () => {
    try {
        // 1. Sync Schema
        await runScript('sync_db.js');
        
        // 2. Add Indexes
        await runScript('add_indexes.js');
        
        // 3. Seed Data
        await runScript('seeder.js');
        
        console.log('\n✅ Database Initialization Complete!');
        process.exit(0);
    } catch (err) {
        console.error(`\n❌ Initialization Failed: ${err.message}`);
        process.exit(1);
    }
};

init();
