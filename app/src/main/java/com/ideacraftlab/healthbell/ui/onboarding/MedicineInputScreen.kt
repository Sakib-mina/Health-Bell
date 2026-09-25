package com.ideacraftlab.healthbell.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.ideacraftlab.healthbell.domain.model.MedicineData
import com.ideacraftlab.healthbell.ui.onboarding.components.AppTimePickerDialog
import com.ideacraftlab.healthbell.ui.onboarding.components.OnboardingLayout
import com.ideacraftlab.healthbell.ui.onboarding.components.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineInputScreen(
    onSkip: () -> Unit,
    onNextClick: (MedicineData) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var form by remember { mutableStateOf("Tablet") }
    var timesPerDay by remember { mutableIntStateOf(1) }
    var mealRelation by remember { mutableStateOf("After Meal") }
    
    val times = remember { mutableStateListOf("08:00 AM", "02:00 PM", "08:00 PM", "11:00 PM") }
    var activeTimeIndex by remember { mutableIntStateOf(-1) }
    
    var showFormDropdown by remember { mutableStateOf(false) }
    val forms = listOf("Tablet", "Capsule", "Syrup", "Injection")
    
    val primaryOrange = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    OnboardingLayout(
        title = "Add Medicine",
        subtitle = "Optional step for scheduling pills.",
        progress = 1.0f,
        buttonText = "Next Step",
        isButtonEnabled = name.isNotBlank() && dosage.isNotBlank(),
        onButtonClick = { 
            onNextClick(
                MedicineData(
                    name = name, 
                    dosage = dosage, 
                    form = form, 
                    times = times.take(timesPerDay).toList(), 
                    mealRelation = mealRelation
                )
            ) 
        },
        secondaryButtonText = "Skip",
        onSecondaryButtonClick = onSkip
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Medicine Name
            OnboardingTextField(
                label = "Medicine name",
                value = name,
                onValueChange = { name = it },
                placeholder = "e.g. Metformin",
                primaryColor = primaryOrange
            )

            // Dosage
            OnboardingTextField(
                label = "Dosage",
                value = dosage,
                onValueChange = { dosage = it },
                placeholder = "1 tablet",
                primaryColor = primaryOrange
            )

            // Form Dropdown
            Column {
                Text("Form", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = onSurface)
                Spacer(modifier = Modifier.height(10.dp))
                Box {
                    Surface(
                        onClick = { showFormDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, onSurface.copy(alpha = 0.1f)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(form, color = onSurface, fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = primaryOrange)
                        }
                    }
                    DropdownMenu(
                        expanded = showFormDropdown,
                        onDismissRequest = { showFormDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.8f).background(MaterialTheme.colorScheme.surface)
                    ) {
                        forms.forEach { f ->
                            DropdownMenuItem(
                                text = { Text(f, color = onSurface) },
                                onClick = {
                                    form = f
                                    showFormDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Times Per Day
            Column {
                Text("Times per day", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3, 4).forEach { count ->
                        val isSelected = timesPerDay == count
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { timesPerDay = count }
                                .border(1.dp, if (isSelected) primaryOrange else onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            color = if (isSelected) primaryOrange.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${count}x", color = if (isSelected) primaryOrange else onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Time Pickers
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(timesPerDay) { index ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { activeTimeIndex = index }
                            .border(1.dp, onSurface.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Time ${index + 1}", style = MaterialTheme.typography.bodyMedium, color = onSurface.copy(alpha = 0.6f))
                            Text(times[index], style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = primaryOrange)
                        }
                    }
                }
            }

            // Meal Relation
            Column {
                Text("Meal Relation", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val relations = listOf("🥣 Before", "🍛 After", "🍎 With")
                    relations.forEach { relation ->
                        val isSelected = mealRelation.contains(relation.split(" ")[1])
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { mealRelation = relation }
                                .border(1.dp, if (isSelected) primaryOrange else onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            color = if (isSelected) primaryOrange.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(relation.split(" ")[0], fontSize = 22.sp)
                                Text(relation.split(" ")[1], style = MaterialTheme.typography.labelSmall, color = if (isSelected) primaryOrange else onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }

        // Time Picker Logic
        if (activeTimeIndex != -1) {
            AppTimePickerDialog(
                onDismiss = { activeTimeIndex = -1 },
                onTimeSelected = { h, m ->
                    times[activeTimeIndex] = formatTime(h, m)
                    activeTimeIndex = -1
                },
                initialHour = 8,
                initialMinute = 0
            )
        }
    }
}

@Composable
fun OnboardingTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    primaryColor: Color
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = onSurface.copy(alpha = 0.4f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
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
