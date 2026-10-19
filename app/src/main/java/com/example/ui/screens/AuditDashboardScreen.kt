package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppUsageCandidate
import com.example.data.model.AppUsageLevel
import com.example.data.model.AuditedFile
import com.example.data.model.StorageFilterOption
import com.example.data.model.StorageSortOption
import com.example.ui.components.AppCandidateCard
import com.example.ui.components.BlueOutlineCard
import com.example.ui.components.FileCandidateCard
import com.example.ui.components.FilterSortBar
import com.example.ui.components.LimitedUsageAppsBanner
import com.example.ui.components.StorageMetricsHeader
import com.example.ui.theme.ActionArchiveAmber
import com.example.ui.theme.ActionDeleteRed
import com.example.ui.theme.BlueOutlineClean
import com.example.ui.theme.BlueOutlineProminent
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedOnWhite
import com.example.ui.theme.TextPrimaryOnWhite
import com.example.ui.theme.TextSecondaryOnWhite
import com.example.ui.viewmodel.StorageUiState

@Composable
fun AuditDashboardScreen(
    state: StorageUiState,
    filteredApps: List<AppUsageCandidate>,
    filteredFiles: List<AuditedFile>,
    onRunAudit: () -> Unit,
    onToggleSelect: (String) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onSelectRecommendedDeletes: () -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterChange: (StorageFilterOption) -> Unit,
    onSortChange: (StorageSortOption) -> Unit,
    onShowDetails: (AuditedFile) -> Unit,
    onQuarantineSingle: (AuditedFile) -> Unit,
    onQuarantineSelected: () -> Unit,
    onArchiveSelected: () -> Unit,
    onGenerateDemoData: () -> Unit,
    onUninstallApp: (AppUsageCandidate) -> Unit,
    onAppDetails: (AppUsageCandidate) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = filteredApps.size + filteredFiles.size
    val isLimitedFilterActive = state.filterOption == StorageFilterOption.LIMITED_APPS_ONLY
    val limitedAppsCount = state.metrics.limitedUsageAppsCount
    val limitedAppsGb = state.metrics.limitedUsageAppsGb

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(PureWhite),
            contentPadding = PaddingValues(bottom = if (state.selectedFileIds.isNotEmpty()) 100.dp else 24.dp)
        ) {
            // Header with metrics and storage allocation meter
            item {
                StorageMetricsHeader(
                    metrics = state.metrics,
                    isLoading = state.isLoading,
                    statusMessage = state.statusMessage,
                    onRunAudit = onRunAudit
                )
            }

            // Search, Filter chips, and Sort
            item {
                Spacer(modifier = Modifier.height(6.dp))
                FilterSortBar(
                    searchQuery = state.searchQuery,
                    onSearchChange = onSearchChange,
                    currentFilter = state.filterOption,
                    onFilterChange = onFilterChange,
                    currentSort = state.sortOption,
                    onSortChange = onSortChange,
                    selectedCount = state.selectedFileIds.size,
                    totalCount = totalCount,
                    onSelectAll = onSelectAll,
                    onSelectRecommendedDeletes = onSelectRecommendedDeletes
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 1. High-Priority Limited Usage Apps Banner
            if (limitedAppsCount > 0) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        LimitedUsageAppsBanner(
                            count = limitedAppsCount,
                            reclaimableGb = limitedAppsGb,
                            isFilterActive = isLimitedFilterActive,
                            onToggleFilter = {
                                if (isLimitedFilterActive) {
                                    onFilterChange(StorageFilterOption.ALL)
                                } else {
                                    onFilterChange(StorageFilterOption.LIMITED_APPS_ONLY)
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // 2. HIGHLIGHTED FIRST: Apps with Limited Usage
            if (filteredApps.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = ActionDeleteRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Limited Usage Apps (${filteredApps.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryOnWhite
                                )
                            }
                            Text(
                                text = "Ranked First",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ActionDeleteRed
                            )
                        }
                    }
                }

                items(filteredApps, key = { it.packageName }) { app ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PureWhite)
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                    ) {
                        AppCandidateCard(
                            app = app,
                            onUninstall = { onUninstallApp(app) },
                            onAppDetails = { onAppDetails(app) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // 3. Heavy Storage Files Section
            if (filteredFiles.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Storage Files & Archives (${filteredFiles.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryOnWhite
                                )
                            }
                            Text(
                                text = "By Prune Score",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMutedOnWhite
                            )
                        }
                    }
                }

                items(filteredFiles, key = { it.id }) { file ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PureWhite)
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                    ) {
                        FileCandidateCard(
                            file = file,
                            isSelected = state.selectedFileIds.contains(file.id),
                            onToggleSelect = { onToggleSelect(file.id) },
                            onShowDetails = { onShowDetails(file) },
                            onQuarantineSingle = { onQuarantineSingle(file) }
                        )
                    }
                }
            }

            // Empty state if nothing matches
            if (filteredApps.isEmpty() && filteredFiles.isEmpty()) {
                item {
                    EmptyAuditState(
                        isSearching = state.searchQuery.isNotEmpty() || state.filterOption != StorageFilterOption.ALL,
                        onGenerateDemo = onGenerateDemoData
                    )
                }
            }
        }

        // Floating Batch Action Bar for selected files
        AnimatedVisibility(
            visible = state.selectedFileIds.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val selectedFiles = state.auditedFiles.filter { it.id in state.selectedFileIds }
            val selectedGb = String.format("%.2f", selectedFiles.sumOf { it.sizeGb })

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PureWhite,
                border = BorderStroke(1.6.dp, BlueOutlineProminent),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${state.selectedFileIds.size} files selected",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                        Text(
                            text = "$selectedGb GB footprint",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryOnWhite
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onArchiveSelected,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.2.dp, ActionArchiveAmber),
                            modifier = Modifier.testTag("batch_archive_button")
                        ) {
                            Icon(imageVector = Icons.Default.Archive, contentDescription = null, tint = ActionArchiveAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Archive", color = ActionArchiveAmber, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onQuarantineSelected,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ActionDeleteRed,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("batch_quarantine_button")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Quarantine", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAuditState(
    isSearching: Boolean,
    onGenerateDemo: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PureWhite)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        BlueOutlineCard(
            modifier = Modifier.fillMaxWidth(),
            isProminent = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = if (isSearching) "No matching items found" else "Storage & Apps Optimized",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryOnWhite
                )

                Text(
                    text = if (isSearching)
                        "Try relaxing your search query or filter criteria."
                    else
                        "No limited usage apps or heavy storage files exceeding threshold were found.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryOnWhite,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                if (!isSearching) {
                    Button(
                        onClick = onGenerateDemo,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.2.dp, BlueOutlineProminent)
                    ) {
                        Text("Populate Test Storage Footprint")
                    }
                }
            }
        }
    }
}
