package com.ideacraftlab.healthbell.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ideacraftlab.healthbell.ui.onboarding.components.OnboardingLayout

enum class ActivityLevel(val emoji: String, val title: String) {
    LIGHT("🧘‍♂️", "Light"),
    MODERATE("🏃", "Moderate"),
    HIGH("🏋️", "High")
}

@Composable
fun BodyMetricsScreen(
    onNextClick: (String, String, String) -> Unit
) {
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf(ActivityLevel.MODERATE) }
    
    val primaryOrange = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    OnboardingLayout(
        title = "Body Metrics",
        subtitle = "Help us know you better to provide accurate health reminders.",
        progress = 0.9f,
        onButtonClick = { onNextClick(height, weight, activityLevel.name) }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Height Input
            MetricInput(
                label = "How tall are you?",
                value = height,
                onValueChange = { height = it },
                unit = "cm",
                placeholder = "e.g. 170",
                primaryColor = primaryOrange
            )

            // Weight Input
            MetricInput(
                label = "What's your weight?",
                value = weight,
                onValueChange = { weight = it },
                unit = "kg",
                placeholder = "e.g. 65",
                primaryColor = primaryOrange
            )

            // Activity Level Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Daily Activity Level",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = onSurface
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActivityLevel.entries.forEach { level ->
                        ActivityOption(
                            level = level,
                            isSelected = activityLevel == level,
                            onClick = { activityLevel = level },
                            modifier = Modifier.weight(1f),
                            primaryColor = primaryOrange
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    placeholder: String,
    primaryColor: Color
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), 
            color = onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = onSurface.copy(alpha = 0.4f)) },
            suffix = { Text(unit, fontWeight = FontWeight.Bold, color = primaryColor) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = onSurface.copy(alpha = 0.1f),
                focusedTextColor = onSurface,
                unfocusedTextColor = onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@Composable
fun ActivityOption(
    level: ActivityLevel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(
                width = 1.5.dp,
                color = if (isSelected) primaryColor else onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ),
        color = if (isSelected) primaryColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = level.emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = level.title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) primaryColor else onSurface.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
