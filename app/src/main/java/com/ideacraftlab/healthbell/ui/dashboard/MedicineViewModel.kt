package com.ideacraftlab.healthbell.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideacraftlab.healthbell.domain.model.MedicineData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicineViewModel @Inject constructor(
    private val userRepository: com.ideacraftlab.healthbell.domain.repository.UserRepository,
    private val reminderManager: com.ideacraftlab.healthbell.data.manager.ReminderManager
) : ViewModel() {

    private val _medicines = MutableStateFlow<List<MedicineData>>(emptyList())
    val medicines: StateFlow<List<MedicineData>> = _medicines.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userCoins = MutableStateFlow(50)
    val userCoins: StateFlow<Int> = _userCoins.asStateFlow()

    init {
        observeUserData()
    }

    private fun observeUserData() {
        userRepository.getUserDataFlow()
            .onStart { _isLoading.value = true }
            .onEach { data ->
                _medicines.value = data?.medicines ?: emptyList()
                _userName.value = data?.name ?: "বন্ধু"
                _userCoins.value = data?.coins ?: 50
                _isLoading.value = false
                if (data != null) {
                    reminderManager.scheduleAllReminders(data)
                }
            }
            .launchIn(viewModelScope)
    }

    fun addMedicine(medicine: MedicineData) {
        viewModelScope.launch {
            if (_userCoins.value >= 10) {
                val newList = _medicines.value + medicine
                userRepository.updateMedicines(newList)
                userRepository.updateCoins(_userCoins.value - 10)
            }
        }
    }

    fun deleteMedicine(id: String) {
        viewModelScope.launch {
            val newList = _medicines.value.filter { it.id != id }
            userRepository.updateMedicines(newList)
        }
    }

    fun updateMedicine(updatedMedicine: MedicineData) {
        viewModelScope.launch {
            val newList = _medicines.value.map { 
                if (it.id == updatedMedicine.id) updatedMedicine else it 
            }
            userRepository.updateMedicines(newList)
        }
    }
}
