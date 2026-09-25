package com.ideacraftlab.healthbell.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideacraftlab.healthbell.domain.model.OnboardingData
import com.ideacraftlab.healthbell.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: com.ideacraftlab.healthbell.domain.repository.UserRepository,
    private val reminderManager: com.ideacraftlab.healthbell.data.manager.ReminderManager
) : ViewModel() {

    private val _userData = MutableStateFlow<OnboardingData?>(null)
    val userData: StateFlow<OnboardingData?> = _userData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _nextWaterTime = MutableStateFlow("")
    val nextWaterTime: StateFlow<String> = _nextWaterTime.asStateFlow()

    private val _waterStats = MutableStateFlow(WaterStats())
    val waterStats: StateFlow<WaterStats> = _waterStats.asStateFlow()

    private val _showReminderOverlay = MutableStateFlow<ReminderType?>(null)
    val showReminderOverlay: StateFlow<ReminderType?> = _showReminderOverlay.asStateFlow()

    sealed class ReminderType {
        data class Water(val amount: Int) : ReminderType()
        data class Medicine(val medId: String, val name: String, val time: String) : ReminderType()
    }

    data class WaterStats(
        val frequencyMinutes: Int = 0,
        val mlPerDrink: Int = 250
    )

    init {
        observeUserData()
        startReminderTicker()
    }

    private fun observeUserData() {
        _isLoading.value = true
        userRepository.getUserDataFlow()
            .onEach { data ->
                _userData.value = data
                calculateNextWaterTime(data)
                calculateWaterStats(data)
                if (data != null) {
                    reminderManager.scheduleAllReminders(data)
                }
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    private fun startReminderTicker() {
        viewModelScope.launch {
            while (true) {
                checkReminders()
                delay(60000) // Check every minute
            }
        }
    }

    private fun checkReminders() {
        val data = _userData.value ?: return
        val now = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val nowStr = timeFormat.format(now.time)

        // Medicine Reminder
        data.medicines.forEach { med ->
            med.times.forEach { time ->
                if (time == nowStr && !data.takenMedicinesToday.contains("${med.id}_$time")) {
                    _showReminderOverlay.value = ReminderType.Medicine(med.id, med.name, time)
                }
            }
        }

        // Water Reminder (if next time is reached)
        if (_nextWaterTime.value != "" && _nextWaterTime.value == nowStr) {
             _showReminderOverlay.value = ReminderType.Water(_waterStats.value.mlPerDrink)
        }
    }

    fun dismissReminder() {
        _showReminderOverlay.value = null
    }

    fun skipMedicine(id: String, time: String) {
        // Mark as "overdue" or just dismissed for today
        dismissReminder()
    }

    private fun calculateWaterStats(data: OnboardingData?) {
        if (data == null) return
        val totalGoal = data.dailyWaterGoal.coerceAtLeast(1)
        val mlPerDrink = 250
        val totalDrinks = totalGoal / mlPerDrink
        
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val wakeTime = try { timeFormat.parse(data.wakeUpTime) } catch(_: Exception) { null }
        val bedTime = try { timeFormat.parse(data.bedtime) } catch(_: Exception) { null }
        
        val activeHours = if (wakeTime != null && bedTime != null) {
            var diff = (bedTime.time - wakeTime.time) / (1000 * 60 * 60)
            if (diff < 0) diff += 24
            diff.coerceAtLeast(1)
        } else 16L
        
        val freq = (activeHours * 60 / totalDrinks.coerceAtLeast(1)).toInt()
        _waterStats.value = WaterStats(frequencyMinutes = freq, mlPerDrink = mlPerDrink)
    }

    fun addWater(ml: Int) {
        val currentData = _userData.value ?: return
        val newIntake = currentData.currentWaterIntake + ml
        viewModelScope.launch {
            userRepository.updateWaterIntake(newIntake)
            dismissReminder()
        }
    }

    fun toggleMedicine(id: String, time: String, taken: Boolean) {
        viewModelScope.launch {
            userRepository.toggleMedicineTaken(id, time, taken)
            if (taken) dismissReminder()
        }
    }

    private fun calculateNextWaterTime(data: OnboardingData?) {
        if (data == null) return
        
        val totalGoal = data.dailyWaterGoal.coerceAtLeast(1)
        val mlPerDrink = 250
        val totalDrinks = totalGoal / mlPerDrink
        
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMin = now.get(Calendar.MINUTE)
        val nowInMins = currentHour * 60 + currentMin

        val wakeTime = try { timeFormat.parse(data.wakeUpTime) } catch(_: Exception) { null }
        val bedTime = try { timeFormat.parse(data.bedtime) } catch(_: Exception) { null }
        
        if (wakeTime == null || bedTime == null) {
            _nextWaterTime.value = "08:00 AM"
            return
        }

        val wakeCal = Calendar.getInstance().apply { time = wakeTime }
        val bedCal = Calendar.getInstance().apply { time = bedTime }
        
        val wakeMins = wakeCal.get(Calendar.HOUR_OF_DAY) * 60 + wakeCal.get(Calendar.MINUTE)
        var bedMins = bedCal.get(Calendar.HOUR_OF_DAY) * 60 + bedCal.get(Calendar.MINUTE)
        
        if (bedMins <= wakeMins) bedMins += 1440 

        val isSleeping = if (bedMins > 1440) {
            val adjustedBedMins = bedMins - 1440
            nowInMins in adjustedBedMins until wakeMins
        } else {
            nowInMins >= bedMins || nowInMins < wakeMins
        }

        if (isSleeping) {
            _nextWaterTime.value = "After Wake up"
            return
        }

        val activeMins = bedMins - wakeMins
        val intervalMins = activeMins / totalDrinks.coerceAtLeast(1)
        
        val lastIntake = if (data.lastWaterIntakeTime > 0) {
            val lastCal = Calendar.getInstance().apply { timeInMillis = data.lastWaterIntakeTime }
            if (lastCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                lastCal.get(Calendar.HOUR_OF_DAY) * 60 + lastCal.get(Calendar.MINUTE)
            } else wakeMins
        } else wakeMins

        var nextMins = lastIntake + intervalMins
        if (nextMins <= nowInMins) nextMins = nowInMins + intervalMins
        
        if (nextMins >= bedMins) {
            _nextWaterTime.value = "Tomorrow"
        } else {
            val nextCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, (nextMins.toInt() / 60) % 24)
                set(Calendar.MINUTE, nextMins.toInt() % 60)
            }
            _nextWaterTime.value = timeFormat.format(nextCal.time)
        }
    }
}
