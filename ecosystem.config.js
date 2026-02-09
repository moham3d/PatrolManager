module.exports = {
  apps: [{
    name: 'patrolmanager',
    script: './server.js',
    cwd: '/home/ubuntu/PatrolManager',
    instances: 1,
    autorestart: true,
    watch: false,
    max_memory_restart: '500M',
    env: {
      NODE_ENV: 'development',
      PORT: 3000
    },
    env_production: {
      NODE_ENV: 'production',
      PORT: 3000
    },
    error_file: '/home/ubuntu/PatrolManager/logs/pm2-error.log',
    out_file: '/home/ubuntu/PatrolManager/logs/pm2-out.log',
    log_date_format: 'YYYY-MM-DD HH:mm:ss Z',
    merge_logs: true
  }]
};
