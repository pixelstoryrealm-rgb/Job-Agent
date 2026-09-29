package com.example.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirebaseManager
import com.example.data.model.CvProcessingStatus
import com.example.data.model.Education
import com.example.data.model.EmploymentType
import com.example.data.model.Experience
import com.example.data.model.Skill
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val profile by profileViewModel.userProfile.collectAsState()
    val isAiLoading by profileViewModel.isAiLoading.collectAsState()
    val aiAnalysisResult by profileViewModel.aiAnalysisResult.collectAsState()
    val aiAdviceTitle by profileViewModel.aiAdviceDialogTitle.collectAsState()
    val aiAdviceItems by profileViewModel.aiAdviceDialogItems.collectAsState()

    var showEditPersonalDialog by remember { mutableStateOf(false) }
    var showAddEduDialog by remember { mutableStateOf(false) }
    var showAddSkillDialog by remember { mutableStateOf(false) }
    var showAddExpDialog by remember { mutableStateOf(false) }
    var showFirebaseGuideDialog by remember { mutableStateOf(false) }

    // CV Document Picker
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "Uploaded_Resume.pdf"
            profileViewModel.uploadOrReplaceCV(
                fileName = fileName,
                fileSize = "620 KB",
                fileType = if (fileName.endsWith(".docx", ignoreCase = true)) "DOCX" else "PDF",
                uriString = uri.toString()
            )
        }
    }

    LaunchedEffect(Unit) {
        profileViewModel.message.collect {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("profile_screen_scroll"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Profile Completion Tracker Header
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Profile Completion",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Real-time calculation from saved portfolio data",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${profile.completionPercentage}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { profile.completionPercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PrimaryBlue,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // 2. Firebase Cloud Sync Status Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (profileViewModel.isFirebaseActive) AccentEmerald.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFirebaseGuideDialog = true }
                        .testTag("firebase_status_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (profileViewModel.isFirebaseActive) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = if (profileViewModel.isFirebaseActive) AccentEmerald else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (profileViewModel.isFirebaseActive) "Firebase Cloud Sync Active" else "Local Persistent Mode",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (profileViewModel.isFirebaseActive) AccentEmerald else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (profileViewModel.isFirebaseActive) "Firestore, Auth & Storage connected" else "Awaiting google-services.json for cloud sync • Tap for guide",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3. AI PROFILE ASSISTANT (Section 21)
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_profile_assistant_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = PrimaryBlue.copy(alpha = 0.15f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "AI Profile Assistant",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Server-side intelligence pipeline",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isAiLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = PrimaryBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { profileViewModel.triggerAnalyzeProfile() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("ai_action_analyze_profile")
                            ) {
                                Text("⚡ Analyze My Profile")
                            }
                            FilledTonalButton(
                                onClick = { profileViewModel.triggerBestJobTypes() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("ai_action_best_job_types")
                            ) {
                                Text("🎯 Find Best Job Types")
                            }
                            FilledTonalButton(
                                onClick = { profileViewModel.triggerMissingSkills() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("ai_action_missing_skills")
                            ) {
                                Text("🔍 What Skills Am I Missing?")
                            }
                            FilledTonalButton(
                                onClick = { profileViewModel.triggerImproveSearch() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("ai_action_improve_search")
                            ) {
                                Text("💡 Improve My Job Search")
                            }
                            FilledTonalButton(
                                onClick = { profileViewModel.triggerExplainCv() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("ai_action_explain_cv")
                            ) {
                                Text("📄 Explain My CV")
                            }
                        }

                        // Display Full AI Analysis Result if present
                        aiAnalysisResult?.let { result ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = result.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = AccentEmerald.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Readiness: ${result.readinessScore}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentEmerald,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(result.summary, style = MaterialTheme.typography.bodySmall)

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Key Strengths:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    result.strengths.forEach { s ->
                                        Text("✓ $s", style = MaterialTheme.typography.bodySmall, color = AccentEmerald)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Recommended Next Steps:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    result.suggestedProfileEnhancements.forEach { tip ->
                                        Text("• $tip", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { profileViewModel.dismissAiAnalysisResult() }) {
                                            Text("Dismiss")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Personal Information Card
            item {
                ProfileSectionCard(
                    title = "Personal Information",
                    icon = Icons.Filled.Person,
                    onEditClick = { showEditPersonalDialog = true }
                ) {
                    Text(
                        text = profile.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = profile.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📧 ${profile.email}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "📱 ${profile.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "📍 ${profile.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (profile.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = profile.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 5. CV Management (PDF, DOC/DOCX) & Status Pipeline
            item {
                ProfileSectionCard(
                    title = "Curriculum Vitae (CV)",
                    icon = Icons.Filled.Description,
                    onEditClick = {
                        docPickerLauncher.launch(arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    },
                    editLabel = if (profile.cv != null) "Replace" else "Upload"
                ) {
                    if (profile.cv != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = PrimaryBlue.copy(alpha = 0.15f),
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = profile.cv!!.fileType,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryBlue,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = profile.cv!!.fileName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "Uploaded ${profile.cv!!.uploadDate} • ${profile.cv!!.fileSizeFormatted}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(onClick = { profileViewModel.deleteCV() }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete CV", tint = AccentRose)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Status Badge & Pipeline Info
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (profile.cv!!.status) {
                                            CvProcessingStatus.PROCESSED -> AccentEmerald.copy(alpha = 0.15f)
                                            CvProcessingStatus.PROCESSING -> AccentAmber.copy(alpha = 0.15f)
                                            CvProcessingStatus.UPLOADING -> PrimaryBlue.copy(alpha = 0.15f)
                                            CvProcessingStatus.FAILED -> AccentRose.copy(alpha = 0.15f)
                                            CvProcessingStatus.UPLOADED -> AccentCyan.copy(alpha = 0.15f)
                                        }
                                    ) {
                                        Text(
                                            text = "Status: ${profile.cv!!.status.name}",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when (profile.cv!!.status) {
                                                CvProcessingStatus.PROCESSED -> AccentEmerald
                                                CvProcessingStatus.PROCESSING -> AccentAmber
                                                CvProcessingStatus.UPLOADING -> PrimaryBlue
                                                CvProcessingStatus.FAILED -> AccentRose
                                                CvProcessingStatus.UPLOADED -> AccentCyan
                                            }
                                        )
                                    }

                                    if (profile.cv!!.status == CvProcessingStatus.PROCESSING) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    }
                                }

                                if (profile.cv!!.extractedSkills.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "CV Extracted Skills: ${profile.cv!!.extractedSkills.joinToString(", ")}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PrimaryBlue
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                docPickerLauncher.launch(arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload CV (PDF, DOC, DOCX)")
                        }
                    }
                }
            }

            // 6. Skills Card
            item {
                ProfileSectionCard(
                    title = "Skills",
                    icon = Icons.Filled.Work,
                    onEditClick = { showAddSkillDialog = true },
                    editLabel = "+ Add Skill"
                ) {
                    if (profile.skills.isEmpty()) {
                        Text("No skills added yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            profile.skills.forEach { skill ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = skill.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Remove skill",
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { profileViewModel.removeSkill(skill.id) },
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Education Card
            item {
                ProfileSectionCard(
                    title = "Education",
                    icon = Icons.Filled.School,
                    onEditClick = { showAddEduDialog = true },
                    editLabel = "+ Add"
                ) {
                    if (profile.educations.isEmpty()) {
                        Text("No education details added", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        profile.educations.forEach { edu ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(edu.degree, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("${edu.institution} • ${edu.passingYear}", style = MaterialTheme.typography.bodyMedium, color = PrimaryBlue)
                                Text("Grade: ${edu.grade}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // 8. Experience Card
            item {
                ProfileSectionCard(
                    title = "Experience",
                    icon = Icons.Filled.Business,
                    onEditClick = { showAddExpDialog = true },
                    editLabel = "+ Add"
                ) {
                    if (profile.experiences.isEmpty()) {
                        Text("No prior experience listed", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        profile.experiences.forEach { exp ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(exp.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("${exp.company} • ${exp.startDate} - ${exp.endDate}", style = MaterialTheme.typography.bodyMedium, color = PrimaryBlue)
                                if (exp.description.isNotBlank()) {
                                    Text(exp.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // 9. Job Preferences
            item {
                ProfileSectionCard(
                    title = "Job Preferences",
                    icon = Icons.Filled.LocationOn,
                    onEditClick = {
                        Toast.makeText(context, "Preferences can be customized in Find Jobs filter", Toast.LENGTH_SHORT).show()
                    },
                    editLabel = "Edit"
                ) {
                    Text(
                        text = "Preferred Locations: ${profile.preference.preferredLocations.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Expected Salary: ৳${profile.preference.expectedSalaryMin} – ৳${profile.preference.expectedSalaryMax}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentEmerald
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Preferred Shift: ${profile.preference.preferredShift}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Preferred Type: ${profile.preference.preferredJobTypes.joinToString { it.label }}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // 10. Account Settings & Logout
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Account Settings", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                authViewModel.logout()
                                onLogoutClick()
                            },
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                contentColor = AccentRose
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign Out")
                        }
                    }
                }
            }
        }
    }

    // AI Advice Dialog
    aiAdviceTitle?.let { title ->
        AlertDialog(
            onDismissRequest = { profileViewModel.dismissAiAdviceDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    aiAdviceItems.forEach { item ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text("• ", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            Text(item, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { profileViewModel.dismissAiAdviceDialog() }) {
                    Text("Got It")
                }
            }
        )
    }

    // Firebase Setup Guide Dialog
    if (showFirebaseGuideDialog) {
        AlertDialog(
            onDismissRequest = { showFirebaseGuideDialog = false },
            title = { Text("Firebase Cloud Configuration") },
            text = {
                Text(
                    text = FirebaseManager.SETUP_GUIDE,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(onClick = { showFirebaseGuideDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Edit Personal Info Dialog
    if (showEditPersonalDialog) {
        var editName by remember { mutableStateOf(profile.fullName) }
        var editTitle by remember { mutableStateOf(profile.title) }
        var editPhone by remember { mutableStateOf(profile.phone) }
        var editLocation by remember { mutableStateOf(profile.location) }
        var editBio by remember { mutableStateOf(profile.bio) }

        AlertDialog(
            onDismissRequest = { showEditPersonalDialog = false },
            title = { Text("Edit Personal Information") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Full Name") })
                    OutlinedTextField(value = editTitle, onValueChange = { editTitle = it }, label = { Text("Professional Title") })
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Phone") })
                    OutlinedTextField(value = editLocation, onValueChange = { editLocation = it }, label = { Text("Location") })
                    OutlinedTextField(value = editBio, onValueChange = { editBio = it }, label = { Text("Bio") }, maxLines = 3)
                }
            },
            confirmButton = {
                Button(onClick = {
                    profileViewModel.updatePersonalDetails(editName, editTitle, editPhone, editLocation, editBio)
                    showEditPersonalDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPersonalDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Skill Dialog
    if (showAddSkillDialog) {
        var newSkill by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddSkillDialog = false },
            title = { Text("Add Skill") },
            text = {
                OutlinedTextField(
                    value = newSkill,
                    onValueChange = { newSkill = it },
                    label = { Text("Skill name (e.g. PLC, AutoCAD)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newSkill.isNotBlank()) {
                        profileViewModel.addSkill(newSkill.trim(), "Intermediate")
                        showAddSkillDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSkillDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Education Dialog
    if (showAddEduDialog) {
        var degree by remember { mutableStateOf("") }
        var institute by remember { mutableStateOf("") }
        var year by remember { mutableStateOf("") }
        var grade by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddEduDialog = false },
            title = { Text("Add Education") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = degree, onValueChange = { degree = it }, label = { Text("Degree / Certificate") })
                    OutlinedTextField(value = institute, onValueChange = { institute = it }, label = { Text("Institution") })
                    OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Passing Year") })
                    OutlinedTextField(value = grade, onValueChange = { grade = it }, label = { Text("Grade / CGPA") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (degree.isNotBlank() && institute.isNotBlank()) {
                        profileViewModel.addEducation(degree, institute, "Engineering", year, grade)
                        showAddEduDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEduDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Experience Dialog
    if (showAddExpDialog) {
        var title by remember { mutableStateOf("") }
        var company by remember { mutableStateOf("") }
        var start by remember { mutableStateOf("") }
        var end by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddExpDialog = false },
            title = { Text("Add Experience") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Job Title") })
                    OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company Name") })
                    OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("Start Date (e.g. Jan 2024)") })
                    OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("End Date or Present") })
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Responsibilities") }, maxLines = 3)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank() && company.isNotBlank()) {
                        profileViewModel.addExperience(title, company, "Dhaka", start, end, end.equals("Present", ignoreCase = true), desc)
                        showAddExpDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    icon: ImageVector,
    onEditClick: () -> Unit,
    editLabel: String = "Edit",
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(onClick = onEditClick) {
                    Text(editLabel, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
