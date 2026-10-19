package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AppAction
import com.example.data.model.AppUsageCandidate
import com.example.data.model.AppUsageLevel
import com.example.ui.theme.ActionArchiveAmber
import com.example.ui.theme.ActionDeleteBorder
import com.example.ui.theme.ActionDeleteContainer
import com.example.ui.theme.ActionDeleteRed
import com.example.ui.theme.BlueOutlineClean
import com.example.ui.theme.BlueOutlineProminent
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedOnWhite
import com.example.ui.theme.TextPrimaryOnWhite
import com.example.ui.theme.TextSecondaryOnWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppCandidateCard(
    app: AppUsageCandidate,
    onUninstall: () -> Unit,
    onAppDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDormantOrLimited = app.usageLevel == AppUsageLevel.DORMANT || app.usageLevel == AppUsageLevel.LIMITED
    val borderColor = if (isDormantOrLimited) BlueOutlineProminent else BlueOutlineClean

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PureWhite)
            .border(
                BorderStroke(if (isDormantOrLimited) 2.dp else 1.4.dp, borderColor),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
            .testTag("app_card_${app.packageName}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Android / App Icon + App Name + Category + Footprint
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Logo Icon Container
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDormantOrLimited) Color(0xFFFEF2F2) else Color(0xFFEFF6FF))
                        .border(
                            1.4.dp,
                            if (isDormantOrLimited) ActionDeleteBorder else BlueOutlineClean,
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = "App Icon",
                        tint = if (isDormantOrLimited) ActionDeleteRed else BluePrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = TextPrimaryOnWhite
                    )
                    Text(
                        text = "${app.category} • ${app.packageName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedOnWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Size Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEFF6FF))
                        .border(1.dp, BlueOutlineClean, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${app.sizeGb} GB",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                }
            }

            // Badges Flow: Usage Status, Idle Days, Utility Score, Prune Score
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Usage Level Badge
                AppUsageBadge(level = app.usageLevel)

                // Idle Days
                StatusPill(
                    label = "${app.daysIdle}d since launch",
                    bg = Color(0xFFF8FAFC),
                    border = Color(0xFFCBD5E1),
                    text = TextPrimaryOnWhite
                )

                // Prune Score
                StatusPill(
                    label = "Prune: ${app.pruneScore}",
                    bg = Color(0xFFEFF6FF),
                    border = BlueOutlineClean,
                    text = BluePrimary,
                    isMono = true
                )

                // Utility Rating
                StatusPill(
                    label = "Utility ${app.utilityScore}/10",
                    bg = if (app.utilityScore <= 2) ActionDeleteContainer else Color(0xFFFFFBEB),
                    border = if (app.utilityScore <= 2) ActionDeleteBorder else Color(0xFFFBBF24),
                    text = if (app.utilityScore <= 2) ActionDeleteRed else ActionArchiveAmber
                )
            }

            // AI Rationale
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Audit Verdict: ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                    Text(
                        text = app.rationale,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = TextPrimaryOnWhite
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAppDetails,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.2.dp, BlueOutlineClean),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("app_info_btn_${app.packageName}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "App Details",
                        tint = BluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "App Info / Clear",
                        style = MaterialTheme.typography.labelSmall,
                        color = BluePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onUninstall,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActionDeleteRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("app_uninstall_btn_${app.packageName}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Uninstall App",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Uninstall App",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun AppUsageBadge(level: AppUsageLevel) {
    val (bg, border, text) = when (level) {
        AppUsageLevel.DORMANT -> Triple(ActionDeleteContainer, ActionDeleteBorder, ActionDeleteRed)
        AppUsageLevel.LIMITED -> Triple(Color(0xFFFFFBEB), Color(0xFFFBBF24), ActionArchiveAmber)
        AppUsageLevel.OCCASIONAL -> Triple(Color(0xFFEFF6FF), BlueOutlineClean, BluePrimary)
        AppUsageLevel.ACTIVE -> Triple(Color(0xFFECFDF5), Color(0xFF34D399), Color(0xFF059669))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.2.dp, border, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = level.badge,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = text
        )
    }
}
