package com.ideacraftlab.healthbell.ui.dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ideacraftlab.healthbell.domain.model.MedicineData
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onManageMedicines: () -> Unit = {}
) {
    val userData by viewModel.userData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val nextWaterTime by viewModel.nextWaterTime.collectAsState()
    val waterStats by viewModel.waterStats.collectAsState()
    val reminder by viewModel.showReminderOverlay.collectAsState()
    
    val primaryOrange = MaterialTheme.colorScheme.primary
    val waterBlue = Color(0xFF2196F3) 
    val bgColor = Color(0xFFFDF9F3)
    val locale = Locale.getDefault()
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMM", locale) }

    val firstName = remember(userData?.name) { 
        userData?.name?.split(" ")?.firstOrNull() ?: "বন্ধু" 
    }

    val progress by remember(userData?.currentWaterIntake, userData?.dailyWaterGoal) {
        derivedStateOf {
            val intake = userData?.currentWaterIntake ?: 0
            val goal = userData?.dailyWaterGoal ?: 2500
            (intake.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
        }
    }

    val context = LocalContext.current
    var showPermissionAlert by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (!android.provider.Settings.canDrawOverlays(context)) {
            showPermissionAlert = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        if (isLoading && userData == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryOrange)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp)) 
                
                HomeHeader(
                    firstName = firstName,
                    dateText = dateFormat.format(Date()),
                    coins = userData?.coins ?: 50
                )

                Spacer(modifier = Modifier.height(28.dp))

                HydrationCard(
                    currentIntake = userData?.currentWaterIntake ?: 0,
                    goal = userData?.dailyWaterGoal ?: 2500,
                    progress = progress,
                    waterStats = waterStats,
                    waterBlue = waterBlue,
                    primaryOrange = primaryOrange,
                    onAddWater = { viewModel.addWater(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                NextReminderCard(nextWaterTime = nextWaterTime)

                Spacer(modifier = Modifier.height(32.dp))

                if (userData?.medicines?.isNotEmpty() == true) {
                    MedicinesSection(
                        medicines = userData?.medicines ?: emptyList(),
                        takenMedicines = userData?.takenMedicinesToday ?: emptyList(),
                        primaryOrange = primaryOrange,
                        onManageMedicines = onManageMedicines,
                        onToggleMedicine = { id, time, taken -> viewModel.toggleMedicine(id, time, taken) }
                    )
                }

                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        if (showPermissionAlert) {
            AlertDialog(
                onDismissRequest = { showPermissionAlert = false },
                title = { Text("Permission Required", fontWeight = FontWeight.Bold) },
                text = { Text("To show reminders while you use other apps or when your screen is off, please allow 'Display over other apps'.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showPermissionAlert = false
                            val intent = Intent(
                                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryOrange)
                    ) {
                        Text("Allow Permission", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionAlert = false }) {
                        Text("Later", color = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.White
            )
        }

        val activeReminder = reminder
        if (activeReminder != null) {
            ReminderOverlayWrapper(
                reminder = activeReminder,
                waterBlue = waterBlue,
                onAction = { type ->
                    when (type) {
                        is HomeViewModel.ReminderType.Water -> viewModel.addWater(type.amount)
                        is HomeViewModel.ReminderType.Medicine -> viewModel.toggleMedicine(type.medId, type.time, true)
                    }
                },
                onSkip = { type ->
                    if (type is HomeViewModel.ReminderType.Medicine) {
                        viewModel.skipMedicine(type.medId, type.time)
                    } else {
                        viewModel.dismissReminder()
                    }
                }
            )
        }
    }
}

@Composable
fun HomeHeader(firstName: String, dateText: String, coins: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hello, $firstName",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2D2D2D)
                )
            )
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFD700).copy(alpha = 0.1f),
            modifier = Modifier.padding(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🪙", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(coins.toString(), fontWeight = FontWeight.Black, color = Color(0xFFDAA520))
            }
        }
    }
}

@Composable
fun HydrationCard(
    currentIntake: Int,
    goal: Int,
    progress: Float,
    waterStats: HomeViewModel.WaterStats,
    waterBlue: Color,
    primaryOrange: Color,
    onAddWater: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TODAY'S HYDRATION",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color.LightGray
                )
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            LargeWaterBottle(
                progress = progress,
                waterColor = waterBlue
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "$currentIntake ml",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = primaryOrange
                )
            )
            
            Text(
                text = "of $goal ml goal",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HomeStatItem("Every", "${waterStats.frequencyMinutes} min", Icons.Default.Timer, waterBlue)
                HomeStatItem("Goal", "${waterStats.mlPerDrink} ml", Icons.Default.WaterDrop, waterBlue)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickAddCircle("+100", waterBlue) { onAddWater(100) }
                QuickAddCircle("+250", waterBlue) { onAddWater(250) }
                QuickAddCircle("+500", waterBlue) { onAddWater(500) }
            }
        }
    }
}

