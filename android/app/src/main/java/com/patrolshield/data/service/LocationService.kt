package com.patrolshield.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.*
import com.patrolshield.R
import com.patrolshield.common.UserPreferences
import com.patrolshield.data.remote.socket.SocketManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var socketManager: SocketManager

    @Inject
    lateinit var userPreferences: UserPreferences

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    emitLocation(location)
                }
            }
        }
        
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.create().apply {
                interval = 10000 // 10 seconds
                fastestInterval = 5000
                priority = Priority.PRIORITY_HIGH_ACCURACY
            }
            
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun emitLocation(location: Location) {
        // Only emit if authenticated
        val siteId = userPreferences.getSiteId()
        socketManager.emitLocation(location.latitude, location.longitude, siteId)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopForeground(true)
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PatrolShield Active")
            .setContentText("Tracking location for patrol...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Try to use generic or launcher
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "location_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP_SERVICE = "STOP_LOCATION_SERVICE"
    }
}
