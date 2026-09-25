package com.ideacraftlab.healthbell.domain.model

data class OnboardingData(
    val name: String = "",
    val goal: String = "both",
    val height: String = "",
    val weight: String = "",
    val activityLevel: String = "",
    val wakeUpTime: String = "08:00 AM",
    val bedtime: String = "11:00 PM",
    val medicines: List<MedicineData> = emptyList(),
    val dailyWaterGoal: Int = 2000,
    val currentWaterIntake: Int = 0,
    val lastWaterIntakeTime: Long = 0L,
    val takenMedicinesToday: List<String> = emptyList(),
    val weeklyProgress: List<Float> = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.2f),
    val coins: Int = 50
)

data class MedicineData(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val dosage: String = "",
    val form: String = "",
    val times: List<String> = emptyList(),
    val mealRelation: String = ""
)
