# 🤖 PatrolShield AI Agents - Quick Reference

This document provides a quick reference for all AI agents created for the PatrolShield project.

---

## Agent Registry

| Agent | File | Focus | Tasks |
|--------|-------|--------|--------|
| **Database Architect** | `.agent/rules/database-architect.md` | Database schema, migrations, models, indexes |
| **Backend Engineer** | `.agent/rules/backend-engineer.md` | Controllers, APIs, business logic, validation |
| **Security Specialist** | `.agent/rules/security-specialist.md` | Auth, RBAC, rate limiting, security hardening |
| **Frontend Developer** | `.agent/rules/frontend-developer.md` | EJS views, Socket.IO UI, charts, maps |
| **Mobile Engineer** | `.agent/rules/mobile-engineer.md` | Android native, Kotlin, Jetpack Compose, offline-first |
| **DevOps Engineer** | `.agent/rules/devops-engineer.md` | Cron jobs, logging, monitoring, automation |
| **Integration Specialist** | `.agent/rules/integration-specialist.md` | Socket.IO, real-time events, rooms, broadcasts |
| **Product Manager** | `.agent/rules/product-manager.md` | Strategy, design, engineering triad |

---

## Agent Quick Links

### 🏗️ Database Architect
**Purpose**: Build and maintain robust database schema
**Key Files**:
- `/src/models/` - All Sequelize model definitions
- `/src/migrations/` - Database schema migrations
- `/src/models/index.js` - Model associations

**Critical Tasks**:
- Add missing foreign keys to all models
- Create missing models (IncidentEvidence, GPSLog, DeviceRegistration, etc.)
- Implement audit trail fields (createdBy, updatedBy, deletedAt)
- Add database indexes for performance

---

### ⚙️ Backend Engineer
**Purpose**: Implement secure, validated API endpoints
**Key Files**:
- `/src/controllers/` - All controller logic
- `/src/routes/` - API route definitions
- `/src/utils/errors.js` - Custom error classes

**Critical Tasks**:
- Fix Shift status ENUM mismatch
- Add RBAC to all routes
- Create authController.js
- Implement comprehensive validation
- Add transaction support

---

### 🔒 Security Specialist
**Purpose**: Protect system from security vulnerabilities
**Key Files**:
- `/src/middleware/` - Security middleware
- `/src/config/passport.js` - Authentication configuration

**Critical Tasks**:
- Implement rate limiting
- Fix CORS configuration
- Add CSRF protection
- Secure session cookies
- Add Helmet security headers
- Implement account lockout

---

### 🎨 Frontend Developer
**Purpose**: Build responsive, real-time user interfaces
**Key Files**:
- `/src/views/` - All EJS templates
- `/src/views/partials/` - Reusable components
- `/src/public/js/` - Client-side JavaScript

**Critical Tasks**:
- Add active patrol progress visualization
- Implement geofence polygon editor
- Add incident markers on live map
- Add real-time guard status indicators
- Implement map marker clustering

---

### 📱 Mobile Engineer
**Purpose**: Build offline-first Android native app
**Key Files**:
- `/android/app/src/main/java/com/patrolshield/` - All Kotlin code
- `/android/app/build.gradle` - Dependencies

**Critical Tasks**:
- Implement QR scanner (CameraX + ML Kit)
- Implement NFC scanner
- Add incident photo upload
- Implement supervisor resolution UI
- Implement sync priority system
- Add EncryptedSharedPreferences

---

### 🚀 DevOps Engineer
**Purpose**: Ensure reliable 24/7 operation with monitoring
**Key Files**:
- `/src/cron/` - All scheduled jobs
- `/src/config/logger.js` - Winston logging

**Critical Tasks**:
- Implement shift reminders
- Implement incomplete patrol monitoring
- Implement incident follow-up reminders
- Implement automated reports
- Fix attendance monitor issues
- Upgrade to Winston logging

---

### 🔌 Integration Specialist
**Purpose**: Build robust real-time communication layer
**Key Files**:
- `/src/sockets/socketHandler.js` - Main Socket.IO implementation
- `/src/sockets/middleware.js` - Socket authentication

**Critical Tasks**:
- Add Socket.IO authentication
- Implement 19+ missing socket events
- Implement site-based room management
- Add event validation
- Implement error acknowledgments
- Optimize broadcasts

---

## Agent Collaboration Flow

```
Database Architect
       ↓ creates models/migrations
       ↓
Backend Engineer
       ↓ implements controllers/APIs
       ↓
Security Specialist
       ↓ protects routes/data
       ↓
Frontend Developer
       ↓ creates EJS views
       ↓
Integration Specialist
       ↓ adds Socket.IO real-time
       ↓
Mobile Engineer
       ↓ implements Android app
       ↓
DevOps Engineer
       ↓ monitors & automates
```

