# 🎙️ Proof of Concept: Walkie Talkie (Push-to-Talk)

**Date:** 2026-02-08
**Status:** ✅ Tested & Verified
**Technology:** Socket.io + MediaRecorder API (Web Audio Blobs)

## 🎯 Objective
To enable real-time voice communication between guards and supervisors without external hardware (traditional radios) or complex WebRTC infrastructure (STUN/TURN servers).

## 🛠️ Implementation Details

### Architecture (Simple Broadcasting)
Instead of a full peer-to-peer stream (WebRTC), we use a "Voice Note" approach with instant playback:
1.  **Client A** holds the button -> Browser records audio chunks.
2.  **Client A** releases button -> Browser combines chunks into a `Blob`.
3.  **Client A** sends `Blob` via Socket.io (`voice_message` event).
4.  **Server** receives `Blob` and broadcasts it to everyone else (`play_voice` event).
5.  **Client B** receives data, creates a `Blob URL`, and auto-plays it via HTML5 Audio.

### Code Snippets

**Server Side (Socket.io):**
```javascript
socket.on('voice_message', (data) => {
    // Broadcast raw audio blob to room/everyone
    socket.broadcast.emit('play_voice', data);
});
```

**Client Side (Frontend):**
```javascript
// Recording
navigator.mediaDevices.getUserMedia({ audio: true }).then(stream => {
    mediaRecorder = new MediaRecorder(stream);
    mediaRecorder.ondataavailable = e => chunks.push(e.data);
    mediaRecorder.onstop = () => {
        socket.emit('voice_message', new Blob(chunks));
        chunks = [];
    };
});

// Playing
socket.on('play_voice', (arrayBuffer) => {
    const blob = new Blob([arrayBuffer]);
    const audio = new Audio(URL.createObjectURL(blob));
    audio.play();
});
```

## ✅ Test Results
*   **Latency:** Minimal (<1s for short messages).
*   **Bandwidth:** Low (only transmits when talking).
*   **SSL:** Required on production (Browsers block Mic on insecure HTTP).
*   **Storage:** Ephemeral (Audio is not stored on server, just relayed). *Future enhancement: Store logs for incidents.*

## 🚀 Next Steps (Integration)
1.  Add "PTT" button to Mobile App (Android/WebView).
2.  Add "Listen" mode to Web Dashboard.
3.  Integrate with specific Rooms (e.g., `socket.join('site_123')`) so guards only hear their own site.
