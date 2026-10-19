package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.QuarantinedItemEntity
import com.example.data.local.StorageDatabase
import com.example.data.model.AppUsageCandidate
import com.example.data.model.AppUsageLevel
import com.example.data.model.AuditedFile
import com.example.data.model.FileAction
import com.example.data.model.StorageAuditMetrics
import com.example.data.model.StorageFilterOption
import com.example.data.model.StorageSortOption
import com.example.domain.AppUsageAuditor
import com.example.domain.DemoStorageGenerator
import com.example.domain.StorageEvaluator
import com.example.domain.StorageExecutionManager
import com.example.domain.StorageScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

data class StorageUiState(
    val isLoading: Boolean = false,
    val statusMessage: String = "",
    val auditedFiles: List<AuditedFile> = emptyList(),
    val appCandidates: List<AppUsageCandidate> = emptyList(),
    val selectedFileIds: Set<String> = emptySet(),
    val metrics: StorageAuditMetrics = StorageAuditMetrics(),
    val filterOption: StorageFilterOption = StorageFilterOption.ALL,
    // Highlight apps with limited usage first by default
    val sortOption: StorageSortOption = StorageSortOption.LIMITED_USAGE_FIRST,
    val searchQuery: String = "",
    val minSizeMb: Int = 40,
    val detailFile: AuditedFile? = null,
    val activeTab: Int = 0, // 0 = Dashboard, 1 = Quarantine Trash, 2 = Sandbox / Settings
    val quarantinedItems: List<QuarantinedItemEntity> = emptyList(),
    val snackbarMessage: String? = null,
    val isDemoDataPresent: Boolean = false
)

class StorageViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(StorageUiState())
    val uiState: StateFlow<StorageUiState> = _uiState.asStateFlow()

    private val executionManager = StorageExecutionManager(application)
    private val dao = StorageDatabase.getInstance(application).storageDao()

    init {
        // Collect quarantined items from Room database
        viewModelScope.launch {
            dao.getAllQuarantined().collect { items ->
                _uiState.update { it.copy(quarantinedItems = items) }
            }
        }

        // Check if demo sandbox exists, if not generate it once so user gets immediate rich data
        viewModelScope.launch {
            val hasDemo = DemoStorageGenerator.hasDemoFiles(getApplication())
            if (!hasDemo) {
                withContext(Dispatchers.IO) {
                    DemoStorageGenerator.generateDemoFiles(getApplication())
                }
            }
            _uiState.update { it.copy(isDemoDataPresent = true) }
            runStorageAudit()
        }
    }

    fun setActiveTab(index: Int) {
        _uiState.update { it.copy(activeTab = index) }
    }

    fun setFilter(filter: StorageFilterOption) {
        _uiState.update { it.copy(filterOption = filter) }
    }

    fun setSort(sort: StorageSortOption) {
        _uiState.update { it.copy(sortOption = sort) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setMinSizeMb(mb: Int) {
        _uiState.update { it.copy(minSizeMb = mb) }
        runStorageAudit()
    }

    fun showFileDetails(file: AuditedFile?) {
        _uiState.update { it.copy(detailFile = file) }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun toggleFileSelection(fileId: String) {
        _uiState.update { state ->
            val newSelection = state.selectedFileIds.toMutableSet()
            if (newSelection.contains(fileId)) {
                newSelection.remove(fileId)
            } else {
                newSelection.add(fileId)
            }
            state.copy(selectedFileIds = newSelection)
        }
    }

    fun selectAll(select: Boolean) {
        _uiState.update { state ->
            val filtered = getFilteredFiles(state)
            val newSelection = if (select) filtered.map { it.id }.toSet() else emptySet()
            state.copy(selectedFileIds = newSelection)
        }
    }

    fun selectRecommendedDeletes() {
        _uiState.update { state ->
            val deleteIds = state.auditedFiles
                .filter { it.action == FileAction.Immediate_Delete }
                .map { it.id }
                .toSet()
            state.copy(selectedFileIds = deleteIds)
        }
    }

    fun runStorageAudit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Auditing apps and storage footprint...") }
            withContext(Dispatchers.IO) {
                try {
                    val app = getApplication<Application>()

                    // 1. Audit Installed Applications for Limited Usage
                    val apps = AppUsageAuditor.auditInstalledApps(app)

                    // 2. Scan Storage Directories for Heavy Storage Candidates
                    val targets = listOfNotNull(
                        DemoStorageGenerator.getDemoWorkspaceDir(app),
                        app.cacheDir,
                        app.filesDir,
                        app.getExternalFilesDir(null)
                    )

                    val minBytes = _uiState.value.minSizeMb.toLong() * 1024L * 1024L
                    val allCandidates = mutableListOf<com.example.data.model.FileCandidate>()

                    for (target in targets) {
                        val candidates = StorageScanner.scanDirectory(target, minSizeBytes = minBytes)
                        allCandidates.addAll(candidates)
                    }

                    val distinctCandidates = allCandidates.distinctBy { it.filePath }
                    val audited = StorageEvaluator.evaluateBatch(distinctCandidates)

                    val metrics = computeMetrics(audited, apps)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = "",
                            auditedFiles = audited,
                            appCandidates = apps,
                            metrics = metrics,
                            selectedFileIds = emptySet()
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = "",
                            snackbarMessage = "Audit failed: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    fun launchAppUninstall(context: Context, app: AppUsageCandidate) {
        try {
            val intent = AppUsageAuditor.createUninstallIntent(app.packageName).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to app details
            launchAppDetails(context, app)
        }
    }

    fun launchAppDetails(context: Context, app: AppUsageCandidate) {
        try {
            val intent = AppUsageAuditor.createAppDetailsIntent(app.packageName).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _uiState.update { it.copy(snackbarMessage = "Could not open settings: ${e.message}") }
        }
    }

    fun dismissAppCandidate(packageName: String) {
        _uiState.update { state ->
            val updated = state.appCandidates.filterNot { it.packageName == packageName }
            state.copy(
                appCandidates = updated,
                metrics = computeMetrics(state.auditedFiles, updated),
                snackbarMessage = "App removed from recommendation list."
            )
        }
    }

    fun quarantineSelected() {
        val selectedIds = _uiState.value.selectedFileIds
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Safely quarantining selected items...") }
            var count = 0
            withContext(Dispatchers.IO) {
                val toQuarantine = _uiState.value.auditedFiles.filter { it.id in selectedIds }
                for (file in toQuarantine) {
                    val res = executionManager.moveToQuarantine(file)
                    if (res.isSuccess) count++
                }
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    snackbarMessage = "Moved $count items to Quarantine (Recycle Bin). Can be restored anytime.",
                    selectedFileIds = emptySet()
                )
            }
            runStorageAudit()
        }
    }

    fun archiveSelected() {
        val selectedIds = _uiState.value.selectedFileIds
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Archiving selected items...") }
            var count = 0
            withContext(Dispatchers.IO) {
                val toArchive = _uiState.value.auditedFiles.filter { it.id in selectedIds }
                for (file in toArchive) {
                    val res = executionManager.moveToArchive(file)
                    if (res.isSuccess) count++
                }
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    snackbarMessage = "Moved $count items to Cold Archive folder.",
                    selectedFileIds = emptySet()
                )
            }
            runStorageAudit()
        }
    }

    fun quarantineSingle(file: AuditedFile) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                executionManager.moveToQuarantine(file)
            }
            _uiState.update {
                it.copy(snackbarMessage = "Moved '${file.fileName}' to Quarantine (Reversible).")
            }
            runStorageAudit()
        }
    }

    fun restoreQuarantined(item: QuarantinedItemEntity) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                executionManager.restoreQuarantinedItem(item)
            }
            if (res.isSuccess) {
                _uiState.update {
                    it.copy(snackbarMessage = "Restored '${item.fileName}' back to original directory.")
                }
                runStorageAudit()
            } else {
                _uiState.update {
                    it.copy(snackbarMessage = "Failed to restore: ${res.exceptionOrNull()?.message}")
                }
            }
        }
    }

    fun purgeQuarantined(item: QuarantinedItemEntity) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                executionManager.permanentlyPurgeItem(item)
            }
            if (res.isSuccess) {
                _uiState.update {
                    it.copy(snackbarMessage = "Permanently purged '${item.fileName}'.")
                }
            }
        }
    }

    fun purgeAllQuarantined() {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                executionManager.purgeAllQuarantined()
            }
            if (res.isSuccess) {
                _uiState.update {
                    it.copy(snackbarMessage = "Permanently emptied Quarantine Bin (${res.getOrNull()} items).")
                }
            }
        }
    }

    fun generateDemoFiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Generating realistic demo storage footprint...") }
            val count = withContext(Dispatchers.IO) {
                DemoStorageGenerator.generateDemoFiles(getApplication())
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isDemoDataPresent = true,
                    snackbarMessage = "Generated $count test files across Downloads, Cache, Media, and Docs."
                )
            }
            runStorageAudit()
        }
    }

    fun clearDemoFiles() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                DemoStorageGenerator.clearDemoFiles(getApplication())
            }
            _uiState.update {
                it.copy(
                    isDemoDataPresent = false,
                    snackbarMessage = "Cleared sandbox test files."
                )
            }
            runStorageAudit()
        }
    }

    private fun computeMetrics(files: List<AuditedFile>, apps: List<AppUsageCandidate>): StorageAuditMetrics {
        var totalBytes = 0L
        var reclaimableBytes = 0L
        var archivableBytes = 0L
        var keepBytes = 0L
        var delCount = 0
        var archCount = 0
        var keepCount = 0

        for (f in files) {
            totalBytes += f.sizeBytes
            when (f.action) {
                FileAction.Immediate_Delete -> {
                    reclaimableBytes += f.sizeBytes
                    delCount++
                }
                FileAction.Archive_Cold_Storage -> {
                    archivableBytes += f.sizeBytes
                    archCount++
                }
                FileAction.Keep -> {
                    keepBytes += f.sizeBytes
                    keepCount++
                }
            }
        }

        // Include limited/dormant app footprints
        val limitedApps = apps.filter { it.usageLevel == AppUsageLevel.DORMANT || it.usageLevel == AppUsageLevel.LIMITED }
        val limitedAppsBytes = limitedApps.sumOf { it.sizeBytes }

        fun toGb(b: Long): Double = String.format(Locale.US, "%.2f", b.toDouble() / (1024.0 * 1024.0 * 1024.0)).toDouble()

        return StorageAuditMetrics(
            totalScannedBytes = totalBytes + apps.sumOf { it.sizeBytes },
            totalScannedGb = toGb(totalBytes + apps.sumOf { it.sizeBytes }),
            reclaimableBytes = reclaimableBytes + limitedAppsBytes,
            reclaimableGb = toGb(reclaimableBytes + limitedAppsBytes),
            archivableBytes = archivableBytes,
            archivableGb = toGb(archivableBytes),
            keepBytes = keepBytes,
            keepGb = toGb(keepBytes),
            candidateCount = files.size + apps.size,
            deleteCount = delCount + limitedApps.size,
            archiveCount = archCount,
            keepCount = keepCount,
            limitedUsageAppsCount = limitedApps.size,
            limitedUsageAppsGb = toGb(limitedAppsBytes)
        )
    }

    fun getFilteredApps(state: StorageUiState): List<AppUsageCandidate> {
        val query = state.searchQuery.trim().lowercase(Locale.ROOT)
        var list = state.appCandidates

        if (query.isNotEmpty()) {
            list = list.filter {
                it.appName.lowercase(Locale.ROOT).contains(query) ||
                        it.packageName.lowercase(Locale.ROOT).contains(query) ||
                        it.category.lowercase(Locale.ROOT).contains(query)
            }
        }

        if (state.filterOption == StorageFilterOption.LIMITED_APPS_ONLY) {
            list = list.filter { it.usageLevel == AppUsageLevel.DORMANT || it.usageLevel == AppUsageLevel.LIMITED }
        }

        return when (state.sortOption) {
            StorageSortOption.LIMITED_USAGE_FIRST -> list.sortedWith(
                compareByDescending<AppUsageCandidate> {
                    when (it.usageLevel) {
                        AppUsageLevel.DORMANT -> 4
                        AppUsageLevel.LIMITED -> 3
                        AppUsageLevel.OCCASIONAL -> 2
                        AppUsageLevel.ACTIVE -> 1
                    }
                }.thenByDescending { it.pruneScore }
            )
            StorageSortOption.PRUNE_SCORE_DESC -> list.sortedByDescending { it.pruneScore }
            StorageSortOption.SIZE_DESC -> list.sortedByDescending { it.sizeBytes }
            StorageSortOption.IDLE_DAYS_DESC -> list.sortedByDescending { it.daysIdle }
            StorageSortOption.UTILITY_ASC -> list.sortedBy { it.utilityScore }
            StorageSortOption.NAME_ASC -> list.sortedBy { it.appName.lowercase(Locale.ROOT) }
        }
    }

    fun getFilteredFiles(state: StorageUiState): List<AuditedFile> {
        if (state.filterOption == StorageFilterOption.LIMITED_APPS_ONLY) {
            return emptyList() // Only show apps when filter is limited apps only
        }

        val query = state.searchQuery.trim().lowercase(Locale.ROOT)
        var list = state.auditedFiles

        if (query.isNotEmpty()) {
            list = list.filter {
                it.fileName.lowercase(Locale.ROOT).contains(query) ||
                        it.extension.lowercase(Locale.ROOT).contains(query) ||
                        it.parentDir.lowercase(Locale.ROOT).contains(query)
            }
        }

        list = when (state.filterOption) {
            StorageFilterOption.ALL -> list
            StorageFilterOption.LIMITED_APPS_ONLY -> emptyList()
            StorageFilterOption.DELETE_ONLY -> list.filter { it.action == FileAction.Immediate_Delete }
            StorageFilterOption.ARCHIVE_ONLY -> list.filter { it.action == FileAction.Archive_Cold_Storage }
            StorageFilterOption.KEEP_ONLY -> list.filter { it.action == FileAction.Keep }
            StorageFilterOption.HEAVY_ONLY -> list.filter { it.sizeBytes >= 500L * 1024L * 1024L }
        }

        return when (state.sortOption) {
            StorageSortOption.LIMITED_USAGE_FIRST -> list.sortedByDescending { it.pruneScore }
            StorageSortOption.PRUNE_SCORE_DESC -> list.sortedByDescending { it.pruneScore }
            StorageSortOption.SIZE_DESC -> list.sortedByDescending { it.sizeBytes }
            StorageSortOption.IDLE_DAYS_DESC -> list.sortedByDescending { it.daysIdle }
            StorageSortOption.UTILITY_ASC -> list.sortedBy { it.utilityScore }
            StorageSortOption.NAME_ASC -> list.sortedBy { it.fileName.lowercase(Locale.ROOT) }
        }
    }
}
