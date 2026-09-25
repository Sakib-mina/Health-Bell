package com.ideacraftlab.healthbell.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ideacraftlab.healthbell.ui.onboarding.components.AppTimePickerDialog
import com.ideacraftlab.healthbell.ui.onboarding.components.OnboardingLayout
import com.ideacraftlab.healthbell.ui.onboarding.components.formatTime

@Composable
fun ActiveHoursScreen(
    onNextClick: (String, String) -> Unit
) {
    var wakeUpTime by remember { mutableStateOf("07:00 AM") }
    var bedtime by remember { mutableStateOf("11:00 PM") }
    
    var showWakeUpPicker by remember { mutableStateOf(false) }
    var showBedtimePicker by remember { mutableStateOf(false) }

    val primaryOrange = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    OnboardingLayout(
        title = "Active Hours",
        subtitle = "Set waking hours so alerts stop during rest.",
        progress = 1.0f,
        onButtonClick = { onNextClick(wakeUpTime, bedtime) }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            
            // Wake-up Time Selection
            TimeSelectorCard(
                label = "Wake-up Time",
                time = wakeUpTime,
                onClick = { showWakeUpPicker = true },
                primaryColor = primaryOrange
            )

            // Bedtime Selection
            TimeSelectorCard(
                label = "Bedtime",
                time = bedtime,
                onClick = { showBedtimePicker = true },
                primaryColor = primaryOrange
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            // Info Message
            val infoColor = Color(0xFF1976D2)
            Surface(
                color = infoColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = infoColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Reminders will automatically distribute evenly during these hours.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = infoColor,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }

        // Time Picker Dialogs
        if (showWakeUpPicker) {
            AppTimePickerDialog(
                onDismiss = { showWakeUpPicker = false },
                onTimeSelected = { h, m ->
                    wakeUpTime = formatTime(h, m)
                    showWakeUpPicker = false
                },
                initialHour = 7,
                initialMinute = 0
            )
        }

        if (showBedtimePicker) {
            AppTimePickerDialog(
                onDismiss = { showBedtimePicker = false },
                onTimeSelected = { h, m ->
                    bedtime = formatTime(h, m)
                    showBedtimePicker = false
                },
                initialHour = 23,
                initialMinute = 0
            )
        }
    }
}

@Composable
fun TimeSelectorCard(
    label: String,
    time: String,
    onClick: () -> Unit,
    primaryColor: Color
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable { onClick() }
                .border(
                    width = 2.dp,
                    color = onSurface.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(20.dp)
                ),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(primaryColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = time,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = onSurface
                        )
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select time",
                    tint = onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
