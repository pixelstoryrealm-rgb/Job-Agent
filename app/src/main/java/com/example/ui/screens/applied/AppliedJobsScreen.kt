package com.example.ui.screens.applied

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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Application
import com.example.data.model.ApplicationStatus
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.ApplicationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppliedJobsScreen(
    applicationViewModel: ApplicationViewModel,
    onNavigateToJobDetails: (String) -> Unit
) {
    val context = LocalContext.current
    val applications by applicationViewModel.applications.collectAsState()
    val selectedFilter by applicationViewModel.selectedStatusFilter.collectAsState()
    val summary by applicationViewModel.summary.collectAsState()

    LaunchedEffect(Unit) {
        applicationViewModel.toastEvent.collect {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Application Tracker",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${applications.size} active applications tracked",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status Tabs Filter Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        val total = summary.applicationStartedCount + summary.appliedCount + summary.shortlistedCount + summary.interviewCount + summary.selectedCount + summary.rejectedCount + summary.withdrawnCount
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { applicationViewModel.setStatusFilter(null) },
                            label = { Text("All ($total)") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == ApplicationStatus.APPLICATION_STARTED,
                            onClick = { applicationViewModel.setStatusFilter(ApplicationStatus.APPLICATION_STARTED) },
                            label = { Text("Started (${summary.applicationStartedCount})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == ApplicationStatus.APPLIED,
                            onClick = { applicationViewModel.setStatusFilter(ApplicationStatus.APPLIED) },
                            label = { Text("Applied (${summary.appliedCount})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == ApplicationStatus.SHORTLISTED,
                            onClick = { applicationViewModel.setStatusFilter(ApplicationStatus.SHORTLISTED) },
                            label = { Text("Shortlisted (${summary.shortlistedCount})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == ApplicationStatus.INTERVIEW,
                            onClick = { applicationViewModel.setStatusFilter(ApplicationStatus.INTERVIEW) },
                            label = { Text("Interview (${summary.interviewCount})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == ApplicationStatus.SELECTED,
                            onClick = { applicationViewModel.setStatusFilter(ApplicationStatus.SELECTED) },
                            label = { Text("Selected (${summary.selectedCount})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == ApplicationStatus.REJECTED,
                            onClick = { applicationViewModel.setStatusFilter(ApplicationStatus.REJECTED) },
                            label = { Text("Rejected (${summary.rejectedCount})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == ApplicationStatus.WITHDRAWN,
                            onClick = { applicationViewModel.setStatusFilter(ApplicationStatus.WITHDRAWN) },
                            label = { Text("Withdrawn (${summary.withdrawnCount})") }
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (applications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    title = "No Applications Found",
                    message = if (selectedFilter == null)
                        "You haven't applied to any positions yet. Find jobs that match your skillset on the Home screen!"
                    else
                        "No applications in status ${selectedFilter?.label}.",
                    icon = Icons.Filled.AssignmentTurnedIn
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("applied_jobs_list"),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(applications, key = { it.id }) { application ->
                    ApplicationCardItem(
                        application = application,
                        onUpdateStatus = { newStatus ->
                            applicationViewModel.updateApplicationStatus(application.id, newStatus)
                        },
                        onWithdraw = {
                            applicationViewModel.withdrawApplication(application.id)
                        },
                        onCardClick = {
                            onNavigateToJobDetails(application.jobId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ApplicationCardItem(
    application: Application,
    onUpdateStatus: (ApplicationStatus) -> Unit,
    onWithdraw: () -> Unit,
    onCardClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val statusColor = when (application.status) {
        ApplicationStatus.APPLICATION_STARTED -> AccentCyan
        ApplicationStatus.APPLIED -> PrimaryBlue
        ApplicationStatus.SHORTLISTED -> AccentCyan
        ApplicationStatus.INTERVIEW -> AccentAmber
        ApplicationStatus.SELECTED -> AccentEmerald
        ApplicationStatus.REJECTED -> AccentRose
        ApplicationStatus.WITHDRAWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("application_card_${application.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Job title and overflow status menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.jobTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = application.companyName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Options")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        Text(
                            "Update Status",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                        ApplicationStatus.values().forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.label) },
                                onClick = {
                                    menuExpanded = false
                                    onUpdateStatus(st)
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Withdraw Application", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onWithdraw()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${application.location} • ${application.salaryText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // Status Badge and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = application.status.label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Applied ${application.appliedDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
