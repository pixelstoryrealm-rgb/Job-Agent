package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ApplicationStatus
import com.example.ui.navigation.BottomNavItem
import com.example.ui.screens.applied.AppliedJobsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.jobs.FindJobsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.saved.SavedJobsScreen
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.ApplicationViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.JobViewModel
import com.example.ui.viewmodel.NotificationViewModel
import com.example.ui.viewmodel.ProfileViewModel

@Composable
fun MainAppScreen(
    jobViewModel: JobViewModel,
    applicationViewModel: ApplicationViewModel,
    profileViewModel: ProfileViewModel,
    notificationViewModel: NotificationViewModel,
    authViewModel: AuthViewModel,
    onNavigateToJobDetails: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavItem.Home.route) }

    val savedJobIds by jobViewModel.savedJobIds.collectAsState()
    val applicationSummary by applicationViewModel.summary.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                BottomNavItem.items.forEach { item ->
                    val isSelected = selectedTab == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = item.route },
                        icon = {
                            when (item) {
                                is BottomNavItem.Saved -> {
                                    BadgedBox(
                                        badge = {
                                            if (savedJobIds.isNotEmpty()) {
                                                Badge { Text(savedJobIds.size.toString()) }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title
                                        )
                                    }
                                }
                                is BottomNavItem.Applied -> {
                                    val totalApps = applicationSummary.appliedCount + applicationSummary.shortlistedCount + applicationSummary.interviewCount
                                    BadgedBox(
                                        badge = {
                                            if (totalApps > 0) {
                                                Badge { Text(totalApps.toString()) }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title
                                        )
                                    }
                                }
                                else -> {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                }
                            }
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_${item.route}")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                BottomNavItem.Home.route -> {
                    HomeScreen(
                        jobViewModel = jobViewModel,
                        applicationViewModel = applicationViewModel,
                        profileViewModel = profileViewModel,
                        notificationViewModel = notificationViewModel,
                        onNavigateToJobDetails = onNavigateToJobDetails,
                        onNavigateToNotifications = onNavigateToNotifications,
                        onNavigateToFindJobsWithQuery = { query ->
                            if (query.isNotBlank()) {
                                jobViewModel.onSearchQueryChanged(query)
                            }
                            selectedTab = BottomNavItem.FindJobs.route
                        },
                        onNavigateToApplicationsWithStatus = { status ->
                            applicationViewModel.setStatusFilter(status)
                            selectedTab = BottomNavItem.Applied.route
                        }
                    )
                }
                BottomNavItem.FindJobs.route -> {
                    FindJobsScreen(
                        jobViewModel = jobViewModel,
                        onNavigateToJobDetails = onNavigateToJobDetails
                    )
                }
                BottomNavItem.Applied.route -> {
                    AppliedJobsScreen(
                        applicationViewModel = applicationViewModel,
                        onNavigateToJobDetails = onNavigateToJobDetails
                    )
                }
                BottomNavItem.Saved.route -> {
                    SavedJobsScreen(
                        jobViewModel = jobViewModel,
                        onNavigateToJobDetails = onNavigateToJobDetails
                    )
                }
                BottomNavItem.Profile.route -> {
                    ProfileScreen(
                        profileViewModel = profileViewModel,
                        authViewModel = authViewModel,
                        onLogoutClick = onLogoutClick
                    )
                }
            }
        }
    }
}
