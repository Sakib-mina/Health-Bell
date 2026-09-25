package com.ideacraftlab.healthbell.domain.repository

import com.ideacraftlab.healthbell.domain.model.MedicineData
import com.ideacraftlab.healthbell.domain.model.OnboardingData

interface UserRepository {
    suspend fun saveOnboardingData(data: OnboardingData): Result<Unit>
    fun isUserLoggedIn(): Boolean
    suspend fun signUp(email: String, password: String, name: String): Result<String>
    suspend fun logIn(email: String, password: String): Result<String>
    suspend fun signInWithGoogle(idToken: String): Result<String>
    suspend fun signOut(): Result<Unit>
    fun getUserEmail(): String?
    suspend fun getUserData(): Result<OnboardingData?>
    fun getUserDataFlow(): kotlinx.coroutines.flow.Flow<OnboardingData?>
    suspend fun updateMedicines(medicines: List<MedicineData>): Result<Unit>
    suspend fun updateWaterIntake(amount: Int): Result<Unit>
    suspend fun toggleMedicineTaken(medicineId: String, time: String, taken: Boolean): Result<Unit>
    suspend fun updateCoins(newBalance: Int): Result<Unit>
}
