package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Main : Screen("main")
    object JobDetails : Screen("job_details/{jobId}") {
        fun createRoute(jobId: String) = "job_details/$jobId"
    }
    object Notifications : Screen("notifications")
}