@Composable
fun NextReminderCard(nextWaterTime: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF2D4F44) 
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Next water reminder",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = if (nextWaterTime.contains("AM") || nextWaterTime.contains("PM")) "at $nextWaterTime" else nextWaterTime,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )
            }
            Icon(Icons.Default.NotificationsActive, null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun MedicinesSection(
    medicines: List<MedicineData>,
    takenMedicines: List<String>,
    primaryOrange: Color,
    onManageMedicines: () -> Unit,
    onToggleMedicine: (String, String, Boolean) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Medicine",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF2D2D2D)
            )
            TextButton(onClick = onManageMedicines) {
                Text("Manage", color = primaryOrange, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        medicines.forEach { medicine ->
            MedicineHomeCard(
                medicine = medicine,
                takenMedicines = takenMedicines,
                onToggle = { time, taken -> onToggleMedicine(medicine.id, time, taken) },
                primaryColor = primaryOrange
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MedicineHomeCard(
    medicine: MedicineData, 
    takenMedicines: List<String>, 
    onToggle: (String, Boolean) -> Unit, 
    primaryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = medicine.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2D2D2D)
                        )
                        Text(
                            text = "${medicine.dosage} • ${medicine.form} • ${medicine.mealRelation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                medicine.times.forEach { time ->
                    val isTaken = takenMedicines.contains("${medicine.id}_$time")
                    
                    Surface(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggle(time, !isTaken) }
                            .border(
                                width = 1.dp,
                                color = if (isTaken) Color(0xFF4CAF50) else Color(0xFFEEEEEE),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        color = if (isTaken) Color(0xFF4CAF50).copy(alpha = 0.08f) else Color.White
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = time,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isTaken) Color(0xFF2E7D32) else Color.Gray
                            )
                            if (isTaken) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Check, 
                                    null, 
                                    tint = Color(0xFF4CAF50), 
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderOverlayWrapper(
    reminder: HomeViewModel.ReminderType,
    waterBlue: Color,
    onAction: (HomeViewModel.ReminderType) -> Unit,
    onSkip: (HomeViewModel.ReminderType) -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        ReminderOverlay(
            reminder = reminder,
            onAction = onAction,
            onSkip = onSkip,
            waterBlue = waterBlue
        )
    }
}

@Composable
fun ReminderOverlay(
    reminder: HomeViewModel.ReminderType,
    onAction: (HomeViewModel.ReminderType) -> Unit,
    onSkip: (HomeViewModel.ReminderType) -> Unit,
    waterBlue: Color
) {
    val primaryOrange = MaterialTheme.colorScheme.primary
    val bgColor = if (reminder is HomeViewModel.ReminderType.Water) waterBlue else primaryOrange
    
    var selectedAmount by remember { mutableIntStateOf(250) }

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
                imageVector = if (reminder is HomeViewModel.ReminderType.Water) Icons.Default.WaterDrop else Icons.Default.Medication,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(120.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = if (reminder is HomeViewModel.ReminderType.Water) "Time to Hydrate!" else "Medicine Time!",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = when(reminder) {
                    is HomeViewModel.ReminderType.Water -> "How much water did you drink?"
                    is HomeViewModel.ReminderType.Medicine -> "Please take your ${reminder.name} now."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            
            if (reminder is HomeViewModel.ReminderType.Water) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(100, 250, 500).forEach { amount ->
                        val isSelected = selectedAmount == amount
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable { selectedAmount = amount },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                            border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${amount}ml",
                                    color = if (isSelected) bgColor else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { 
                    if (reminder is HomeViewModel.ReminderType.Water) {
                        onAction(HomeViewModel.ReminderType.Water(selectedAmount))
                    } else {
                        onAction(reminder)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = CircleShape
            ) {
                Text(
                    text = if (reminder is HomeViewModel.ReminderType.Water) "Confirm Intake" else "Mark as Taken",
                    color = bgColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { onSkip(reminder) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (reminder is HomeViewModel.ReminderType.Water) "Not now" else "Skip / Overdue",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun HomeStatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "$label: ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2D2D2D))
    }
}

@Composable
fun LargeWaterBottle(progress: Float, waterColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "HomeWater")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "HomeWave"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.graphicsLayer { clip = true } 
    ) {
        Canvas(modifier = Modifier.size(140.dp, 260.dp)) {
            val bottlePath = Path().apply {
                val cornerRadiusPx = 35.dp.toPx()
                addRoundRect(
                    RoundRect(
                        left = 15.dp.toPx(),
                        top = 50.dp.toPx(),
                        right = size.width - 15.dp.toPx(),
                        bottom = size.height - 15.dp.toPx(),
                        radiusX = cornerRadiusPx,
                        radiusY = cornerRadiusPx
                    )
                )
                moveTo(size.width * 0.38f, 50.dp.toPx())
                lineTo(size.width * 0.38f, 20.dp.toPx())
                lineTo(size.width * 0.62f, 20.dp.toPx())
                lineTo(size.width * 0.62f, 50.dp.toPx())
            }

            drawPath(
                path = bottlePath,
                color = Color.LightGray.copy(alpha = 0.3f),
                style = Stroke(width = 4.dp.toPx())
            )
            
            drawRoundRect(
                color = Color(0xFF795548), 
                topLeft = Offset(size.width * 0.38f, 5.dp.toPx()),
                size = Size(size.width * 0.24f, 12.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            clipPath(bottlePath) {
                val topBound = 50.dp.toPx()
                val bottomBound = size.height - 15.dp.toPx()
                val waterLevel = bottomBound - (progress * (bottomBound - topBound))
                
                val wavePath = Path().apply {
                    moveTo(0f, waterLevel)
                    val step = 20f 
                    var x = 0f
                    while (x <= size.width) {
                        val y = waterLevel + sin(x * 0.04f + waveOffset) * 6f
                        lineTo(x, y)
                        x += step
                    }
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(wavePath, color = waterColor.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun QuickAddCircle(value: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier.size(68.dp),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(2.dp, color.copy(alpha = 0.1f)),
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "ml", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}
