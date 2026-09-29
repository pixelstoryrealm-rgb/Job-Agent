package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.repository.AuthStateStatus
import com.example.ui.navigation.Screen
import com.example.ui.screens.MainAppScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.jobs.JobDetailsScreen
import com.example.ui.screens.notifications.NotificationScreen
import com.example.ui.theme.YourJobTheme
import com.example.ui.viewmodel.AppContainer
import com.example.ui.viewmodel.AppViewModelFactory
import com.example.ui.viewmodel.ApplicationViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.JobViewModel
import com.example.ui.viewmodel.NotificationViewModel
import com.example.ui.viewmodel.ProfileViewModel

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(applicationContext)

        enableEdgeToEdge()

        setContent {
            YourJobTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    YourJobApp(appContainer = appContainer)
                }
            }
        }
    }
}

@Composable
fun YourJobApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    val factory = AppViewModelFactory(appContainer)

    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val jobViewModel: JobViewModel = viewModel(factory = factory)
    val applicationViewModel: ApplicationViewModel = viewModel(factory = factory)
    val profileViewModel: ProfileViewModel = viewModel(factory = factory)
    val notificationViewModel: NotificationViewModel = viewModel(factory = factory)

    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    if (authState.status == AuthStateStatus.AUTHENTICATED) {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        // Register Screen
        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Forgot Password Screen
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Main App Screen (Hosts persistent Bottom Navigation)
        composable(Screen.Main.route) {
            MainAppScreen(
                jobViewModel = jobViewModel,
                applicationViewModel = applicationViewModel,
                profileViewModel = profileViewModel,
                notificationViewModel = notificationViewModel,
                authViewModel = authViewModel,
                onNavigateToJobDetails = { jobId ->
                    navController.navigate(Screen.JobDetails.createRoute(jobId))
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onLogoutClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        // Job Details Screen
        composable(
            route = Screen.JobDetails.route,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            JobDetailsScreen(
                jobId = jobId,
                jobViewModel = jobViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Notifications Screen
        composable(Screen.Notifications.route) {
            NotificationScreen(
                notificationViewModel = notificationViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToJob = { jobId ->
                    navController.navigate(Screen.JobDetails.createRoute(jobId))
                }
            )
        }
    }
}
