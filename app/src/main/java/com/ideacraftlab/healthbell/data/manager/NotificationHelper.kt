package com.ideacraftlab.healthbell.data.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ideacraftlab.healthbell.MainActivity
import com.ideacraftlab.healthbell.ui.reminder.ReminderActivity
import com.ideacraftlab.healthbell.data.receiver.ReminderActionReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "health_bell_reminder"

    fun showReminderNotification(type: String, medName: String = "", medId: String = "", time: String = "") {
        // Professional IDs: Water uses a fixed ID, Medicine uses unique IDs per medicine
        val notificationId = if (type == "water") {
            9999
        } else {
            (medId.hashCode() + time.hashCode()).coerceAtLeast(0) + 10000
        }

        // Production check: Don't fire if permission is revoked
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Health Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Shows medicine and water reminders"
                setSound(android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI, android.app.Notification.AUDIO_ATTRIBUTES_DEFAULT)
                enableVibration(true)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tapping the notification opens the App
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context, notificationId, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Full Screen Intent (Wakes screen when locked)
        val fullScreenIntent = Intent(context, ReminderActivity::class.java).apply {
            putExtra("type", type)
            putExtra("med_name", medName)
            putExtra("med_id", medId)
            putExtra("time", time)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, notificationId + 2, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Quick Action Intent (Mark as Taken)
        val doneIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = "ACTION_DONE"
            putExtra("type", type)
            putExtra("med_id", medId)
            putExtra("time", time)
            putExtra("notification_id", notificationId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 1, doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (type == "water") "Time to Hydrate! 💧" else "Medicine Time! 💊"
        val message = if (type == "water") "Drink some water now." else "Take your $medName now."
        val actionText = if (type == "water") "I Drank It" else "Mark as Taken"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(contentPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_edit, actionText, donePendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .setSound(android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)
            .setVibrate(longArrayOf(0, 500, 1000, 500))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
