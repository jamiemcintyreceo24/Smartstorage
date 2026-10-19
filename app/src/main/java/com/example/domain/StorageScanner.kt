package com.example.domain

import com.example.data.model.FileCandidate
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object StorageScanner {

    private val SYSTEM_DIR_BLACKLIST = setOf(
        "/system",
        "/vendor",
        "/apex",
        "/proc",
        "/sys",
        "/dev",
        "/etc",
        "/sbin",
        "/odm",
        "/product",
        "/system_ext",
        "/data/system",
        "/data/data/com.android.providers.settings"
    )

    private val PROTECTED_EXTENSIONS = setOf(
        ".sys",
        ".dll",
        ".so",
        ".dex",
        ".kext",
        ".drv",
        ".efi",
        ".rom",
        ".msu",
        ".vxd"
    )

    private val PROTECTED_FILENAMES = setOf(
        "androidmanifest.xml",
        "classes.dex",
        "build.prop",
        "vold.fstab",
        "pagefile.sys",
        "hiberfil.sys",
        "swapfile.sys",
        "dumpstack.log",
        ".ds_store"
    )

    fun isPathSafe(file: File): Boolean {
        return try {
            val canonicalPath = file.canonicalPath.lowercase(Locale.ROOT)
            val fileName = file.name.lowercase(Locale.ROOT)
            val extension = if (fileName.contains('.')) {
                "." + fileName.substringAfterLast('.')
            } else ""

            if (fileName in PROTECTED_FILENAMES || extension in PROTECTED_EXTENSIONS) {
                return false
            }

            for (blacklisted in SYSTEM_DIR_BLACKLIST) {
                if (canonicalPath == blacklisted || canonicalPath.startsWith("$blacklisted/")) {
                    return false
                }
            }

            true
        } catch (_: Exception) {
            false
        }
    }

    fun scanDirectory(
        targetRoot: File,
        minSizeBytes: Long = 50L * 1024L * 1024L // Default 50MB for mobile testability
    ): List<FileCandidate> {
        val candidates = mutableListOf<FileCandidate>()
        if (!targetRoot.exists() || !isPathSafe(targetRoot)) {
            return candidates
        }

        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fun walkDir(dir: File) {
            val files = dir.listFiles() ?: return
            for (file in files) {
                if (!isPathSafe(file)) continue

                if (file.isDirectory) {
                    // Do not recurse into blacklisted or quarantine/archive directories
                    if (file.name == "quarantine" || file.name == "storage_archive") continue
                    walkDir(file)
                } else if (file.isFile) {
                    val length = file.length()
                    if (length >= minSizeBytes) {
                        val lastMod = file.lastModified()
                        val diffMillis = max(0L, now - lastMod)
                        val daysIdle = max(0, (diffMillis / (1000L * 60L * 60L * 24L)).toInt())
                        val sizeGb = length.toDouble() / (1024.0 * 1024.0 * 1024.0)
                        val ext = if (file.name.contains('.')) "." + file.extension.lowercase(Locale.ROOT) else ""

                        candidates.add(
                            FileCandidate(
                                fileName = file.name,
                                filePath = file.absolutePath,
                                sizeBytes = length,
                                sizeGb = String.format(Locale.US, "%.3f", sizeGb).toDouble(),
                                daysIdle = daysIdle,
                                lastModified = dateFormat.format(Date(lastMod)),
                                extension = ext,
                                parentDir = file.parentFile?.name ?: "root"
                            )
                        )
                    }
                }
            }
        }

        walkDir(targetRoot)
        return candidates
    }
}
