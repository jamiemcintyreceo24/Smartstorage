package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AuditedFile
import com.example.data.model.FileAction
import com.example.ui.theme.ActionArchiveAmber
import com.example.ui.theme.ActionArchiveBorder
import com.example.ui.theme.ActionArchiveContainer
import com.example.ui.theme.ActionDeleteBorder
import com.example.ui.theme.ActionDeleteContainer
import com.example.ui.theme.ActionDeleteRed
import com.example.ui.theme.ActionKeepBorder
import com.example.ui.theme.ActionKeepContainer
import com.example.ui.theme.ActionKeepGreen
import com.example.ui.theme.BlueOutlineClean
import com.example.ui.theme.BlueOutlineProminent
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedOnWhite
import com.example.ui.theme.TextPrimaryOnWhite
import com.example.ui.theme.TextSecondaryOnWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FileCandidateCard(
    file: AuditedFile,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onShowDetails: () -> Unit,
    onQuarantineSingle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) BlueOutlineProminent else BlueOutlineClean
    val cardBg = if (isSelected) Color(0xFFEFF6FF) else PureWhite

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(
                BorderStroke(if (isSelected) 2.dp else 1.4.dp, borderColor),
                RoundedCornerShape(14.dp)
            )
            .clickable { onToggleSelect() }
            .padding(14.dp)
            .testTag("file_card_${file.id}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header: Checkbox + Icon + File Name + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = BluePrimary,
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("checkbox_${file.id}")
                )

                Spacer(modifier = Modifier.width(8.dp))

                // File Type Icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(getFileCategoryBg(file.extension))
                        .border(1.2.dp, BlueOutlineClean, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getFileCategoryIcon(file.extension),
                        contentDescription = null,
                        tint = getFileCategoryTint(file.extension),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // File Name & Folder
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = TextPrimaryOnWhite
                    )
                    Text(
                        text = "folder: ${file.parentDir} • modified: ${file.lastModified}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedOnWhite
                    )
                }

                // Info / Formula Breakdown Button
                IconButton(
                    onClick = onShowDetails,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "View Prune Score Formula",
                        tint = BluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Quick Quarantine Button
                IconButton(
                    onClick = onQuarantineSingle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Quarantine to Recycle Bin",
                        tint = ActionDeleteRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Chips: Recommendation, Prune Score, Size, Idle days, Utility
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Action Badge
                ActionChip(action = file.action)

                // Prune Score
                StatusPill(
                    label = "Score: ${file.pruneScore}",
                    bg = Color(0xFFEFF6FF),
                    border = BlueOutlineClean,
                    text = BluePrimary,
                    isMono = true
                )

                // Size
                StatusPill(
                    label = "${file.sizeGb} GB",
                    bg = Color(0xFFF8FAFC),
                    border = Color(0xFFCBD5E1),
                    text = TextPrimaryOnWhite
                )

                // Idle Days
                StatusPill(
                    label = "${file.daysIdle}d idle",
                    bg = Color(0xFFF8FAFC),
                    border = Color(0xFFCBD5E1),
                    text = TextSecondaryOnWhite
                )

                // Utility rating
                StatusPill(
                    label = "Utility ${file.utilityScore}/10",
                    bg = if (file.utilityScore <= 3) ActionDeleteContainer else if (file.utilityScore <= 6) ActionArchiveContainer else ActionKeepContainer,
                    border = if (file.utilityScore <= 3) ActionDeleteBorder else if (file.utilityScore <= 6) ActionArchiveBorder else ActionKeepBorder,
                    text = if (file.utilityScore <= 3) ActionDeleteRed else if (file.utilityScore <= 6) ActionArchiveAmber else ActionKeepGreen
                )
            }

            // AI / Heuristic Rationale
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
                        text = "AI Rationale: ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                    Text(
                        text = file.rationale,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = TextPrimaryOnWhite
                    )
                }
            }
        }
    }
}

@Composable
fun ActionChip(action: FileAction) {
    val (bg, border, text) = when (action) {
        FileAction.Immediate_Delete -> Triple(ActionDeleteContainer, ActionDeleteBorder, ActionDeleteRed)
        FileAction.Archive_Cold_Storage -> Triple(ActionArchiveContainer, ActionArchiveBorder, ActionArchiveAmber)
        FileAction.Keep -> Triple(ActionKeepContainer, ActionKeepBorder, ActionKeepGreen)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.2.dp, border, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = action.badge,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = text
        )
    }
}

@Composable
fun StatusPill(
    label: String,
    bg: Color,
    border: Color,
    text: Color,
    isMono: Boolean = false
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = if (isMono) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = text
        )
    }
}

private fun getFileCategoryIcon(extension: String): ImageVector {
    return when (extension.lowercase()) {
        ".mov", ".mp4", ".mkv", ".avi", ".raw" -> Icons.Default.Movie
        ".tar", ".gz", ".zip", ".iso", ".7z" -> Icons.Default.FolderZip
        ".pdf", ".doc", ".docx", ".xls", ".xlsx" -> Icons.Default.Description
        ".apk", ".dmg", ".exe", ".msi" -> Icons.Default.Save
        else -> Icons.Default.Description
    }
}

private fun getFileCategoryBg(extension: String): Color {
    return when (extension.lowercase()) {
        ".mov", ".mp4", ".mkv" -> Color(0xFFFFFBEB)
        ".tar", ".gz", ".zip", ".iso" -> Color(0xFFFEF2F2)
        ".pdf", ".doc", ".docx" -> Color(0xFFECFDF5)
        else -> Color(0xFFEFF6FF)
    }
}

private fun getFileCategoryTint(extension: String): Color {
    return when (extension.lowercase()) {
        ".mov", ".mp4", ".mkv" -> ActionArchiveAmber
        ".tar", ".gz", ".zip", ".iso" -> ActionDeleteRed
        ".pdf", ".doc", ".docx" -> ActionKeepGreen
        else -> BluePrimary
    }
}
