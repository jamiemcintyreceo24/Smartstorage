package com.example.data.model

enum class FileAction(val label: String, val badge: String) {
    Immediate_Delete("Delete", "🔴 DELETE"),
    Archive_Cold_Storage("Archive", "🟡 ARCHIVE"),
    Keep("Keep", "🟢 KEEP");

    companion object {
        fun fromString(value: String): FileAction {
            return when (value.uppercase()) {
                "IMMEDIATE_DELETE", "DELETE" -> Immediate_Delete
                "ARCHIVE_COLD_STORAGE", "ARCHIVE" -> Archive_Cold_Storage
                else -> Keep
            }
        }
    }
}

enum class AppUsageLevel(val label: String, val badge: String) {
    DORMANT("Dormant (0 usage)", "💤 DORMANT (90d+)"),
    LIMITED("Limited usage", "⚡ LIMITED USAGE"),
    OCCASIONAL("Occasional", "⏳ OCCASIONAL"),
    ACTIVE("Frequent", "✨ ACTIVE")
}

enum class AppAction(val label: String) {
    UNINSTALL_RECOMMENDED("Uninstall"),
    OFFLOAD_CACHE("Clear Cache"),
    KEEP_APP("Keep")
}

data class AppUsageCandidate(
    val packageName: String,
    val appName: String,
    val category: String,
    val sizeBytes: Long,
    val sizeGb: Double,
    val daysIdle: Int,
    val lastUsedFormatted: String,
    val usageLevel: AppUsageLevel,
    val utilityScore: Int, // 1 to 10
    val pruneScore: Double,
    val action: AppAction,
    val rationale: String,
    val isSystemApp: Boolean = false,
    val isSelected: Boolean = false
)

data class FileCandidate(
    val fileName: String,
    val filePath: String,
    val sizeBytes: Long,
    val sizeGb: Double,
    val daysIdle: Int,
    val lastModified: String,
    val extension: String,
    val parentDir: String
)

data class AuditedFile(
    val id: String,
    val fileName: String,
    val filePath: String,
    val sizeBytes: Long,
    val sizeGb: Double,
    val daysIdle: Int,
    val lastModified: String,
    val extension: String,
    val parentDir: String,
    val utilityScore: Int, // 1 to 10
    val action: FileAction,
    val rationale: String,
    val pruneScore: Double, // (size_gb * ln(1 + days_idle)) / utility_score
    val isSelected: Boolean = false,
    val isQuarantined: Boolean = false,
    val isArchived: Boolean = false,
    // Tag for files originating from or associated with dormant/limited apps
    val isLimitedUsageApp: Boolean = false
)

data class StorageAuditMetrics(
    val totalScannedBytes: Long = 0L,
    val totalScannedGb: Double = 0.0,
    val reclaimableBytes: Long = 0L,
    val reclaimableGb: Double = 0.0,
    val archivableBytes: Long = 0L,
    val archivableGb: Double = 0.0,
    val keepBytes: Long = 0L,
    val keepGb: Double = 0.0,
    val candidateCount: Int = 0,
    val deleteCount: Int = 0,
    val archiveCount: Int = 0,
    val keepCount: Int = 0,
    val limitedUsageAppsCount: Int = 0,
    val limitedUsageAppsGb: Double = 0.0
)

enum class StorageSortOption(val title: String) {
    LIMITED_USAGE_FIRST("Limited Usage Apps First (Default)"),
    PRUNE_SCORE_DESC("Prune Score (Highest Burden)"),
    SIZE_DESC("Largest Footprint First"),
    IDLE_DAYS_DESC("Most Idle Days First"),
    UTILITY_ASC("Lowest Utility First"),
    NAME_ASC("Name (A-Z)")
}

enum class StorageFilterOption(val title: String) {
    ALL("All Candidates"),
    LIMITED_APPS_ONLY("⚡ Limited Usage Apps"),
    DELETE_ONLY("Recommended Delete"),
    ARCHIVE_ONLY("Recommended Archive"),
    KEEP_ONLY("Keep / Protected"),
    HEAVY_ONLY("Heavy (>500 MB)")
}
