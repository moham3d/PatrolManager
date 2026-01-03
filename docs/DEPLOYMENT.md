# 🚀 PatrolShield Deployment Guide

## Prerequisites
- Node.js v20+
- PostgreSQL (Production) or SQLite (Development)
- SMTP Server (for automated reports)

## Installation
1. Clone the repository
2. Install dependencies: `npm install`
3. Configure environment variables (see `.env.example`)
4. Run migrations: `npx sequelize-cli db:migrate` OR `npm run db:init`
5. Start the server: `npm start` OR `npm rn dev`

## Environment Variables
- `PORT`: Server port (default 3000)
- `DATABASE_URL`: Connection string for PostgreSQL
- `JWT_SECRET`: Secret for mobile authentication
- `SESSION_SECRET`: Secret for web sessions
- `MAIL_HOST`: SMTP host
- `MAIL_USER`: SMTP username
- `MAIL_PASS`: SMTP password

## Automation (Cron Jobs)
Automated tasks are handled by `node-cron` within the application. Ensure the server has a persistent process manager like PM2 to keep it running.

```bash
pm2 start server.js --name patrolshield
```

## Maintenance
- **Logs**: Located in `logs/` directory
- **Backups**: Implement daily PostgreSQL backups using `pg_dump`
- **Cleanup**: The application automatically cleans up old evidence files (>90 days) via internal cron jobs.
