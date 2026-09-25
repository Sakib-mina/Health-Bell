package com.ideacraftlab.healthbell.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.ideacraftlab.healthbell.domain.model.MedicineData
import com.ideacraftlab.healthbell.domain.model.OnboardingData
import com.ideacraftlab.healthbell.domain.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val externalScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())
    private val _userDataFlow = MutableStateFlow<OnboardingData?>(null)
    private var isObserving = false

    override fun isUserLoggedIn(): Boolean = auth.currentUser != null

    private fun startObserving() {
        if (isObserving) return
        val uid = auth.currentUser?.uid ?: return
        isObserving = true
        firestore.collection("users").document(uid).snapshots()
            .map { it.toObject(OnboardingData::class.java) }
            .onEach { _userDataFlow.value = it }
            .launchIn(externalScope)
    }

    override fun getUserDataFlow(): kotlinx.coroutines.flow.Flow<OnboardingData?> {
        startObserving()
        return _userDataFlow.asStateFlow()
    }

    override suspend fun signUp(email: String, password: String, name: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: ""
            // Initial save of the name during sign up
            firestore.collection("users").document(uid).set(mapOf("name" to name)).await()
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logIn(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val uid = result.user?.uid ?: ""
            
            // Check if user document exists, if not create basic one
            val doc = firestore.collection("users").document(uid).get().await()
            if (!doc.exists()) {
                firestore.collection("users").document(uid).set(
                    mapOf(
                        "name" to (result.user?.displayName ?: "Google User"),
                        "email" to (result.user?.email ?: "")
                    )
                ).await()
            }
            
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            isObserving = false
            _userDataFlow.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserEmail(): String? = auth.currentUser?.email

    override suspend fun getUserData(): Result<OnboardingData?> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            val data = document.toObject(OnboardingData::class.java)
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMedicines(medicines: List<MedicineData>): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            firestore.collection("users").document(uid).update("medicines", medicines).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateWaterIntake(amount: Int): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            firestore.collection("users").document(uid).update(
                mapOf(
                    "currentWaterIntake" to amount,
                    "lastWaterIntakeTime" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleMedicineTaken(medicineId: String, time: String, taken: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        val entry = "${medicineId}_$time"
        return try {
            val userRef = firestore.collection("users").document(uid)
            if (taken) {
                userRef.update("takenMedicinesToday", com.google.firebase.firestore.FieldValue.arrayUnion(entry)).await()
            } else {
                userRef.update("takenMedicinesToday", com.google.firebase.firestore.FieldValue.arrayRemove(entry)).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCoins(newBalance: Int): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            firestore.collection("users").document(uid).update("coins", newBalance).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveOnboardingData(data: OnboardingData): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
        return try {
            firestore.collection("users").document(uid).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
