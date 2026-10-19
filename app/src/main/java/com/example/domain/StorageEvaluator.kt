package com.example.domain

import com.example.data.model.AuditedFile
import com.example.data.model.FileAction
import com.example.data.model.FileCandidate
import java.util.Locale
import java.util.UUID
import kotlin.math.ln
import kotlin.math.max

object StorageEvaluator {

    /**
     * Mathematical Formula (Fixed per Auditor Spec):
     * Prune Recommendation Score increases with larger size, lower utility, and longer idle time.
     * Prune Score = (Size_GB * ln(1 + Days_Idle)) / Utility_Score
     */
    fun calculatePruneScore(sizeGb: Double, daysIdle: Int, utilityScore: Int): Double {
        val effectiveIdle = max(daysIdle, 1)
        val effectiveUtility = max(utilityScore, 1)
        val score = (sizeGb * ln(1.0 + effectiveIdle)) / effectiveUtility.toDouble()
        return String.format(Locale.US, "%.4f", score).toDouble()
    }

    /**
     * Evaluates a file candidate into an AuditedFile with utility rating (1-10),
     * actionable triage recommendation, concise rationale, and prune score.
     */
    fun evaluateCandidate(candidate: FileCandidate): AuditedFile {
        val nameLower = candidate.fileName.lowercase(Locale.ROOT)
        val extLower = candidate.extension.lowercase(Locale.ROOT)
        val parentLower = candidate.parentDir.lowercase(Locale.ROOT)

        val (utility, action, rationale) = when {
            // Rule 1: Vital Documents / Financials / Legal / Archives (Utility 9-10 -> Keep)
            isVitalDocument(nameLower, extLower) -> {
                Triple(
                    9,
                    FileAction.Keep,
                    "Vital document or financial record preserved regardless of age."
                )
            }

            // Rule 2: Disposable Bloat - Installers, disc images, caches, tarballs (Utility 1-3 -> Delete)
            isDisposableBloat(nameLower, extLower, parentLower) -> {
                if (candidate.daysIdle >= 14) {
                    val score = if (candidate.daysIdle > 60) 1 else 2
                    Triple(
                        score,
                        FileAction.Immediate_Delete,
                        "Disposable installer or cache idle for ${candidate.daysIdle} days."
                    )
                } else {
                    Triple(
                        3,
                        FileAction.Immediate_Delete,
                        "Temporary package or installation archive ready for cleanup."
                    )
                }
            }

            // Rule 3: Media Projects & Renders (Utility 4-6 -> Archive)
            isMediaProject(nameLower, extLower) -> {
                if (candidate.daysIdle >= 45) {
                    Triple(
                        4,
                        FileAction.Archive_Cold_Storage,
                        "Heavy media project idle > 45 days, optimal for cold archive."
                    )
                } else {
                    Triple(
                        6,
                        FileAction.Keep,
                        "Active media project accessed recently."
                    )
                }
            }

            // General archive candidates: old large archives or backups
            isArchiveArchive(nameLower, extLower) -> {
                if (candidate.daysIdle >= 30) {
                    Triple(
                        5,
                        FileAction.Archive_Cold_Storage,
                        "Stale archive suitable for secondary cold storage."
                    )
                } else {
                    Triple(
                        6,
                        FileAction.Keep,
                        "Recent backup package in active directory."
                    )
                }
            }

            // Default fallback
            else -> {
                if (candidate.daysIdle > 90) {
                    Triple(
                        4,
                        FileAction.Archive_Cold_Storage,
                        "Unused file idle for over 90 days."
                    )
                } else {
                    Triple(
                        7,
                        FileAction.Keep,
                        "Standard user data with regular access history."
                    )
                }
            }
        }

        val pruneScore = calculatePruneScore(candidate.sizeGb, candidate.daysIdle, utility)

        return AuditedFile(
            id = UUID.nameUUIDFromBytes(candidate.filePath.toByteArray()).toString(),
            fileName = candidate.fileName,
            filePath = candidate.filePath,
            sizeBytes = candidate.sizeBytes,
            sizeGb = candidate.sizeGb,
            daysIdle = candidate.daysIdle,
            lastModified = candidate.lastModified,
            extension = candidate.extension,
            parentDir = candidate.parentDir,
            utilityScore = utility,
            action = action,
            rationale = rationale,
            pruneScore = pruneScore
        )
    }

    fun evaluateBatch(candidates: List<FileCandidate>): List<AuditedFile> {
        return candidates.map { evaluateCandidate(it) }
            .sortedByDescending { it.pruneScore }
    }

    private fun isVitalDocument(name: String, ext: String): Boolean {
        val vitalExtensions = setOf(".kdbx", ".key", ".pfx", ".cer", ".sql", ".sqlite", ".db")
        if (ext in vitalExtensions) return true

        val documentExtensions = setOf(".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".odt")
        val vitalKeywords = setOf("tax", "w2", "1099", "statement", "invoice", "receipt", "contract", "legal", "passport", "lease", "deed", "insurance", "financial")

        if (ext in documentExtensions) {
            return vitalKeywords.any { name.contains(it) }
        }

        return vitalKeywords.any { name.contains(it) }
    }

    private fun isDisposableBloat(name: String, ext: String, parent: String): Boolean {
        val bloatExtensions = setOf(".iso", ".dmg", ".exe", ".msi", ".apk", ".tmp", ".log", ".cache", ".part", ".crdownload")
        if (ext in bloatExtensions) return true

        val bloatKeywords = setOf("installer", "setup", "update", "patch", "cache", "temp", "node_modules", "debug", "unconfirmed")
        if (bloatKeywords.any { name.contains(it) || parent.contains(it) }) return true

        val compressedArchives = setOf(".tar", ".gz", ".tgz", ".zip", ".rar", ".7z")
        if (ext in compressedArchives && (name.contains("sdk") || name.contains("dist") || name.contains("build") || name.contains("temp"))) {
            return true
        }

        return false
    }

    private fun isMediaProject(name: String, ext: String): Boolean {
        val mediaExtensions = setOf(".mov", ".mp4", ".mkv", ".avi", ".flv", ".raw", ".dng", ".cr2", ".nef", ".wav", ".flac", ".psd", ".prproj", ".aep")
        return ext in mediaExtensions
    }

    private fun isArchiveArchive(name: String, ext: String): Boolean {
        val archiveExts = setOf(".zip", ".tar", ".gz", ".7z", ".rar", ".bz2")
        return ext in archiveExts
    }
}
