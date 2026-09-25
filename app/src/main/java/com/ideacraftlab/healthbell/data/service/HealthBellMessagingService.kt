package com.ideacraftlab.healthbell.data.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ideacraftlab.healthbell.data.manager.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HealthBellMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            val type = remoteMessage.data["type"] ?: "water"
            val medName = remoteMessage.data["med_name"] ?: ""
            val medId = remoteMessage.data["med_id"] ?: ""
            val time = remoteMessage.data["time"] ?: ""
            
            notificationHelper.showReminderNotification(type, medName, medId, time)
        }

        // Check if message contains a notification payload (fallback).
        remoteMessage.notification?.let {
            notificationHelper.showReminderNotification("water")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        // Normally we'd send this to the server
    }
}
