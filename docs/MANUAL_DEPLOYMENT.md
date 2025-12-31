# Manual Deployment & Database Setup (SQLite)

This guide explains how to set up the PatrolManager application and database on a new Linux server using the default SQLite configuration.

## Prerequisites

- **Node.js**: Ensure Node.js (v18 or higher) is installed.
- **Project Files**: The project code must be present on the server.

## Step 1: Install Dependencies

Navigate to the project root directory and install the required packages:

```bash
npm install
```

## Step 2: Initialize the Database

We use the scripts in the `@scripts` directory to create the database file (`database.sqlite`) and creating the table structure.

**Option A: Fresh Setup (Recommended for new servers)**
This command creates the tables. If `database.sqlite` does not exist, it will be created.

```bash
node scripts/db_manager.js sync
```

**Option B: Full Reset (WARNING: DATA LOSS)**
If you want to wipe an existing database and start over:

```bash
node scripts/db_manager.js reset
```

## Step 3: Seed Initial Data

Populate the database with default roles, permissions, and the initial Admin user.

```bash
node scripts/seeder.js
```

By default, this creates:
- **Admin**: `admin@patrol.eg` / `password123`
- **Manager**: `manager@patrol.eg` / `password123`
- Sites: Cairo Festival City, Smart Village
- Basic Zones and Checkpoints

## Step 4: Verify Setup

Check that the database file was created:

```bash
ls -l database.sqlite
```

You can now start the server:

```bash
npm start
```
