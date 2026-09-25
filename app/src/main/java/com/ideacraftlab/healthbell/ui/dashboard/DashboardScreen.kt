package com.ideacraftlab.healthbell.ui.dashboard

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Progress : BottomNavItem("progress", "Progress", Icons.Default.BarChart)
    object Medicine : BottomNavItem("medicine", "Medicine", Icons.Default.Medication)
    object Shop : BottomNavItem("shop", "Shop", Icons.Default.ShoppingCart)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}

@Composable
fun DashboardScreen(
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val primaryOrange = Color(0xFFFF5F15)

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Progress,
        BottomNavItem.Medicine,
        BottomNavItem.Shop,
        BottomNavItem.Profile
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier.border(0.5.dp, Color(0xFFEEEEEE), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { 
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            ) 
                        },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryOrange,
                            selectedTextColor = primaryOrange,
                            indicatorColor = Color.Transparent, // Removed the "girly" pale background
                            unselectedIconColor = Color(0xFF757575),
                            unselectedTextColor = Color(0xFF757575)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(BottomNavItem.Home.route) { 
                HomeScreen(
                    onManageMedicines = {
                        navController.navigate(BottomNavItem.Medicine.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                ) 
            }
            composable(BottomNavItem.Progress.route) { ProgressScreen() }
            composable(BottomNavItem.Medicine.route) { MedicineScreen() }
            composable(BottomNavItem.Shop.route) { ShopScreen() }
            composable(BottomNavItem.Profile.route) { 
                ProfileScreen(onLogout = onLogout) 
            }
        }
    }
}

