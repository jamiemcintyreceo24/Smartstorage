package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.StorageAuditMetrics
import com.example.ui.theme.ActionArchiveAmber
import com.example.ui.theme.ActionDeleteRed
import com.example.ui.theme.ActionKeepGreen
import com.example.ui.theme.BlueOutlineClean
import com.example.ui.theme.BlueOutlineProminent
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedOnWhite
import com.example.ui.theme.TextPrimaryOnWhite
import com.example.ui.theme.TextSecondaryOnWhite

@Composable
fun StorageMetricsHeader(
    metrics: StorageAuditMetrics,
    isLoading: Boolean,
    statusMessage: String,
    onRunAudit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PureWhite)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App Title & Audit Trigger Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEFF6FF))
                            .border(1.4.dp, BlueOutlineProminent, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "SmartStorage Logo",
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SmartStorage",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryOnWhite
                    )
                }
                Text(
                    text = "Safe OS Crawling • AI Utility Evaluator",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryOnWhite
                )
            }

            Button(
                onClick = onRunAudit,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BlueOutlineProminent),
                modifier = Modifier.testTag("run_audit_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scanning...", style = MaterialTheme.typography.labelLarge)
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Run Audit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Audit Now", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        if (statusMessage.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEFF6FF))
                    .border(1.2.dp, BlueOutlineClean, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.labelMedium,
                    color = BluePrimary
                )
            }
        }

        // 4 Key Metric Cards with Blue Outlines on Pure White
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricPillCard(
                modifier = Modifier.weight(1f),
                title = "Total Space",
                value = "${metrics.totalScannedGb} GB",
                subtitle = "${metrics.candidateCount} files",
                accentColor = BluePrimary,
                icon = Icons.Default.Storage
            )
            MetricPillCard(
                modifier = Modifier.weight(1f),
                title = "Reclaimable",
                value = "${metrics.reclaimableGb} GB",
                subtitle = if (metrics.limitedUsageAppsCount > 0) "${metrics.limitedUsageAppsCount} limited apps • to purge" else "${metrics.deleteCount} to purge",
                accentColor = ActionDeleteRed,
                icon = Icons.Default.DeleteSweep
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricPillCard(
                modifier = Modifier.weight(1f),
                title = "Cold Archive",
                value = "${metrics.archivableGb} GB",
                subtitle = "${metrics.archiveCount} packages",
                accentColor = ActionArchiveAmber,
                icon = Icons.Default.Inventory2
            )
            MetricPillCard(
                modifier = Modifier.weight(1f),
                title = "Protected",
                value = "${metrics.keepGb} GB",
                subtitle = "${metrics.keepCount} vital docs",
                accentColor = ActionKeepGreen,
                icon = Icons.Default.Security
            )
        }

        // Visual Storage Allocation Bar with Blue Outline
        StorageAllocationMeter(metrics = metrics)
    }
}

@Composable
private fun MetricPillCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    BlueOutlineCard(
        modifier = modifier,
        borderWidth = 1.3.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondaryOnWhite
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryOnWhite
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StorageAllocationMeter(metrics: StorageAuditMetrics) {
    val totalBytes = metrics.totalScannedBytes.coerceAtLeast(1L).toFloat()
    val deleteWeight = (metrics.reclaimableBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
    val archiveWeight = (metrics.archivableBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
    val keepWeight = (metrics.keepBytes.toFloat() / totalBytes).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PureWhite)
            .border(1.3.dp, BlueOutlineClean, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Triage Storage Footprint",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryOnWhite
            )
            Text(
                text = "Safe OS Boundaries Enforced",
                style = MaterialTheme.typography.labelSmall,
                color = BluePrimary,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress breakdown bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFFE2E8F0))
        ) {
            if (deleteWeight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(deleteWeight)
                        .height(10.dp)
                        .background(ActionDeleteRed)
                )
            }
            if (archiveWeight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(archiveWeight)
                        .height(10.dp)
                        .background(ActionArchiveAmber)
                )
            }
            if (keepWeight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(keepWeight)
                        .height(10.dp)
                        .background(ActionKeepGreen)
                )
            }
            if (deleteWeight == 0f && archiveWeight == 0f && keepWeight == 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(Color(0xFFCBD5E1))
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendItem(color = ActionDeleteRed, label = "Delete (${(deleteWeight * 100).toInt()}%)")
            LegendItem(color = ActionArchiveAmber, label = "Archive (${(archiveWeight * 100).toInt()}%)")
            LegendItem(color = ActionKeepGreen, label = "Keep (${(keepWeight * 100).toInt()}%)")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondaryOnWhite
        )
    }
}
