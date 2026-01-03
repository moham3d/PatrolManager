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

import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity

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
    private var isGpsRunning = false

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

                        // 2. Log Locally
                        val payload = mapOf(
                            "lat" to location.latitude,
                            "lng" to location.longitude,
                            "accuracy" to location.accuracy,
                            "ts" to location.time
                        )
                        logDao.insertLog(
                            LogEntity(
                                action = "GPS_LOG",
                                payload = Gson().toJson(payload),
                                priority = 4,
                                synced = false,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        }
        
        setupActivityRecognition()
    }

    private fun setupActivityRecognition() {
        val transitions = mutableListOf<ActivityTransition>()
        
        transitions.add(ActivityTransition.Builder()
            .setActivityType(DetectedActivity.STILL)
            .setActivityTransitionType(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build())
            
        transitions.add(ActivityTransition.Builder()
            .setActivityType(DetectedActivity.WALKING)
            .setActivityTransitionType(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build())

        transitions.add(ActivityTransition.Builder()
            .setActivityType(DetectedActivity.RUNNING)
            .setActivityTransitionType(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build())

        val request = ActivityTransitionRequest(transitions)
        val intent = Intent(this, ActivityTransitionReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            this, 0, intent, android.app.PendingIntent.FLAG_MUTABLE
        )

        try {
            ActivityRecognition.getClient(this)
                .requestActivityTransitionUpdates(request, pendingIntent)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            "ACTION_START" -> start()
            "ACTION_STOP" -> stop()
            "ACTIVITY_STILL" -> pauseGps()
            "ACTIVITY_MOVING" -> resumeGps()
        }
        return START_STICKY
    }

    private fun pauseGps() {
        if (isGpsRunning) {
            locationClient.removeLocationUpdates(locationCallback)
            isGpsRunning = false
            consoleLog("GPS Paused (Device Still)")
        }
    }

    private fun resumeGps() {
        if (!isGpsRunning) {
            startLocationUpdates()
            consoleLog("GPS Resumed (Device Moving)")
        }
    }

    private fun consoleLog(msg: String) {
        android.util.Log.d("LocationService", msg)
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
        if (isGpsRunning) return
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).build()
        try {
            locationClient.requestLocationUpdates(request, locationCallback, null)
            isGpsRunning = true
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun stop() {
        locationClient.removeLocationUpdates(locationCallback)
        isGpsRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}
