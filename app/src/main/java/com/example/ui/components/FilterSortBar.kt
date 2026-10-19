package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.StorageFilterOption
import com.example.data.model.StorageSortOption
import com.example.ui.theme.ActionArchiveAmber
import com.example.ui.theme.ActionDeleteRed
import com.example.ui.theme.ActionKeepGreen
import com.example.ui.theme.BlueOutlineClean
import com.example.ui.theme.BlueOutlineProminent
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextPrimaryOnWhite
import com.example.ui.theme.TextSecondaryOnWhite

@Composable
fun FilterSortBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    currentFilter: StorageFilterOption,
    onFilterChange: (StorageFilterOption) -> Unit,
    currentSort: StorageSortOption,
    onSortChange: (StorageSortOption) -> Unit,
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: (Boolean) -> Unit,
    onSelectRecommendedDeletes: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PureWhite)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search & Sort row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_text_field"),
                placeholder = {
                    Text(
                        "Search file name, extension or folder...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = BluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BlueOutlineProminent,
                    unfocusedBorderColor = BlueOutlineClean,
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite,
                    focusedTextColor = TextPrimaryOnWhite,
                    unfocusedTextColor = TextPrimaryOnWhite
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Sort Dropdown button
            Box {
                OutlinedButton(
                    onClick = { sortMenuExpanded = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.4.dp, BlueOutlineClean),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("sort_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort Options",
                        tint = BluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                    modifier = Modifier.background(PureWhite)
                ) {
                    StorageSortOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (option == currentSort) BluePrimary else TextPrimaryOnWhite,
                                    fontWeight = if (option == currentSort) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSortChange(option)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Horizontal filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StorageFilterChip(
                label = "All Items",
                isSelected = currentFilter == StorageFilterOption.ALL,
                onClick = { onFilterChange(StorageFilterOption.ALL) }
            )
            StorageFilterChip(
                label = "⚡ Limited Apps",
                isSelected = currentFilter == StorageFilterOption.LIMITED_APPS_ONLY,
                onClick = { onFilterChange(StorageFilterOption.LIMITED_APPS_ONLY) },
                activeColor = ActionDeleteRed
            )
            StorageFilterChip(
                label = "🔴 Delete Only",
                isSelected = currentFilter == StorageFilterOption.DELETE_ONLY,
                onClick = { onFilterChange(StorageFilterOption.DELETE_ONLY) },
                activeColor = ActionDeleteRed
            )
            StorageFilterChip(
                label = "🟡 Archive Only",
                isSelected = currentFilter == StorageFilterOption.ARCHIVE_ONLY,
                onClick = { onFilterChange(StorageFilterOption.ARCHIVE_ONLY) },
                activeColor = ActionArchiveAmber
            )
            StorageFilterChip(
                label = "🟢 Keep Only",
                isSelected = currentFilter == StorageFilterOption.KEEP_ONLY,
                onClick = { onFilterChange(StorageFilterOption.KEEP_ONLY) },
                activeColor = ActionKeepGreen
            )
            StorageFilterChip(
                label = "💾 > 500 MB",
                isSelected = currentFilter == StorageFilterOption.HEAVY_ONLY,
                onClick = { onFilterChange(StorageFilterOption.HEAVY_ONLY) }
            )
        }

        // Selection batch helper bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedCount > 0) "$selectedCount of $totalCount selected" else "$totalCount candidates",
                style = MaterialTheme.typography.labelMedium,
                color = if (selectedCount > 0) BluePrimary else TextSecondaryOnWhite,
                fontWeight = if (selectedCount > 0) FontWeight.Bold else FontWeight.Normal
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onSelectRecommendedDeletes,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.2.dp, ActionDeleteRed.copy(alpha = 0.8f)),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        "Select Deletes",
                        style = MaterialTheme.typography.labelSmall,
                        color = ActionDeleteRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = { onSelectAll(selectedCount != totalCount) },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.2.dp, BlueOutlineClean),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        if (selectedCount == totalCount && totalCount > 0) "Deselect All" else "Select All",
                        style = MaterialTheme.typography.labelSmall,
                        color = BluePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    activeColor: Color = BluePrimary
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            1.2.dp,
            if (isSelected) activeColor else Color(0xFFCBD5E1)
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = PureWhite,
            selectedContainerColor = if (activeColor == BluePrimary) Color(0xFFEFF6FF) else activeColor.copy(alpha = 0.12f),
            labelColor = TextPrimaryOnWhite,
            selectedLabelColor = activeColor
        )
    )
}
