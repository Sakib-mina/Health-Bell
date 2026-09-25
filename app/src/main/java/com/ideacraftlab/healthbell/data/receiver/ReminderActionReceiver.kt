package com.ideacraftlab.healthbell.data.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ideacraftlab.healthbell.domain.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {

    @Inject lateinit var userRepository: UserRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val type = intent.getStringExtra("type") ?: ""
        val medId = intent.getStringExtra("med_id") ?: ""
        val time = intent.getStringExtra("time") ?: ""
        val notificationId = intent.getIntExtra("notification_id", 1001)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        if (action == "ACTION_DONE") {
            CoroutineScope(Dispatchers.IO).launch {
                if (type == "water") {
                    val currentData = userRepository.getUserData().getOrNull()
                    val newIntake = (currentData?.currentWaterIntake ?: 0) + 250
                    userRepository.updateWaterIntake(newIntake)
                } else {
                    userRepository.toggleMedicineTaken(medId, time, true)
                }
            }
        }
    }
}
