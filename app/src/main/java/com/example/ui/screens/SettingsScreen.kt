package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.BlueOutlineCard
import com.example.ui.theme.ActionDeleteRed
import com.example.ui.theme.BlueOutlineClean
import com.example.ui.theme.BlueOutlineProminent
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextPrimaryOnWhite
import com.example.ui.theme.TextSecondaryOnWhite

@Composable
fun SettingsScreen(
    minSizeMb: Int,
    onMinSizeChange: (Int) -> Unit,
    isDemoDataPresent: Boolean,
    onGenerateDemoData: () -> Unit,
    onClearDemoData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember(minSizeMb) { mutableFloatStateOf(minSizeMb.toFloat()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Scan Threshold
        item {
            BlueOutlineCard(
                modifier = Modifier.fillMaxWidth(),
                isProminent = true
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scan Minimum Size Threshold",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryOnWhite
                        )
                    }

                    Text(
                        text = "Files below this threshold are ignored by the scanner to focus on storage-heavy candidates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryOnWhite
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter: ${sliderValue.toInt()} MB",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                        Text(
                            text = if (sliderValue.toInt() >= 500) "Heavy Only" else "Standard",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryOnWhite
                        )
                    }

                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = { onMinSizeChange(sliderValue.toInt()) },
                        valueRange = 20f..1000f,
                        steps = 49,
                        colors = SliderDefaults.colors(
                            thumbColor = BluePrimary,
                            activeTrackColor = BluePrimary,
                            inactiveTrackColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.testTag("min_size_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("20 MB", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("500 MB", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("1000 MB (1 GB)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        }

        // Section 2: Demo Storage Footprint Generator
        item {
            BlueOutlineCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sandbox Storage Generator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryOnWhite
                        )
                    }

                    Text(
                        text = "Instantly creates realistic heavy files (ISOs, stale caches, APKs, 4K video renders, and tax documents) with authentic idle timestamps to test audit and triage logic.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryOnWhite
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onGenerateDemoData,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BluePrimary,
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, BlueOutlineProminent),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("generate_demo_button")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Generate 3.5 GB Demo", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onClearDemoData,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.2.dp, ActionDeleteRed.copy(alpha = 0.8f)),
                            modifier = Modifier
                                .weight(0.7f)
                                .testTag("clear_demo_button")
                        ) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = ActionDeleteRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear", color = ActionDeleteRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Section 3: Safe OS Boundary & Zero Data Risk Guarantees
        item {
            BlueOutlineCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Zero Risk OS Boundary Protection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryOnWhite
                        )
                    }

                    Text(
                        text = "The crawler strictly guarantees safety by enforcing hard boundary blocks:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryOnWhite
                    )

                    SafetyPoint(title = "Blacklisted Partitions", desc = "/system, /vendor, /apex, /proc, /sys, /data/system")
                    SafetyPoint(title = "Protected Extensions", desc = ".dex, .so, .sys, .dll, .rom, .efi")
                    SafetyPoint(title = "Protected Filenames", desc = "AndroidManifest.xml, classes.dex, build.prop")
                    SafetyPoint(title = "Reversible Quarantine", desc = "Moved to isolated Recycle Bin with full restoration capability")
                }
            }
        }
    }
}

@Composable
private fun SafetyPoint(title: String, desc: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.2.dp, BlueOutlineClean.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = BluePrimary
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryOnWhite
            )
        }
    }
}
