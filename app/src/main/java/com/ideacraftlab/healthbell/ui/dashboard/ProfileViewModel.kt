package com.ideacraftlab.healthbell.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideacraftlab.healthbell.domain.model.OnboardingData
import com.ideacraftlab.healthbell.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userData = MutableStateFlow<OnboardingData?>(null)
    val userData: StateFlow<OnboardingData?> = _userData.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    init {
        observeUserData()
        _userEmail.value = userRepository.getUserEmail() ?: ""
    }

    private fun observeUserData() {
        userRepository.getUserDataFlow()
            .onEach { _userData.value = it }
            .launchIn(viewModelScope)
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            userRepository.signOut().onSuccess {
                onComplete()
            }
        }
    }
}