---

## Task Assignment by Phase

### Phase 1-2: Critical Security & Foundation
- **Security Specialist**: Tasks 1.1-1.6, 3.1-3.3
- **Database Architect**: Tasks 2.1-2.3
- **Backend Engineer**: Tasks 1.7-1.8

### Phase 2: Core Functionality
- **Database Architect**: Tasks 5.1-5.6
- **Backend Engineer**: Tasks 4.1-4.4, 6.1-6.3

### Phase 3: Real-Time & Communication
- **Integration Specialist**: Tasks 7.1-7.5
- **Frontend Developer**: Tasks 8.1-8.5

### Phase 4: Android Critical Features
- **Mobile Engineer**: Tasks 9.1-9.3, 10.1-10.4, 11.1-11.3, 12.1-12.3

### Phase 5: Web UI Improvements
- **Frontend Developer**: Tasks 13.1-13.6, 14.1-14.3

### Phase 6: Automation & Monitoring
- **DevOps Engineer**: Tasks 15.1-15.6, 16.1-16.4

### Phase 7-8: Polish & Advanced Features
- **Frontend Developer**: Tasks 17.1-17.4
- **Mobile Engineer**: Tasks 12.4-12.5, 18.1-18.3
- **All Agents**: Tasks 19.1-19.3, 20.1-20.4

---

## How to Use These Agents

### For AI (or Human Task Assignment)

1. **Read EXECUTION_PLAN.md** to see all 79 tasks
2. **Identify which agent** owns a specific task
3. **Load the agent's rule file** to understand their persona and constraints
4. **Follow their Golden Rules** when implementing
5. **Use their verification commands** to test work

### Example Workflow

**Task**: "Add rate limiting to auth endpoints"

1. Check EXECUTION_PLAN.md: Task 1.1, Security Specialist
2. Load `.agent/rules/security-specialist.md`
3. Follow the pattern in "Golden Rules: Rule #1"
4. Create `src/middleware/rateLimiter.js`
5. Use verification commands to test
6. Update EXECUTION_PLAN.md: Task 1.1 status → ✅ Complete

---

## Agent Contacts & Handoffs

### Handoff Scenarios

**Database Architect → Backend Engineer**:
- When models/migrations are complete
- Database architect creates FK fields, backend engineer uses them
- Database architect creates new model, backend engineer implements controller

**Backend Engineer → Security Specialist**:
- When new API endpoints are created
- Backend engineer creates route, security specialist protects it
- Backend engineer adds sensitive data, security specialist ensures validation

**Frontend Developer → Integration Specialist**:
- When new UI needs real-time updates
- Frontend developer creates view, integration specialist adds socket events
- Frontend developer adds map markers, integration specialist broadcasts them

**Mobile Engineer → Backend Engineer**:
- When mobile app needs new endpoint
- Mobile engineer defines API contract, backend engineer implements it
- Mobile engineer tests with curl/backend engineer fixes bugs

**DevOps Engineer → All Agents**:
- When monitoring/alerts are needed
- DevOps engineer sets up logging, all agents follow logging format
- DevOps engineer creates health check, all agents ensure their components are monitored

---

## Quick Reference: File Locations

### Backend (Node.js)
```
/src/
├── models/          → Database Architect
├── controllers/       → Backend Engineer
├── routes/          → Backend Engineer
├── middleware/       → Security Specialist
├── views/           → Frontend Developer
├── sockets/         → Integration Specialist
├── cron/            → DevOps Engineer
└── config/          → All (shared)
```

### Android (Kotlin)
```
/android/app/src/main/java/com/patrolshield/
├── data/            → Mobile Engineer
├── domain/           → Mobile Engineer
├── presentation/     → Mobile Engineer
├── di/              → Mobile Engineer
└── MainActivity.kt   → Mobile Engineer
```

---

## Agent Status Tracking

| Agent | Tasks Assigned | Tasks Complete | Completion % |
|--------|---------------|----------------|---------------|
| Database Architect | 8 | 0 | 0% |
| Backend Engineer | 12 | 0 | 0% |
| Security Specialist | 9 | 0 | 0% |
| Frontend Developer | 20 | 0 | 0% |
| Mobile Engineer | 15 | 0 | 0% |
| DevOps Engineer | 10 | 0 | 0% |
| Integration Specialist | 5 | 0 | 0% |
| **TOTAL** | **79** | **0** | **0%** |

---

**Last Updated**: 2026-01-03  
**Version**: 1.0  
**Related**: `/docs/EXECUTION_PLAN.md`
