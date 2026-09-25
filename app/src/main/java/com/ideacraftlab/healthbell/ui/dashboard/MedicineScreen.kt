package com.ideacraftlab.healthbell.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ideacraftlab.healthbell.domain.model.MedicineData
import com.ideacraftlab.healthbell.ui.onboarding.components.AppTimePickerDialog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MedicineScreen(
    viewModel: MedicineViewModel = hiltViewModel()
) {
    val medicines by viewModel.medicines.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userCoins by viewModel.userCoins.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val primaryOrange = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.background
    
    var showAddSheet by remember { mutableStateOf(false) }
    var editingMedicine by remember { mutableStateOf<MedicineData?>(null) }
    var showCoinConfirmation by remember { mutableStateOf<MedicineData?>(null) }

    val sheetState = rememberModalBottomSheetState()
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMM", Locale.getDefault()) }
    val firstName = remember(userName) { 
        userName.split(" ").firstOrNull() ?: "বন্ধু" 
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header - Top spacing fixed
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, $firstName",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = dateFormat.format(Date()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                
                // Coins indicator
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.1f), // Gold color
                    modifier = Modifier.padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(userCoins.toString(), fontWeight = FontWeight.Bold, color = Color(0xFFDAA520))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryOrange)
                }
            } else if (medicines.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Text(
                        "No medicines added yet.", 
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 100.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(medicines) { medicine ->
                        MedicineCard(
                            medicine = medicine,
                            onEdit = {
                                editingMedicine = it
                                showAddSheet = true
                            },
                            onDelete = { viewModel.deleteMedicine(it.id) }
                        )
                    }
                }
            }
        }

        // FAB positioned correctly relative to the entire screen
        FloatingActionButton(
            onClick = { 
                editingMedicine = null
                showAddSheet = true 
            },
            containerColor = primaryOrange,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Medicine")
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            MedicineForm(
                initialMedicine = editingMedicine,
                onSave = {
                    if (editingMedicine == null) {
                        showCoinConfirmation = it
                    } else {
                        viewModel.updateMedicine(it)
                    }
                    showAddSheet = false
                }
            )
        }
    }

    if (showCoinConfirmation != null) {
        AlertDialog(
            onDismissRequest = { showCoinConfirmation = null },
            title = { Text("Confirm Addition") },
            text = { Text("Adding this medicine will cost 10 coins. Your current balance: $userCoins coins.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addMedicine(showCoinConfirmation!!)
                        showCoinConfirmation = null
                    },
                    enabled = userCoins >= 10,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryOrange)
                ) {
                    Text("Confirm (-10 🪙)", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCoinConfirmation = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun MedicineCard(
    medicine: MedicineData,
    onEdit: (MedicineData) -> Unit,
    onDelete: (MedicineData) -> Unit
) {
    val primaryOrange = MaterialTheme.colorScheme.primary
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = medicine.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val subText = if (medicine.dosage.isNotBlank()) {
                        "${medicine.dosage} • ${medicine.times.size}x daily"
                    } else {
                        "${medicine.times.size}x daily"
                    }
                    Text(
                        text = subText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (medicine.mealRelation.isNotBlank()) {
                        Text(
                            text = medicine.mealRelation,
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryOrange,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = medicine.form,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Chips - FlowRow to handle multiple times without hiding
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                medicine.times.forEach { time ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("✓", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onEdit(medicine) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit", color = MaterialTheme.colorScheme.onSurface)
                }
                
                OutlinedButton(
                    onClick = { onDelete(medicine) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFD32F2F))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete", color = Color(0xFFD32F2F))
                }
            }
        }
    }
}

@Composable
fun MedicineForm(
    initialMedicine: MedicineData?,
    onSave: (MedicineData) -> Unit
) {
    var name by remember { mutableStateOf(initialMedicine?.name ?: "") }
    var dosage by remember { mutableStateOf(initialMedicine?.dosage ?: "") }
    var form by remember { mutableStateOf(initialMedicine?.form ?: "Tablet") }
    var mealRelation by remember { mutableStateOf(initialMedicine?.mealRelation ?: "After Meal") }
    var frequency by remember { mutableIntStateOf(initialMedicine?.times?.size ?: 1) }
    val selectedTimes = remember { 
        mutableStateListOf<String>().apply { 
            addAll(initialMedicine?.times ?: listOf("08:00 AM")) 
        } 
    }
    
    var timePickerIdx by remember { mutableIntStateOf(-1) }

    val forms = listOf("Tablet", "Capsule", "Syrup", "Injection")
    val mealOptions = listOf("Before Meal", "After Meal", "With Meal")
    val frequencies = listOf(1, 2, 3, 4)

    val primaryOrange = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (initialMedicine == null) "Add New Medicine" else "Edit Medicine",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Medicine Name
        Text("Medicine name", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("e.g. Metformin") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Dosage
            Column(modifier = Modifier.weight(1f)) {
                Text("Dosage", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    placeholder = { Text("1 tablet") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            
            // Form
            Column(modifier = Modifier.weight(1f)) {
                Text("Form", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = form,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurface) },
                        modifier = Modifier.clickable { expanded = true },
                        shape = RoundedCornerShape(12.dp),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            disabledTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
                    DropdownMenu(
                        expanded = expanded, 
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        forms.forEach { 
                            DropdownMenuItem(
                                text = { Text(it, color = MaterialTheme.colorScheme.onSurface) }, 
                                onClick = { form = it; expanded = false }
                            ) 
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Meal Relation
        Text("When to take", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            mealOptions.forEach { option ->
                val selected = mealRelation == option
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { mealRelation = option },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) primaryOrange else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.padding(vertical = 12.dp),
                        textAlign = TextAlign.Center,
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Frequency
        Text("Times per day", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            frequencies.forEach { freq ->
                val selected = frequency == freq
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { 
                            frequency = freq 
                            while (selectedTimes.size < freq) selectedTimes.add("08:00 AM")
                            while (selectedTimes.size > freq) selectedTimes.removeAt(selectedTimes.size - 1)
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${freq}x", color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Times
        selectedTimes.forEachIndexed { index, time ->
            Text("Time ${index + 1}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = time,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.AccessTime, null, tint = MaterialTheme.colorScheme.onSurface) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { timePickerIdx = index },
                shape = RoundedCornerShape(12.dp),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    disabledTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                onSave(
                    MedicineData(
                        id = initialMedicine?.id ?: java.util.UUID.randomUUID().toString(),
                        name = name,
                        dosage = dosage,
                        form = form,
                        times = selectedTimes.toList(),
                        mealRelation = mealRelation
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(16.dp),
            enabled = name.isNotBlank()
        ) {
            Text("Save Medicine", fontWeight = FontWeight.Bold, color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }

    if (timePickerIdx != -1) {
        AppTimePickerDialog(
            initialHour = 8,
            initialMinute = 0,
            onDismiss = { timePickerIdx = -1 },
            onTimeSelected = { h, m ->
                val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m) }
                selectedTimes[timePickerIdx] = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
                timePickerIdx = -1
            }
        )
    }
}
