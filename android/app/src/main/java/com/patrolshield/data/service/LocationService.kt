package com.patrolshield.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.patrolshield.R
import com.patrolshield.data.local.dao.LogDao
import com.patrolshield.data.local.entities.LogEntity
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var logDao: LogDao
    
    @Inject
    lateinit var patrolRepository: com.patrolshield.domain.repository.PatrolRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: com.google.android.gms.location.FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var activeRunId: Int? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.lastOrNull()?.let { location ->
                    serviceScope.launch {
                        // 1. Send Live Heartbeat
                        patrolRepository.sendHeartbeat(location.latitude, location.longitude, activeRunId)

                        // 2. Log Locally (Existing Logic)
                        val payload = mapOf(
                            "lat" to location.latitude,
                            "lng" to location.longitude,
                            "accuracy" to location.accuracy,
                            "ts" to location.time
                        )
                        logDao.insertLog(
                            LogEntity(
                                type = "GPS_LOG",
                                payload = Gson().toJson(payload),
                                synced = false
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            "ACTION_START" -> start()
            "ACTION_STOP" -> stop()
        }
        return START_STICKY
    }

    private fun start() {
        val channelId = "location_channel"
        val notificationManager = getSystemService(NotificationManager::class.java)
        
        val channel = NotificationChannel(
            channelId,
            "Patrol Tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("PatrolShield Active")
            .setContentText("Tracking patrol location...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
        
        startForeground(1, notification)
        
        // Monitor Active Patrol
        serviceScope.launch {
            patrolRepository.getActivePatrol().collect { patrol ->
                activeRunId = patrol?.remoteId
            }
        }

        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).build()
        try {
            locationClient.requestLocationUpdates(request, locationCallback, null)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun stop() {
        locationClient.removeLocationUpdates(locationCallback)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}
