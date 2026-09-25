package com.ideacraftlab.healthbell.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideacraftlab.healthbell.domain.model.OnboardingData
import com.ideacraftlab.healthbell.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userData = MutableStateFlow<OnboardingData?>(null)
    val userData: StateFlow<OnboardingData?> = _userData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isWaterSelected = MutableStateFlow(true)
    val isWaterSelected: StateFlow<Boolean> = _isWaterSelected.asStateFlow()

    init {
        observeUserData()
    }

    private fun observeUserData() {
        _isLoading.value = true
        userRepository.getUserDataFlow()
            .onEach { 
                _userData.value = it 
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    fun setTab(isWater: Boolean) {
        _isWaterSelected.value = isWater
    }

    // Mock data generators for the charts to keep it dynamic
    fun getStreakData(): List<Boolean> {
        val data = mutableListOf<Boolean>()
        // Mocking last 30 days: true if goal met, false if missed
        repeat(30) { data.add(it % 4 != 0) } 
        return data
    }

    fun getWeeklyAdherence(): List<Float> {
        return _userData.value?.weeklyProgress ?: listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.3f)
    }

    fun getMonthlyTrend(): List<Float> {
        return listOf(0.5f, 0.6f, 0.55f, 0.7f, 0.8f, 0.65f, 0.75f, 0.9f, 0.1f, 0.2f, 0.3f, 0.8f, 0.95f, 0.85f, 0.6f)
    }

    fun getOverallSplit(): Pair<Float, Float> {
        val water = _userData.value?.currentWaterIntake?.toFloat() ?: 100f
        val goal = _userData.value?.dailyWaterGoal?.toFloat() ?: 2000f
        val medTaken = _userData.value?.takenMedicinesToday?.size?.toFloat() ?: 1f
        val totalMed = _userData.value?.medicines?.size?.toFloat()?.coerceAtLeast(1f) ?: 1f
        
        return Pair(water / goal, medTaken / totalMed)
    }
}
