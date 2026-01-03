package com.patrolshield.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityTransitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            result?.let {
                for (event in it.transitionEvents) {
                    val serviceIntent = Intent(context, LocationService::class.java)
                    if (event.activityType == DetectedActivity.STILL) {
                        serviceIntent.action = "ACTIVITY_STILL"
                    } else if (event.activityType == DetectedActivity.WALKING || 
                               event.activityType == DetectedActivity.RUNNING) {
                        serviceIntent.action = "ACTIVITY_MOVING"
                    }
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
