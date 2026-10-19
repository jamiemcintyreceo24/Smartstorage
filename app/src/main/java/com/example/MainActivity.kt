package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.PruneScoreBreakdownDialog
import com.example.ui.screens.AuditDashboardScreen
import com.example.ui.screens.QuarantineTrashScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ActionDeleteRed
import com.example.ui.theme.BlueOutlineClean
import com.example.ui.theme.BlueOutlineProminent
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextPrimaryOnWhite
import com.example.ui.theme.TextSecondaryOnWhite
import com.example.ui.viewmodel.StorageViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: StorageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SmartStorageApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SmartStorageApp(viewModel: StorageViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val filteredApps = viewModel.getFilteredApps(state)
    val filteredFiles = viewModel.getFilteredFiles(state)
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle snackbar feedback
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    // Hardware back handler: return to Dashboard tab if on Quarantine or Settings
    BackHandler(enabled = state.activeTab != 0) {
        viewModel.setActiveTab(0)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite),
        containerColor = PureWhite,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.4.dp, BlueOutlineProminent),
                color = PureWhite,
                shadowElevation = 8.dp
            ) {
                NavigationBar(
                    containerColor = PureWhite,
                    tonalElevation = 0.dp
                ) {
                    // Tab 0: Audit Dashboard
                    NavigationBarItem(
                        selected = state.activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        icon = {
                            Icon(
                                imageVector = if (state.activeTab == 0) Icons.Filled.Storage else Icons.Outlined.Storage,
                                contentDescription = "Audit Tab",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                "Audit",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (state.activeTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = BluePrimary,
                            indicatorColor = BluePrimary,
                            unselectedIconColor = TextSecondaryOnWhite,
                            unselectedTextColor = TextSecondaryOnWhite
                        ),
                        modifier = Modifier.testTag("tab_audit")
                    )

                    // Tab 1: Quarantine / Recycle Bin
                    NavigationBarItem(
                        selected = state.activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (state.quarantinedItems.isNotEmpty()) {
                                        Badge(
                                            containerColor = ActionDeleteRed,
                                            contentColor = Color.White
                                        ) {
                                            Text("${state.quarantinedItems.size}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (state.activeTab == 1) Icons.Filled.DeleteSweep else Icons.Outlined.DeleteSweep,
                                    contentDescription = "Quarantine Tab",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                "Quarantine",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (state.activeTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = BluePrimary,
                            indicatorColor = BluePrimary,
                            unselectedIconColor = TextSecondaryOnWhite,
                            unselectedTextColor = TextSecondaryOnWhite
                        ),
                        modifier = Modifier.testTag("tab_quarantine")
                    )

                    // Tab 2: Settings & Sandbox
                    NavigationBarItem(
                        selected = state.activeTab == 2,
                        onClick = { viewModel.setActiveTab(2) },
                        icon = {
                            Icon(
                                imageVector = if (state.activeTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings Tab",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                "Settings",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (state.activeTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = BluePrimary,
                            indicatorColor = BluePrimary,
                            unselectedIconColor = TextSecondaryOnWhite,
                            unselectedTextColor = TextSecondaryOnWhite
                        ),
                        modifier = Modifier.testTag("tab_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PureWhite)
                .padding(innerPadding)
        ) {
            when (state.activeTab) {
                0 -> AuditDashboardScreen(
                    state = state,
                    filteredApps = filteredApps,
                    filteredFiles = filteredFiles,
                    onRunAudit = { viewModel.runStorageAudit() },
                    onToggleSelect = { viewModel.toggleFileSelection(it) },
                    onSelectAll = { viewModel.selectAll(it) },
                    onSelectRecommendedDeletes = { viewModel.selectRecommendedDeletes() },
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onFilterChange = { viewModel.setFilter(it) },
                    onSortChange = { viewModel.setSort(it) },
                    onShowDetails = { viewModel.showFileDetails(it) },
                    onQuarantineSingle = { viewModel.quarantineSingle(it) },
                    onQuarantineSelected = { viewModel.quarantineSelected() },
                    onArchiveSelected = { viewModel.archiveSelected() },
                    onGenerateDemoData = { viewModel.generateDemoFiles() },
                    onUninstallApp = { viewModel.launchAppUninstall(context, it) },
                    onAppDetails = { viewModel.launchAppDetails(context, it) }
                )

                1 -> QuarantineTrashScreen(
                    quarantinedItems = state.quarantinedItems,
                    onRestoreItem = { viewModel.restoreQuarantined(it) },
                    onPurgeItem = { viewModel.purgeQuarantined(it) },
                    onPurgeAll = { viewModel.purgeAllQuarantined() }
                )

                2 -> SettingsScreen(
                    minSizeMb = state.minSizeMb,
                    onMinSizeChange = { viewModel.setMinSizeMb(it) },
                    isDemoDataPresent = state.isDemoDataPresent,
                    onGenerateDemoData = { viewModel.generateDemoFiles() },
                    onClearDemoData = { viewModel.clearDemoFiles() }
                )
            }
        }
    }

    // Formula Breakdown Dialog
    state.detailFile?.let { file ->
        PruneScoreBreakdownDialog(
            file = file,
            onDismiss = { viewModel.showFileDetails(null) },
            onQuarantine = {
                viewModel.quarantineSingle(file)
            }
        )
    }
}
