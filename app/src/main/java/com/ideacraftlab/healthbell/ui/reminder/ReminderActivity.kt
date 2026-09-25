package com.ideacraftlab.healthbell.ui.reminder

import android.app.KeyguardManager
import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ideacraftlab.healthbell.ui.theme.HealthBellTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.ideacraftlab.healthbell.domain.repository.UserRepository
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@AndroidEntryPoint
class ReminderActivity : ComponentActivity() {

    @Inject lateinit var userRepository: UserRepository
    private var ringtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ultimate Wake-up logic for Itel/Transsion devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        
        // Force screen on and brightness for visual impact
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val params = window.attributes
        params.screenBrightness = 1.0f
        window.attributes = params

        val type = intent.getStringExtra("type") ?: "water"
        val medName = intent.getStringExtra("med_name") ?: ""
        val medId = intent.getStringExtra("med_id") ?: ""
        val time = intent.getStringExtra("time") ?: ""

        startAlarm()

        setContent {
            val scope = rememberCoroutineScope()
            HealthBellTheme {
                ReminderContent(
                    type = type,
                    name = medName,
                    onAction = {
                        scope.launch {
                            if (type == "water") {
                                val current = userRepository.getUserData().getOrNull()?.currentWaterIntake ?: 0
                                userRepository.updateWaterIntake(current + 250)
                            } else {
                                userRepository.toggleMedicineTaken(medId, time, true)
                            }
                            stopAlarmAndFinish()
                        }
                    },
                    onSkip = {
                        stopAlarmAndFinish()
                    }
                )
            }
        }
    }

    private fun startAlarm() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(applicationContext, notification)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAlarmAndFinish() {
        ringtone?.stop()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtone?.stop()
    }
}

@Composable
fun ReminderContent(
    type: String,
    name: String,
    onAction: () -> Unit,
    onSkip: () -> Unit
) {
    val primaryOrange = Color(0xFFFF5F15)
    val waterBlue = Color(0xFF2196F3)
    val bgColor = if (type == "water") waterBlue else primaryOrange

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (type == "water") Icons.Default.WaterDrop else Icons.Default.Medication,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (type == "water") "Time to Hydrate!" else "Medicine Time!",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (type == "water") "Drink some water now." else "Please take your $name now.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = CircleShape
            ) {
                Text(
                    text = if (type == "water") "I Drank It" else "Mark as Taken",
                    color = bgColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Dismiss / Skip",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
