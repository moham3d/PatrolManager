# Gemini Skills for PatrolManager

This project uses a set of specialized "skills" (agent personas/rules) to guide development. These skills are defined in the `.agent/rules/` directory.

To use these skills with the Gemini CLI, you can manually load the context or use them as a reference for your prompts.

## Available Skills

| Skill | Role | Usage |
|-------|------|-------|
| **Database Architect** | Schema & Migrations | Use when designing models or writing SQL/migrations. |
| **Backend Engineer** | API & Logic | Use when writing Express controllers, routes, or business logic. |
| **Security Specialist** | Security Hardening | Use for auth, RBAC, encryption, and security reviews. |
| **Frontend Developer** | UI/UX & Views | Use when writing EJS templates, CSS, or client-side JS. |
| **Mobile Engineer** | Android App | Use when working on the Kotlin/Jetpack Compose mobile app. |
| **DevOps Engineer** | Ops & Monitoring | Use for cron jobs, logging, and deployment scripts. |
| **QA Engineer** | Testing | Use for writing Jest tests and verifying security. |
| **Integration Specialist** | Real-time | Use for Socket.IO and event-driven features. |
| **Product Manager** | Planning | Use for defining requirements and task planning. |

## How to "Activate" a Skill

When interacting with the Gemini CLI, you can instruct it to adopt a specific persona by referencing the rule file.

**Example Prompt:**
> "Acting as the **Mobile Engineer** (reference `.agent/rules/mobile-engineer.md`), please help me fix the navigation bug in MainActivity.kt."

**Example Prompt:**
> "Acting as the **Security Specialist** (reference `.agent/rules/security-specialist.md`), review `src/routes/auth.js` for vulnerabilities."

## Skill Content

The actual instructions for each skill are located in:
- `.agent/rules/database-architect.md`
- `.agent/rules/backend-engineer.md`
- `.agent/rules/security-specialist.md`
- `.agent/rules/frontend-developer.md`
- `.agent/rules/mobile-engineer.md`
- `.agent/rules/devops-engineer.md`
- `.agent/rules/qa-engineer.md`
- `.agent/rules/integration-specialist.md`
- `.agent/rules/product-manager.md`

## Creating New Skills

To create a new skill:
1.  Create a new markdown file in `.agent/rules/` (e.g., `ai-researcher.md`).
2.  Follow the template:
    - **Role Definition**: Who are you?
    - **Project Context**: What are you working on?
    - **Key Responsibilities**: What do you do?
    - **Golden Rules**: What must you follow?
3.  Add the new skill to `.agent/rules/agents.md` and this file.
