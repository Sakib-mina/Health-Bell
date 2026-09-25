@file:Suppress("DEPRECATION")

package com.ideacraftlab.healthbell

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.ideacraftlab.healthbell.ui.dashboard.DashboardScreen
import com.ideacraftlab.healthbell.ui.onboarding.ActiveHoursScreen
import com.ideacraftlab.healthbell.ui.onboarding.AuthScreen
import com.ideacraftlab.healthbell.ui.onboarding.BodyMetricsScreen
import com.ideacraftlab.healthbell.ui.onboarding.MedicineInputScreen
import com.ideacraftlab.healthbell.ui.onboarding.OnboardingViewModel
import com.ideacraftlab.healthbell.ui.onboarding.SmartPlanScreen
import com.ideacraftlab.healthbell.ui.onboarding.WelcomeScreen
import com.ideacraftlab.healthbell.ui.theme.HealthBellTheme
import dagger.hilt.android.AndroidEntryPoint

@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("LocalContextGetResourceValueCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            HealthBellTheme {
                val navController = rememberNavController()
                val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                val context = LocalContext.current

                // Google Sign In Setup
                val gso = remember {
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                }
                val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

                val googleSignInLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                        account?.idToken?.let { idToken ->
                            onboardingViewModel.signInWithGoogleAndComplete(idToken) { success ->
                                if (success) {
                                    navController.navigate("dashboard") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // Handle error
                    }
                }

                val startDestination = remember {
                    if (onboardingViewModel.isUserLoggedIn()) "dashboard" else "welcome"
                }
                
                NavHost(
                    navController = navController, 
                    startDestination = startDestination,
                    enterTransition = {
                        androidx.compose.animation.slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = androidx.compose.animation.core.tween(150)
                        ) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(150))
                    },
                    exitTransition = {
                        androidx.compose.animation.slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = androidx.compose.animation.core.tween(150)
                        ) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
                    }
                ) {
                    composable("welcome") {
                        WelcomeScreen(
                            onNextClick = {
                                navController.navigate("body_metrics")
                            }
                        )
                    }
                    composable("body_metrics") {
                        BodyMetricsScreen(
                            onNextClick = { height, weight, activity ->
                                onboardingViewModel.updateBodyMetrics(height, weight, activity)
                                navController.navigate("active_hours")
                            }
                        )
                    }
                    composable("active_hours") {
                        ActiveHoursScreen(
                            onNextClick = { wakeUp, bedtime ->
                                onboardingViewModel.updateActiveHours(wakeUp, bedtime)
                                navController.navigate("medicine_input")
                            }
                        )
                    }
                    composable("medicine_input") {
                        MedicineInputScreen(
                            onSkip = {
                                navController.navigate("smart_plan")
                            },
                            onNextClick = { medicineData ->
                                onboardingViewModel.addMedicine(medicineData)
                                navController.navigate("smart_plan")
                            }
                        )
                    }
                    composable("smart_plan") {
                        val onboardingData by onboardingViewModel.onboardingData.collectAsState()
                        SmartPlanScreen(
                            onboardingData = onboardingData,
                            onStartTracking = {
                                navController.navigate("auth_screen")
                            }
                        )
                    }
                    composable("auth_screen") {
                        val isLoading by onboardingViewModel.isLoading.collectAsState()
                        val error by onboardingViewModel.errorMessage.collectAsState()
                        AuthScreen(
                            isLoading = isLoading,
                            error = error,
                            onSignUp = { email, pass, name ->
                                onboardingViewModel.signUpAndComplete(email, pass, name) { success ->
                                    if (success) {
                                        navController.navigate("dashboard") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    }
                                }
                            },
                            onLogin = { email, pass ->
                                onboardingViewModel.loginAndComplete(email, pass) { success ->
                                    if (success) {
                                        navController.navigate("dashboard") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    }
                                }
                            },
                            onGoogleSignIn = {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            }
                        )
                    }
                    composable("dashboard") {
                        DashboardScreen(
                            onLogout = {
                                navController.navigate("auth_screen") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
