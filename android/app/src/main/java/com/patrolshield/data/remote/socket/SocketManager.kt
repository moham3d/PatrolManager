package com.patrolshield.data.remote.socket

import android.util.Log
import com.patrolshield.common.UserPreferences
import io.socket.client.IO
import io.socket.client.Socket
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

@Singleton
class SocketManager @Inject constructor(
    private val userPrefs: UserPreferences
) {
    private var socket: Socket? = null
    private val TAG = "SocketManager"

    fun connect() {
        val token = userPrefs.getAuthToken()
        if (token == null) {
            Log.e(TAG, "Cannot connect: No auth token")
            return
        }

        try {
            val opts = IO.Options()
            opts.auth = mapOf("token" to token)
            opts.reconnection = true
            opts.forceNew = true

            // Using dev IP
            socket = IO.socket("http://192.168.1.41:3000", opts)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Connected to Socket.IO")
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "Connection error: ${args[0]}")
            }

            socket?.on("panic_alert") { args ->
                val data = args[0] as JSONObject
                Log.w(TAG, "PANIC ALERT RECEIVED: $data")
                // TODO: Broadcast to UI / Notification
            }
            
            // Listen for other events
            val events = listOf(
                "incident_created", "incident_assigned", "incident_resolved",
                "patrol_started", "patrol_completed", "checkpoint_scanned",
                "shift_started", "shift_ended"
            )
            
            events.forEach { event ->
                socket?.on(event) { args ->
                    val data = args[0] as JSONObject
                    Log.d(TAG, "Event $event received: $data")
                }
            }

            socket?.connect()

        } catch (e: Exception) {
            Log.e(TAG, "Socket init error", e)
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }

    fun emitLocation(lat: Double, lng: Double, siteId: Int?) {
        val json = JSONObject()
        json.put("lat", lat)
        json.put("lng", lng)
        if (siteId != null) json.put("siteId", siteId)
        
        socket?.emit("update_location", json)
    }
}
