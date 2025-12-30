# Walkthrough - Phase 6: Analytics & Reporting

## 🚀 Overview
We have successfully implemented **Module E: Analytics & Reporting**. This module allows Site Managers to generate daily summaries of site activity and view key performance indicators.

## 🛠️ Key Components Implemented

### 1. Daily Activity Reports (DAR)
- **PDF-Ready View**: A print-optimized HTML layout (`/reports/print`) that browsers can natively save as PDF.
- **Data Aggregation**: The `reportGenerator` library aggregates:
    -   Completed Patrols
    -   Reported Incidents
    -   Guard Attendance (Clock-ins)

### 2. Dashboard KPIs
- **Live Stats**: The Reports Dashboard (`/reports`) fetches real-time stats via API:
    -   Guards currently online
    -   Incidents reported today
    -   Active patrols

## 📸 Usage
1.  Navigate to **Analytics & Reporting** in the main menu.
2.  Select a **Site** and a **Date**.
3.  Click **Generate Report**.
4.  A new tab opens with the formatted report. Use `Ctrl+P` (Cmd+P) to save as PDF.

## ✅ Verification
1.  **Web**: Visit `http://localhost:3000/reports`.
2.  **Generate**: Try generating a report for "Today".
3.  **API**: Check `GET /reports/stats` returns JSON.
