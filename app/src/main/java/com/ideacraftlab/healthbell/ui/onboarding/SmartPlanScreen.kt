package com.ideacraftlab.healthbell.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ideacraftlab.healthbell.domain.model.OnboardingData
import com.ideacraftlab.healthbell.ui.onboarding.components.OnboardingLayout
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.sin

@Composable
fun SmartPlanScreen(
    onboardingData: OnboardingData,
    onStartTracking: () -> Unit
) {
    val primaryOrange = MaterialTheme.colorScheme.primary
    val waterBlue = Color(0xFF2196F3)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val locale = Locale.getDefault()

    // Water Calculation Logic
    val weight = onboardingData.weight.toFloatOrNull() ?: 70f
    val baseWater = weight * 35 // 35ml per kg
    val activityMultiplier = when(onboardingData.activityLevel) {
        "HIGH" -> 1.3f
        "MODERATE" -> 1.15f
        else -> 1f
    }
    val totalWaterMl = (baseWater * activityMultiplier).toInt()
    
    // Frequency Calculation
    val timeFormat = remember { SimpleDateFormat("hh:mm a", locale) }
    val wakeTime = try { timeFormat.parse(onboardingData.wakeUpTime) } catch(e: Exception) { null }
    val bedTime = try { timeFormat.parse(onboardingData.bedtime) } catch(e: Exception) { null }
    
    val activeHours = if (wakeTime != null && bedTime != null) {
        var diff = (bedTime.time - wakeTime.time) / (1000 * 60 * 60)
        if (diff < 0) diff += 24
        diff.coerceAtLeast(1)
    } else 16L
    
    val cups = (totalWaterMl / 250).coerceAtLeast(1) // 250ml per cup
    val frequencyMinutes = (activeHours * 60) / cups

    OnboardingLayout(
        title = "Your Smart Plan",
        subtitle = "We've calculated the perfect routine for you.",
        progress = 1.0f,
        buttonText = "Start Tracking",
        onButtonClick = onStartTracking
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Dynamic Illustration
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.graphicsLayer { scaleX = 0.9f; scaleY = 0.9f },
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedWaterBottle(waterBlue)
                    AnimatedPill(primaryOrange)
                }
            }

            // Calculations Stats
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PlanStatCard(
                        title = "Water Goal",
                        value = "$totalWaterMl ml",
                        icon = Icons.Default.WaterDrop,
                        color = waterBlue,
                        modifier = Modifier.weight(1f)
                    )
                    PlanStatCard(
                        title = "Drink Every",
                        value = "$frequencyMinutes min",
                        icon = Icons.Default.Timer,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                }

                PlanStatCard(
                    title = "Daily Medicines",
                    value = "${onboardingData.medicines.size} Scheduled",
                    icon = Icons.Default.NotificationsActive,
                    color = primaryOrange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Analysis Message
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = primaryOrange.copy(alpha = 0.05f),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryOrange.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Based on your ${onboardingData.weight}kg weight and ${onboardingData.activityLevel.lowercase()} activity, you need to drink about $cups cups of water daily.",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AnimatedPill(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "PillAnimation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PillRotation"
    )

    Canvas(modifier = Modifier.size(80.dp)) {
        val pillWidth = 28.dp.toPx()
        val pillHeight = 56.dp.toPx()
        
        rotate(rotation) {
            drawRoundRect(
                color = color.copy(alpha = 0.5f),
                topLeft = androidx.compose.ui.geometry.Offset(center.x - pillWidth / 2, center.y),
                size = androidx.compose.ui.geometry.Size(pillWidth, pillHeight / 2),
                cornerRadius = CornerRadius(pillWidth / 2, pillWidth / 2)
            )
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(center.x - pillWidth / 2, center.y - pillHeight / 2),
                size = androidx.compose.ui.geometry.Size(pillWidth, pillHeight / 2),
                cornerRadius = CornerRadius(pillWidth / 2, pillWidth / 2)
            )
        }
    }
}

@Composable
fun AnimatedWaterBottle(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaterAnimation")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveOffset"
    )

    Canvas(modifier = Modifier.size(80.dp, 120.dp)) {
        val bottlePath = Path().apply {
            val cornerRadiusPx = 16.dp.toPx()
            addRoundRect(
                RoundRect(
                    left = 10.dp.toPx(),
                    top = 25.dp.toPx(),
                    right = size.width - 10.dp.toPx(),
                    bottom = size.height - 10.dp.toPx(),
                    radiusX = cornerRadiusPx,
                    radiusY = cornerRadiusPx
                )
            )
            moveTo(size.width * 0.35f, 25.dp.toPx())
            lineTo(size.width * 0.35f, 5.dp.toPx())
            lineTo(size.width * 0.65f, 5.dp.toPx())
            lineTo(size.width * 0.65f, 25.dp.toPx())
        }

        drawPath(
            path = bottlePath,
            color = Color.LightGray.copy(alpha = 0.5f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
        )

        clipPath(bottlePath) {
            val waterLevel = size.height * 0.45f
            val wavePath = Path().apply {
                moveTo(0f, waterLevel)
                val step = 10f 
                var x = 0f
                while (x <= size.width) {
                    val y = waterLevel + sin(x * 0.05f + waveOffset) * 5f
                    lineTo(x, y)
                    x += step
                }
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(wavePath, color = color.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun PlanStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, onSurface.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = onSurface.copy(alpha = 0.5f))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp), color = onSurface)
        }
    }
}
