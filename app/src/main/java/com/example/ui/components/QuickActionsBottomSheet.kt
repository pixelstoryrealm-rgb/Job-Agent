package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Job
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionsBottomSheet(
    job: Job,
    isSaved: Boolean,
    onDismiss: () -> Unit,
    onApplyNow: (Job) -> Unit,
    onToggleSave: (Job) -> Unit,
    onShare: (Job) -> Unit,
    onHideJob: (Job) -> Unit,
    onReportJob: (Job) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .testTag("quick_actions_bottom_sheet")
        ) {
            // Header Info
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${job.title} • ${job.company.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Apply Now
            QuickActionItem(
                icon = Icons.AutoMirrored.Filled.Send,
                label = "Apply Now",
                subLabel = "Submit profile & CV directly",
                iconTint = PrimaryBlue,
                onClick = {
                    onDismiss()
                    onApplyNow(job)
                },
                testTag = "quick_action_apply"
            )

            // 2. Save / Unsave Job
            QuickActionItem(
                icon = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                label = if (isSaved) "Remove from Saved" else "Save Job",
                subLabel = if (isSaved) "Remove from bookmarked jobs" else "Add to your saved jobs list",
                iconTint = AccentEmerald,
                onClick = {
                    onDismiss()
                    onToggleSave(job)
                },
                testTag = "quick_action_save"
            )

            // 3. Share
            QuickActionItem(
                icon = Icons.Filled.Share,
                label = "Share Opportunity",
                subLabel = "Send job link to WhatsApp or colleagues",
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = {
                    onDismiss()
                    onShare(job)
                },
                testTag = "quick_action_share"
            )

            // 4. Hide Job
            QuickActionItem(
                icon = Icons.Filled.VisibilityOff,
                label = "Hide Job",
                subLabel = "Don't show this role again in recommendations",
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = {
                    onDismiss()
                    onHideJob(job)
                },
                testTag = "quick_action_hide"
            )

            // 5. Report Job
            QuickActionItem(
                icon = Icons.Filled.Flag,
                label = "Report Job",
                subLabel = "Inaccurate info, expired, or suspicious",
                iconTint = AccentRose,
                onClick = {
                    onDismiss()
                    onReportJob(job)
                },
                testTag = "quick_action_report"
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    subLabel: String,
    iconTint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
