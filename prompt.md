**AI Agent Prompt - Security Company Owner's Dream App Request**

```
You are lead Android developer for PatrolShield, my security company's mobile app. I've seen the backend you built - it's solid but the Android app needs to be extraordinary. This isn't just another security app; this is going to revolutionize how security companies operate in the field.

Here's what I need you to build:

## Core Philosophy
The app must feel like it was built BY guards, FOR guards. It needs to be intuitive, fast, reliable, and work flawlessly even in basements with zero signal. Lives depend on this.

## Guard Experience (The Foundation)

### Shift Management
- One-tap clock-in with geofencing validation (already works, but make it smoother)
- Real-time shift timer that's always visible
- Automatic shift detection when approaching site boundaries
- Shift handoff between guards with photo documentation
- Break management with timer tracking

### Patrol Execution (The Heart)
- **Offline-First Architecture**: The entire day's patrol routes should be cached locally
- **Smart Navigation**: Turn-by-turn directions between checkpoints (using offline maps)
- **Progress Tracking**: Visual progress bar showing % completed, with checkpoints checked off
- **Audio Prompts**: Optional voice guidance for each next checkpoint
- **Time Estimates**: Show expected duration vs actual time, with alerts if falling behind
- **Scan Options**: NFC tap, QR code scan, or GPS proximity (auto-detect available options)
- **Anti-Cheat**: Camera snapshot with timestamp at each checkpoint (guards know it's fair)

### Incident Reporting
- Quick-report buttons for common incidents: Theft, Fire, Medical, Suspicious Activity, Access Control, Maintenance
- Voice-to-text for incident descriptions
- Photo/video capture with automatic geotagging
- One-tap escalation to supervisor
- Incident history with status updates visible to reporting guard

### Emergency Features (Non-Negotiable)
- **SOS Button**: Prominent, red, always accessible (even when phone is locked)
- **Long-press to activate** (prevent accidental triggers)
- **Silent alarm option** (for hostage situations)
- **Location broadcast** to nearest 3 guards via geospatial calculation
- **Auto-video recording** starts automatically for 30 seconds when triggered
- **Two-factor confirmation** to dismiss (prevent false all-clears)

### Real-Time Awareness
- Live map showing:
  - My current location
  - Nearby teammates (distance and direction)
  - My assigned checkpoints
  - Active incidents in my zone
- Battery optimization mode that still sends location every 5 minutes
- Activity-based location updates (more frequent when moving, less when stationary)

## Supervisor Experience (Field Command)

### Live Operations Dashboard
- Bird's eye view of all active patrols with real-time progress
- Guard locations on map with status indicators (on patrol, idle, in emergency)
- Incident feed with filtering by priority/location
- Ability to broadcast messages to all guards at a site
- Performance metrics: checkpoints per hour, incident response time, patrol completion rate

### Tactical Management
- Reassign patrols to available guards
- Create ad-hoc checkpoints during emergencies
- Override patrol routes in real-time
- Monitor guard battery levels and connectivity status
- Approve/reject incident escalations
- Live chat with individual guards or group broadcast

## Technical Requirements (I Mean It)

### Performance
- App launch under 2 seconds
- All core features work within 500ms of tap
- Location updates every 30-60 seconds (configurable based on battery)
- Sync queue handles 1000+ offline operations

### Battery Life
- Smart location polling: 15s when patrolling, 5min when idle
- Background location service under 3% battery/hour
- Battery saver mode that extends shift by 4+ hours
- Low battery warnings at 20%, 10%, 5%

### Reliability
- Crash-free rate >99.9%
- Data never lost - everything queues for sync
- Graceful degradation: app works with 0 signal (everything local)
- Auto-reconnect to WebSocket when connection restored
- Conflict resolution for concurrent edits (last-write-wins with timestamps)

### Security
- Encrypted local storage for all data
- Biometric authentication (fingerprint/face unlock) for critical actions
- Certificate pinning for API calls
- Automatic logout after 30 minutes inactivity
- Secure enclave for stored credentials

## User Experience Details

### Visual Design
- Dark mode as default (guards work at night)
- High contrast for outdoor visibility
- Large touch targets (minimum 48x48dp)
- Color coding: Red (critical), Orange (warning), Green (good), Blue (info)
- Progress indicators everywhere (patrols, shift, incidents)

### Accessibility
- One-handed operation support (critical for guards on patrol)
- Voice commands: "Start patrol," "Report incident," "SOS"
- Haptic feedback for all confirmations
- Screen reader support for visually impaired guards
- Adjustable text size without breaking layout

### Notifications
- Urgent alerts override Do Not Disturb
- Context-aware: Only notify for things that need my action
- Quiet hours (except emergencies)
- Grouped notifications (don't spam with 20 checkpoint scans)

## Advanced Features (The Differentiators)

### AI-Powered Insights
- Predictive routing: Suggest optimal patrol order based on historical data
- Anomaly detection: Alert when guard deviates from expected pattern
- Fatigue detection: Monitor pace and suggest breaks
- Incident prediction: Alert supervisors about "hotspots" before incidents happen

### Integration Capabilities
- Two-way radio integration (connect to existing communication systems)
- Badge scanner integration for facility access
- Third-party alarm system integration
- Video management system integration (pull up nearby cameras)

### Analytics (For Managers)
- Guard performance comparison
- Patrol route efficiency analysis
- Incident hotspots visualization
- Response time heatmaps
- Predictive scheduling optimization

## What I Want From You

Build this as if your life depends on it, because a guard's life might. Test it in the field - walk through a dark parking lot, try it in a basement, try it with 2% battery. Fix every bug, optimize every screen, make every interaction delightful.

The backend API is ready. Use it. But, the mobile experience needs to be so good that guards say "I can't imagine patrolling without this."

Deliver me an app that:
1. Guards love using because it makes their job easier and safer
2. Supervisors can't live without because it gives them complete situational awareness
3. Our clients are impressed by the professionalism and transparency
4. Competitors can't match because we thought of everything

Now go build me the best security app in the industry.
```
