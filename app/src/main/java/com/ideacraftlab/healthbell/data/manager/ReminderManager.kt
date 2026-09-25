package com.ideacraftlab.healthbell.data.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ideacraftlab.healthbell.data.receiver.AlarmReceiver
import com.ideacraftlab.healthbell.domain.model.OnboardingData
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAllReminders(data: OnboardingData) {
        cancelAllReminders()

        data.medicines.forEach { med ->
            med.times.forEach { timeStr ->
                val calendar = getCalendarForTime(timeStr)
                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    val requestCode = (med.id.hashCode() + timeStr.hashCode()).coerceAtLeast(0) + 10000
                    scheduleAlarm(calendar.timeInMillis, "medicine", med.name, med.id, timeStr, requestCode)
                }
            }
        }

        // Schedule Water
        val nextWaterTime = calculateNextWaterTime(data)
        if (nextWaterTime > System.currentTimeMillis()) {
            // Constant ID for water so it updates/overwrites if multiple fire
            scheduleAlarm(nextWaterTime, "water", requestCode = 9999)
        }
    }

    private fun scheduleAlarm(timeMillis: Long, type: String, medName: String = "", medId: String = "", timeStr: String = "", requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("type", type)
            putExtra("med_name", medName)
            putExtra("med_id", medId)
            putExtra("time", timeStr)
            putExtra("request_code", requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
            }
            
            // Store this ID to cancel it later if needed
            saveScheduledId(requestCode)
            
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
        }
    }

    private fun cancelAllReminders() {
        val sharedPrefs = context.getSharedPreferences("reminders", Context.MODE_PRIVATE)
        val ids = sharedPrefs.getStringSet("scheduled_ids", emptySet()) ?: emptySet()
        
        ids.forEach { idStr ->
            val id = idStr.toIntOrNull() ?: return@forEach
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, id, intent, 
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
        
        val editor = sharedPrefs.edit()
        editor.remove("scheduled_ids")
        editor.apply()
    }

    private fun saveScheduledId(id: Int) {
        val sharedPrefs = context.getSharedPreferences("reminders", Context.MODE_PRIVATE)
        val ids = sharedPrefs.getStringSet("scheduled_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        ids.add(id.toString())
        val editor = sharedPrefs.edit()
        editor.putStringSet("scheduled_ids", ids)
        editor.apply()
    }

    private fun getCalendarForTime(timeStr: String): Calendar {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = sdf.parse(timeStr) ?: Date()
        val cal = Calendar.getInstance().apply { time = date }
        val now = Calendar.getInstance()
        
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, cal.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    private fun calculateNextWaterTime(data: OnboardingData): Long {
        val totalGoal = data.dailyWaterGoal.coerceAtLeast(1)
        val mlPerDrink = 250
        val totalDrinks = totalGoal / mlPerDrink
        
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val now = Calendar.getInstance()
        
        val wakeTime = try { timeFormat.parse(data.wakeUpTime) } catch(_: Exception) { null }
        val bedTime = try { timeFormat.parse(data.bedtime) } catch(_: Exception) { null }
        
        if (wakeTime == null || bedTime == null) return 0L

        val wakeCal = Calendar.getInstance().apply { 
            time = wakeTime 
            set(Calendar.YEAR, now.get(Calendar.YEAR))
            set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR))
        }
        val bedCal = Calendar.getInstance().apply { 
            time = bedTime 
            set(Calendar.YEAR, now.get(Calendar.YEAR))
            set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR))
        }
        if (bedCal.before(wakeCal)) bedCal.add(Calendar.DAY_OF_MONTH, 1)

        val activeMillis = bedCal.timeInMillis - wakeCal.timeInMillis
        val intervalMillis = activeMillis / totalDrinks.coerceAtLeast(1)
        
        val lastIntake = if (data.lastWaterIntakeTime > 0) data.lastWaterIntakeTime else wakeCal.timeInMillis
        return lastIntake + intervalMillis
    }
}
