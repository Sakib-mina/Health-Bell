package com.ideacraftlab.healthbell.ui.dashboard

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    val userData by viewModel.userData.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val bgColor = Color(0xFFF5F2E9)
    val primaryOrange = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
        )
        Text(
            text = "Your details & settings",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // User Info Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            val name = userData?.name ?: "বন্ধু"
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color(0xFF2D4F44),
                border = BorderStroke(2.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = name.firstOrNull()?.toString() ?: "ব",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(text = name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text(text = userEmail, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Stats Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileStatCard(userData?.dailyWaterGoal?.toString() ?: "2000", "Daily water goal (ml)", Modifier.weight(1f))
            ProfileStatCard(userData?.medicines?.size?.toString() ?: "0", "Active medicines", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Details Section
        ProfileSection(title = "Details") {
            DetailItem("Age", "-")
            DetailItem("Weight", "${userData?.weight ?: "-"} kg")
            DetailItem("Awake hours", "${userData?.wakeUpTime ?: "08:00"} - ${userData?.bedtime ?: "23:00"}")
            DetailItem("Reminder interval", "Every 2h") // Static for now or dynamic based on logic
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Settings / Legal Section
        ProfileSection(title = "Settings") {
            LegalItem("Privacy Policy", Icons.Default.PrivacyTip) {
                val intent = Intent(Intent.ACTION_VIEW, "https://sites.google.com/view/healthbell-privacy-policy/home".toUri())
                context.startActivity(intent)
            }
            LegalItem("Terms & Conditions", Icons.Default.Description) {
                val intent = Intent(Intent.ACTION_VIEW, "https://sites.google.com/view/healthbell-terms-condiiton/home".toUri())
                context.startActivity(intent)
            }
            LegalItem("Support Us", Icons.Default.Info) {
                showAboutDialog = true
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Log Out Button
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFF0F0F0))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = primaryOrange, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Log Out", color = primaryOrange, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showAboutDialog) {
        AboutUsDialog(onDismiss = { showAboutDialog = false })
    }

    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout(onLogout)
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
fun LogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Out", fontWeight = FontWeight.Bold) },
        text = { Text("Are you sure you want to log out of your account?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Log Out", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}

@Composable
fun ProfileStatCard(value: String, label: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2D4F44)))
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color(0xFF2D2D2D), fontWeight = FontWeight.Medium)
            Text(text = value, color = Color.Gray)
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
    }
}

@Composable
fun LegalItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = label, color = Color(0xFF2D2D2D), fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
    }
}

@Composable
fun AboutUsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5F15)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close", color = Color.White)
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = Color(0xFFFF5F15).copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("HB", fontWeight = FontWeight.Black, color = Color(0xFFFF5F15), fontSize = 24.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Support Email", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Health Bell is your ultimate companion for a healthier lifestyle. We focus on simplicity and efficiency to help you stay hydrated and never miss a dose of your medicines.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text("Version: 1.0.0", color = Color.Gray, fontSize = 12.sp)
                Text("Support: rokoncloud@gmail.com", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Designed with ❤️ for your wellbeing.", color = Color(0xFFFF5F15), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}
