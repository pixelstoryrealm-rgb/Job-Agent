package com.example.ui.screens.jobs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.source.JobSortOption
import com.example.ui.components.EmptyStateView
import com.example.ui.components.JobItemCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.JobViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FindJobsScreen(
    jobViewModel: JobViewModel,
    onNavigateToJobDetails: (String) -> Unit
) {
    val searchQuery by jobViewModel.searchQuery.collectAsState()
    val parsedAiQuery by jobViewModel.parsedAiQuery.collectAsState()
    val filteredJobs by jobViewModel.filteredJobs.collectAsState()
    val activeFilters by jobViewModel.activeFilters.collectAsState()
    val savedJobIds by jobViewModel.savedJobIds.collectAsState()
    val isLoading by jobViewModel.isLoading.collectAsState()

    var showSortSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Find Opportunities",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { jobViewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            "Try 'Dhaka te 20k+ salary er junior electrical job'",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { jobViewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("find_jobs_search_input")
                )

                // AI Parsed Query Chips Bar
                AnimatedVisibility(visible = parsedAiQuery != null) {
                    parsedAiQuery?.let { ai ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryBlue.copy(alpha = 0.08f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AI Interpreted Filter (Confidence ${(ai.confidence * 100).toInt()}%)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    ai.extractedLocation?.let {
                                        AiBadge("Location: $it")
                                    }
                                    ai.extractedMinSalary?.let {
                                        AiBadge("Min: ৳${it / 1000}k")
                                    }
                                    ai.extractedJobTitle?.let {
                                        AiBadge("Title: $it")
                                    }
                                    ai.extractedEmploymentType?.let {
                                        AiBadge("Type: $it")
                                    }
                                    ai.extractedSector?.let {
                                        AiBadge("Sector: $it")
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter & Sort Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        item {
                            FilterChip(
                                selected = activeFilters.sector == "Government",
                                onClick = {
                                    jobViewModel.updateSectorFilter(
                                        if (activeFilters.sector == "Government") null else "Government"
                                    )
                                },
                                label = { Text("Government") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = activeFilters.sector == "Private",
                                onClick = {
                                    jobViewModel.updateSectorFilter(
                                        if (activeFilters.sector == "Private") null else "Private"
                                    )
                                },
                                label = { Text("Private") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = activeFilters.employmentType == "Remote",
                                onClick = {
                                    jobViewModel.updateEmploymentTypeFilter(
                                        if (activeFilters.employmentType == "Remote") null else "Remote"
                                    )
                                },
                                label = { Text("Remote") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = activeFilters.employmentType == "Full Time",
                                onClick = {
                                    jobViewModel.updateEmploymentTypeFilter(
                                        if (activeFilters.employmentType == "Full Time") null else "Full Time"
                                    )
                                },
                                label = { Text("Full Time") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = activeFilters.employmentType == "Internship",
                                onClick = {
                                    jobViewModel.updateEmploymentTypeFilter(
                                        if (activeFilters.employmentType == "Internship") null else "Internship"
                                    )
                                },
                                label = { Text("Internship") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Sort Button
                    Surface(
                        onClick = { showSortSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.testTag("sort_filter_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Sort,
                                contentDescription = "Sort",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = activeFilters.sortBy.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("find_jobs_results_list"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredJobs.size} positions available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (searchQuery.isNotBlank() || activeFilters.sector != null || activeFilters.employmentType != null) {
                        TextButton(onClick = { jobViewModel.clearFilters() }) {
                            Text("Clear Filters", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            if (filteredJobs.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No jobs found",
                        message = "Try changing your search terms or relaxing filters to discover more engineering roles.",
                        actionLabel = "Reset Filters",
                        onActionClick = { jobViewModel.clearFilters() }
                    )
                }
            } else {
                items(filteredJobs) { job ->
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

    // Sort Bottom Sheet
    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Sort Jobs By",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                JobSortOption.values().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                jobViewModel.updateSortOption(option)
                                showSortSheet = false
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = activeFilters.sortBy == option,
                            onClick = {
                                jobViewModel.updateSortOption(option)
                                showSortSheet = false
                            }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = option.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (activeFilters.sortBy == option) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = AccentCyan.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryBlue
        )
    }
}
