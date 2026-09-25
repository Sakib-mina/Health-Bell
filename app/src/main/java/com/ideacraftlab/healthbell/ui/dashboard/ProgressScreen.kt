package com.ideacraftlab.healthbell.ui.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val isWaterSelected by viewModel.isWaterSelected.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val primaryColor = if (isWaterSelected) Color(0xFF2D4F44) else Color(0xFFA44A3F)
    val bgColor = Color(0xFFF5F2E9)

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        if (isLoading && userData == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF5F15))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Progress Insights",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2D2D2D)
                    )
                )
                Text(
                    text = "Track your health journey",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Custom Tab Switcher
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        TabItem(
                            text = "Water",
                            isSelected = isWaterSelected,
                            color = Color(0xFF2D4F44),
                            modifier = Modifier.weight(1f)
                        ) { viewModel.setTab(isWater = true) }
                        
                        TabItem(
                            text = "Medicine",
                            isSelected = !isWaterSelected,
                            color = Color(0xFFA44A3F),
                            modifier = Modifier.weight(1f)
                        ) { viewModel.setTab(isWater = false) }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Grid
                val waterGoal = userData?.dailyWaterGoal ?: 2500
                val currentWater = userData?.currentWaterIntake ?: 0
                val waterAdherence = if (waterGoal > 0) ((currentWater.toFloat() / waterGoal) * 100).toInt() else 0
                
                val totalMeds = userData?.medicines?.size ?: 0
                val takenMeds = userData?.takenMedicinesToday?.size ?: 0
                val medAdherence = if (totalMeds > 0) ((takenMeds.toFloat() / totalMeds) * 100).toInt() else 0

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(if(isWaterSelected) (if(currentWater >= waterGoal) "1" else "0") else (if(medAdherence == 100) "1" else "0"), "Current streak", Modifier.weight(1f))
                    StatCard("31", "Best streak", Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        value = if(isWaterSelected) currentWater.toString() else "$medAdherence%", 
                        label = if(isWaterSelected) "Today (ml)" else "Today's Adherence", 
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(if(isWaterSelected) "$waterAdherence%" else "92%", "Overall adherence", Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // GitHub Style Streak (Contribution Map)
                ProgressCard(title = "Last 30 Days") {
                    ContributionGrid(primaryColor, viewModel.getStreakData())
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Monthly Trend (Line Chart)
                ProgressCard(title = "Monthly Trend") {
                    LineChart(primaryColor, viewModel.getMonthlyTrend())
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Weekly Adherence (Bar Chart)
                ProgressCard(title = "Weekly Adherence (%)") {
                    WeeklyBarChart(primaryColor, viewModel.getWeeklyAdherence())
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Overall Split (Donut Chart)
                ProgressCard(title = "Overall Split") {
                    val split = viewModel.getOverallSplit()
                    OverallSplitChart(split.first, split.second)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.Gray
            )
        )
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun ProgressCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                if (title == "Last 7 Days" || title == "Weekly Adherence (%)") {
                    Text(text = "View all", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun ContributionGrid(color: Color, streakData: List<Boolean>) {
    Column {
        // 10 columns x 3 rows for a clean 30-day look
        val columns = 10
        val rows = 3
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(columns) { col ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(rows) { row ->
                        val index = col * rows + row
                        val goalMet = streakData.getOrNull(index) ?: false
                        
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (goalMet) color else color.copy(alpha = 0.05f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (goalMet) Color.Transparent else Color(0xFFEEEEEE),
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text("Missed ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color.copy(alpha = 0.05f)).border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(3.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
            Text(" Goal Met", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun LineChart(color: Color, data: List<Float>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
        val path = Path()
        val stepX = size.width / (data.size - 1)
        
        // Draw grid lines
        repeat(5) { i ->
            val y = size.height - (i * size.height / 4)
            drawLine(Color.LightGray.copy(alpha = 0.3f), Offset(0f, y), Offset(size.width, y))
        }

        data.forEachIndexed { i, value ->
            val x = i * stepX
            val y = size.height - (value * size.height)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(path, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        
        // Fill area
        val fillPath = path
        fillPath.lineTo(size.width, size.height)
        fillPath.lineTo(0f, size.height)
        fillPath.close()
        drawPath(fillPath, Brush.verticalGradient(listOf(color.copy(alpha = 0.2f), Color.Transparent)))
    }
}

@Composable
fun WeeklyBarChart(color: Color, data: List<Float>) {
    Row(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        val labels = listOf("W-1", "W-2", "W-3", "W-4", "W-5", "W-6", "W-7", "W-8")
        data.forEachIndexed { i, value ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .fillMaxHeight(value)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(color)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(labels[i], style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun OverallSplitChart(waterRatio: Float, medRatio: Float) {
    val waterColor = Color(0xFF2D4F44)
    val medColor = Color(0xFFA44A3F)
    
    val total = (waterRatio + medRatio).coerceAtLeast(0.01f)
    val waterSweep = (waterRatio / total) * 360f
    val medSweep = (medRatio / total) * 360f
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.size(120.dp)) {
            drawArc(
                color = waterColor,
                startAngle = -90f,
                sweepAngle = waterSweep,
                useCenter = false,
                style = Stroke(width = 25.dp.toPx())
            )
            drawArc(
                color = medColor,
                startAngle = -90f + waterSweep,
                sweepAngle = medSweep,
                useCenter = false,
                style = Stroke(width = 25.dp.toPx())
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem("Water", waterColor)
            LegendItem("Medicine", medColor)
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}
