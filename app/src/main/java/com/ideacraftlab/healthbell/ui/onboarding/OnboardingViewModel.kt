package com.ideacraftlab.healthbell.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideacraftlab.healthbell.domain.model.MedicineData
import com.ideacraftlab.healthbell.domain.model.OnboardingData
import com.ideacraftlab.healthbell.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: com.ideacraftlab.healthbell.domain.repository.UserRepository,
    private val reminderManager: com.ideacraftlab.healthbell.data.manager.ReminderManager
) : ViewModel() {

    private val _onboardingData = MutableStateFlow(OnboardingData(goal = "both"))
    val onboardingData: StateFlow<OnboardingData> = _onboardingData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun isUserLoggedIn(): Boolean = userRepository.isUserLoggedIn()

    fun updateBodyMetrics(height: String, weight: String, activityLevel: String) {
        val w = weight.toFloatOrNull() ?: 70f
        val baseWater = w * 35
        val multiplier = when(activityLevel) {
            "HIGH" -> 1.3f
            "MODERATE" -> 1.15f
            else -> 1f
        }
        val totalGoal = (baseWater * multiplier).toInt()
        
        _onboardingData.update { 
            it.copy(
                height = height, 
                weight = weight, 
                activityLevel = activityLevel,
                dailyWaterGoal = totalGoal
            ) 
        }
    }

    fun updateActiveHours(wakeUp: String, bedtime: String) {
        _onboardingData.update { it.copy(wakeUpTime = wakeUp, bedtime = bedtime) }
    }

    fun addMedicine(medicine: MedicineData) {
        _onboardingData.update { 
            it.copy(medicines = it.medicines + medicine) 
        }
    }

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun signUpAndComplete(email: String, password: String, name: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val authResult = userRepository.signUp(email, password, name)
            if (authResult.isSuccess) {
                val data = _onboardingData.value.copy(name = name, coins = 50)
                userRepository.saveOnboardingData(data)
                reminderManager.scheduleAllReminders(data)
                onComplete(true)
            } else {
                _errorMessage.value = authResult.exceptionOrNull()?.message ?: "Sign up failed"
                onComplete(false)
            }
            _isLoading.value = false
        }
    }

    fun loginAndComplete(email: String, password: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val authResult = userRepository.logIn(email, password)
            if (authResult.isSuccess) {
                val data = userRepository.getUserData().getOrNull() ?: _onboardingData.value
                userRepository.saveOnboardingData(data)
                reminderManager.scheduleAllReminders(data)
                onComplete(true)
            } else {
                _errorMessage.value = authResult.exceptionOrNull()?.message ?: "Login failed"
                onComplete(false)
            }
            _isLoading.value = false
        }
    }

    fun signInWithGoogleAndComplete(idToken: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val authResult = userRepository.signInWithGoogle(idToken)
            if (authResult.isSuccess) {
                val existingData = userRepository.getUserData().getOrNull()
                val dataToSave = existingData ?: _onboardingData.value.copy(coins = 50)
                userRepository.saveOnboardingData(dataToSave)
                reminderManager.scheduleAllReminders(dataToSave)
                onComplete(true)
            } else {
                _errorMessage.value = authResult.exceptionOrNull()?.message ?: "Google Sign-in failed"
                onComplete(false)
            }
            _isLoading.value = false
        }
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val data = _onboardingData.value.copy(coins = 50)
            userRepository.saveOnboardingData(data)
            reminderManager.scheduleAllReminders(data)
            onComplete()
            _isLoading.value = false
        }
    }
}
