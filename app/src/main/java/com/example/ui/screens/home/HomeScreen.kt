package com.example.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApplicationStatus
import com.example.data.model.Job
import com.example.ui.components.AiJobSwipeCard
import com.example.ui.components.ApplicationSummarySection
import com.example.ui.components.JobItemCard
import com.example.ui.components.QuickActionsBottomSheet
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AiGlow
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.ApplicationViewModel
import com.example.ui.viewmodel.JobActionEffect
import com.example.ui.viewmodel.JobViewModel
import com.example.ui.viewmodel.NotificationViewModel
import com.example.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    jobViewModel: JobViewModel,
    applicationViewModel: ApplicationViewModel,
    profileViewModel: ProfileViewModel,
    notificationViewModel: NotificationViewModel,
    onNavigateToJobDetails: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToFindJobsWithQuery: (String) -> Unit,
    onNavigateToApplicationsWithStatus: (ApplicationStatus?) -> Unit
) {
    val context = LocalContext.current
    val allJobs by jobViewModel.allJobs.collectAsState()
    val isRefreshing by jobViewModel.isRefreshing.collectAsState()
    val savedJobIds by jobViewModel.savedJobIds.collectAsState()
    val selectedQuickActionJob by jobViewModel.selectedJobForQuickActions.collectAsState()
    val userProfile by profileViewModel.userProfile.collectAsState()
    val unreadNotifCount by notificationViewModel.unreadCount.collectAsState()
    val applicationSummary by applicationViewModel.summary.collectAsState()

    val aiMatchDeck = jobViewModel.getActiveAiMatchJobs()
    val currentCardIndex by jobViewModel.aiCardIndex.collectAsState()

    // Listen to effects
    LaunchedEffect(Unit) {
        jobViewModel.actionEffects.collect { effect ->
            when (effect) {
                is JobActionEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is JobActionEffect.NavigateToDetails -> {
                    onNavigateToJobDetails(effect.jobId)
                }
                is JobActionEffect.OpenQuickActions -> {
                    jobViewModel.onCardLongPress(effect.job)
                }
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { jobViewModel.refreshJobs() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen_list"),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. TOP HEADER AREA
            item {
                HomeTopHeader(
                    userName = userProfile.fullName.ifBlank { "Asif" },
                    unreadCount = unreadNotifCount,
                    onNotificationClick = onNavigateToNotifications,
                    onSearchClick = { onNavigateToFindJobsWithQuery("") }
                )
            }

            // 2. AI JOB MATCH SECTION (SWIPEABLE CARD STACK)
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryBlue.copy(alpha = 0.15f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI JOB MATCH",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 1.sp
                            )
                        }

                        // Gesture hint
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.TouchApp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Swipe or Tap",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (aiMatchDeck.isNotEmpty()) {
                        val currentJob = aiMatchDeck[currentCardIndex.coerceIn(0, aiMatchDeck.size - 1)]
                        AiJobSwipeCard(
                            job = currentJob,
                            isSaved = savedJobIds.contains(currentJob.id),
                            onSwipeRightSave = { jobViewModel.onSwipeRightSave(currentJob) },
                            onSwipeLeftSkip = { jobViewModel.onSwipeLeftSkip(currentJob) },
                            onSwipeUpOpenDetails = { onNavigateToJobDetails(currentJob.id) },
                            onLongPressQuickActions = { jobViewModel.onCardLongPress(currentJob) },
                            onOpenDetails = { onNavigateToJobDetails(currentJob.id) }
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("No more cards in stack", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Pull down to refresh new AI recommendations", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // 3. APPLICATION SUMMARY
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    ApplicationSummarySection(
                        summary = applicationSummary,
                        onStatusClick = onNavigateToApplicationsWithStatus
                    )
                }
            }

            // 4. HIGH MATCH JOBS
            item {
                SectionHeader(title = "HIGH MATCH JOBS", count = allJobs.count { it.isHighMatch })
            }
            item {
                val highMatchList = allJobs.filter { it.isHighMatch }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(highMatchList) { job ->
                        JobCompactCard(
                            job = job,
                            isSaved = savedJobIds.contains(job.id),
                            onJobClick = { onNavigateToJobDetails(job.id) },
                            onToggleSave = { jobViewModel.toggleSaveJob(job.id) }
                        )
                    }
                }
            }

            // 5. NEW JOBS
            item {
                Spacer(modifier = Modifier.height(14.dp))
                SectionHeader(title = "NEW JOBS", count = allJobs.count { it.isFeatured })
            }
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    allJobs.filter { it.isFeatured }.take(2).forEach { job ->
                        JobItemCard(
                            job = job,
                            isSaved = savedJobIds.contains(job.id),
                            onJobClick = { onNavigateToJobDetails(job.id) },
                            onToggleSave = { jobViewModel.toggleSaveJob(job.id) }
                        )
                    }
                }
            }

            // 6. JOBS NEAR YOU
            item {
                Spacer(modifier = Modifier.height(14.dp))
                SectionHeader(title = "JOBS NEAR YOU", count = allJobs.count { it.isNearUser })
            }
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    allJobs.filter { it.isNearUser }.forEach { job ->
                        JobItemCard(
                            job = job,
                            isSaved = savedJobIds.contains(job.id),
                            onJobClick = { onNavigateToJobDetails(job.id) },
                            onToggleSave = { jobViewModel.toggleSaveJob(job.id) }
                        )
                    }
                }
            }

            // 7. RECENTLY ADDED
            item {
                Spacer(modifier = Modifier.height(14.dp))
                SectionHeader(title = "RECENTLY ADDED", count = allJobs.count { it.isRecentlyAdded })
            }
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    allJobs.filter { it.isRecentlyAdded }.forEach { job ->
                        JobItemCard(
                            job = job,
                            isSaved = savedJobIds.contains(job.id),
                            onJobClick = { onNavigateToJobDetails(job.id) },
                            onToggleSave = { jobViewModel.toggleSaveJob(job.id) }
                        )
                    }
                }
            }
        }
    }

    // Quick Actions Bottom Sheet
    selectedQuickActionJob?.let { job ->
        QuickActionsBottomSheet(
            job = job,
            isSaved = savedJobIds.contains(job.id),
            onDismiss = { jobViewModel.closeQuickActions() },
            onApplyNow = {
                jobViewModel.applyToJob(it)
            },
            onToggleSave = {
                jobViewModel.toggleSaveJob(it.id)
            },
            onShare = {
                Toast.makeText(context, "Job link copied for sharing", Toast.LENGTH_SHORT).show()
            },
            onHideJob = {
                jobViewModel.hideJob(it.id)
            },
            onReportJob = {
                jobViewModel.reportJob(it.id)
            }
        )
    }
}

@Composable
private fun HomeTopHeader(
    userName: String,
    unreadCount: Int,
    onNotificationClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Good Morning, $userName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Find your next opportunity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .testTag("home_notification_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge { Text(unreadCount.toString()) }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (unreadCount > 0) Icons.Filled.NotificationsActive else Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar (Read-only trigger leading to Find Jobs or AI search)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSearchClick() }
                .testTag("home_search_bar_trigger")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Search jobs, skills, or 'Dhaka 25k electrical'...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 1.sp
        )
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "$count available",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun JobCompactCard(
    job: Job,
    isSaved: Boolean,
    onJobClick: () -> Unit,
    onToggleSave: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onJobClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AccentCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${job.match.percentage}% Match",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = job.formattedSalary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentEmerald
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = job.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${job.company.name} • ${job.location}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
