package com.ideacraftlab.healthbell.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.ideacraftlab.healthbell.data.manager.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "HealthBell:AlarmWakeLock"
        )
        wakeLock.acquire(10 * 1000L /*10 seconds*/)

        val type = intent.getStringExtra("type") ?: "water"
        val medName = intent.getStringExtra("med_name") ?: ""
        val medId = intent.getStringExtra("med_id") ?: ""
        val time = intent.getStringExtra("time") ?: ""

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showReminderNotification(type, medName, medId, time)
    }
}
