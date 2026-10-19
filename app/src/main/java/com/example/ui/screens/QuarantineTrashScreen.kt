package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.QuarantinedItemEntity
import com.example.ui.components.BlueOutlineCard
import com.example.ui.theme.ActionDeleteRed
import com.example.ui.theme.ActionKeepGreen
import com.example.ui.theme.BlueOutlineClean
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedOnWhite
import com.example.ui.theme.TextPrimaryOnWhite
import com.example.ui.theme.TextSecondaryOnWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuarantineTrashScreen(
    quarantinedItems: List<QuarantinedItemEntity>,
    onRestoreItem: (QuarantinedItemEntity) -> Unit,
    onPurgeItem: (QuarantinedItemEntity) -> Unit,
    onPurgeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalBytes = quarantinedItems.sumOf { it.sizeBytes }
    val totalGb = String.format(Locale.US, "%.2f", totalBytes.toDouble() / (1024.0 * 1024.0 * 1024.0))
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Banner Header
        item {
            BlueOutlineCard(
                modifier = Modifier.fillMaxWidth(),
                isProminent = true
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reversible Quarantine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryOnWhite
                            )
                        }

                        if (quarantinedItems.isNotEmpty()) {
                            OutlinedButton(
                                onClick = onPurgeAll,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.2.dp, ActionDeleteRed),
                                modifier = Modifier.testTag("empty_trash_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = null,
                                    tint = ActionDeleteRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Empty Trash", color = ActionDeleteRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Text(
                        text = "Files moved here are safely isolated from your workspace but preserved with full metadata. You can restore them to their original location at any time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryOnWhite
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "${quarantinedItems.size} Quarantined Files",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                        Text(
                            text = "$totalGb GB Total Stored",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ActionDeleteRed
                        )
                    }
                }
            }
        }

        // Empty State or List
        if (quarantinedItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Quarantine Bin is Empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimaryOnWhite
                        )
                        Text(
                            text = "When you flag files for cleanup, they will appear here safely before permanent removal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryOnWhite,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            items(quarantinedItems, key = { it.id }) { item ->
                QuarantinedItemCard(
                    item = item,
                    dateFormatted = dateFormat.format(Date(item.timestamp)),
                    onRestore = { onRestoreItem(item) },
                    onPurge = { onPurgeItem(item) }
                )
            }
        }
    }
}

@Composable
private fun QuarantinedItemCard(
    item: QuarantinedItemEntity,
    dateFormatted: String,
    onRestore: () -> Unit,
    onPurge: () -> Unit
) {
    BlueOutlineCard(
        modifier = Modifier.fillMaxWidth(),
        borderWidth = 1.2.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.fileName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextPrimaryOnWhite,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${item.sizeGb} GB",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
            }

            Text(
                text = "Original: ${item.originalPath}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryOnWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Quarantined on: $dateFormatted • Idle: ${item.daysIdle} days",
                style = MaterialTheme.typography.labelSmall,
                color = TextMutedOnWhite
            )

            // Buttons: Restore & Purge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPurge,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.2.dp, ActionDeleteRed.copy(alpha = 0.8f)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Purge",
                        tint = ActionDeleteRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Purge", color = ActionDeleteRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onRestore,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActionKeepGreen,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestoreFromTrash,
                        contentDescription = "Restore",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restore", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
