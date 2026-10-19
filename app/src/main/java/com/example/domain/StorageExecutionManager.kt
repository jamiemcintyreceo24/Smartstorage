package com.example.domain

import android.content.Context
import com.example.data.local.QuarantinedItemEntity
import com.example.data.local.StorageDatabase
import com.example.data.model.AuditedFile
import java.io.File
import java.util.UUID

class StorageExecutionManager(private val context: Context) {

    private val db = StorageDatabase.getInstance(context)
    private val dao = db.storageDao()

    val quarantineDir: File
        get() = File(context.filesDir, "quarantine").apply { if (!exists()) mkdirs() }

    val archiveDir: File
        get() = File(context.filesDir, "storage_archive").apply { if (!exists()) mkdirs() }

    /**
     * Reversible Move to Quarantine (Trash)
     * Preserves original path in Room DB so it can be restored anytime.
     */
    suspend fun moveToQuarantine(file: AuditedFile): Result<QuarantinedItemEntity> {
        return try {
            val sourceFile = File(file.filePath)
            if (!sourceFile.exists()) {
                return Result.failure(IllegalStateException("Source file no longer exists: ${file.filePath}"))
            }

            val quarantineId = UUID.randomUUID().toString()
            val targetFile = File(quarantineDir, "${quarantineId}_${file.fileName}")

            val moved = sourceFile.renameTo(targetFile)
            if (!moved) {
                // Try copy + delete fallback if rename across mount fails
                sourceFile.copyTo(targetFile, overwrite = true)
                sourceFile.delete()
            }

            val entity = QuarantinedItemEntity(
                id = quarantineId,
                originalPath = file.filePath,
                quarantinePath = targetFile.absolutePath,
                fileName = file.fileName,
                sizeBytes = file.sizeBytes,
                sizeGb = file.sizeGb,
                daysIdle = file.daysIdle,
                utilityScore = file.utilityScore,
                rationale = file.rationale,
                timestamp = System.currentTimeMillis()
            )

            dao.insertQuarantined(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Restore quarantined file back to its original path.
     */
    suspend fun restoreQuarantinedItem(item: QuarantinedItemEntity): Result<Unit> {
        return try {
            val quFile = File(item.quarantinePath)
            if (!quFile.exists()) {
                dao.deleteQuarantined(item.id)
                return Result.failure(IllegalStateException("Quarantined file missing on disk"))
            }

            val targetFile = File(item.originalPath)
            val parent = targetFile.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }

            val restored = quFile.renameTo(targetFile)
            if (!restored) {
                quFile.copyTo(targetFile, overwrite = true)
                quFile.delete()
            }

            dao.deleteQuarantined(item.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Permanently purge item from Quarantine
     */
    suspend fun permanentlyPurgeItem(item: QuarantinedItemEntity): Result<Unit> {
        return try {
            val quFile = File(item.quarantinePath)
            if (quFile.exists()) {
                quFile.delete()
            }
            dao.deleteQuarantined(item.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Permanently purge all quarantined items
     */
    suspend fun purgeAllQuarantined(): Result<Int> {
        return try {
            var purged = 0
            val files = quarantineDir.listFiles() ?: emptyArray()
            for (f in files) {
                if (f.delete()) purged++
            }
            dao.clearAllQuarantined()
            Result.success(purged)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Move to cold archive directory
     */
    fun moveToArchive(file: AuditedFile): Result<File> {
        return try {
            val sourceFile = File(file.filePath)
            if (!sourceFile.exists()) {
                return Result.failure(IllegalStateException("Source file does not exist"))
            }

            val destFile = File(archiveDir, file.fileName)
            val moved = sourceFile.renameTo(destFile)
            if (!moved) {
                sourceFile.copyTo(destFile, overwrite = true)
                sourceFile.delete()
            }
            Result.success(destFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
