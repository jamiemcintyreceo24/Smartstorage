package com.example.domain

import android.content.Context
import java.io.File
import java.io.RandomAccessFile

object DemoStorageGenerator {

    private data class DemoFileSpec(
        val subDir: String,
        val fileName: String,
        val sizeMb: Int,
        val idleDays: Int
    )

    private val DEMO_SPECS = listOf(
        // High burden disposable bloat (High prune score)
        DemoFileSpec("Downloads", "ubuntu-24.04-desktop-amd64.iso", 1450, 185),
        DemoFileSpec("Downloads", "android-commandlinetools-linux.tar.gz", 520, 120),
        DemoFileSpec("Cache", "gradle-dependency-cache-bundle.tmp", 340, 95),
        DemoFileSpec("Downloads", "staging-debug-v2.4.1.apk", 210, 65),
        DemoFileSpec("Cache", "uncompressed_symbols_dump.log", 180, 80),

        // Heavy media projects (Cold archive candidates)
        DemoFileSpec("Media", "raw_drone_footage_4k_reel.mov", 920, 75),
        DemoFileSpec("Media", "podcast_master_uncompressed.wav", 410, 55),
        DemoFileSpec("Media", "product_3d_render_sequence.mp4", 290, 48),

        // Vital preserved documents (Must receive Keep)
        DemoFileSpec("Documents", "tax_returns_fy2023_final_signed.pdf", 65, 140),
        DemoFileSpec("Documents", "commercial_lease_agreement.pdf", 45, 90),
        DemoFileSpec("Documents", "corporate_financial_ledger_2024.xlsx", 55, 30),
        DemoFileSpec("Documents", "production_key_vault_backup.kdbx", 40, 45)
    )

    fun getDemoWorkspaceDir(context: Context): File {
        val dir = File(context.filesDir, "auditor_sandbox")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Creates fast sparse dummy files using RandomAccessFile.
     * Uses setLength() so gigabyte files take 0ms and virtually no physical flash wear,
     * while reporting exact file length to standard stat and file.length() APIs!
     */
    fun generateDemoFiles(context: Context): Int {
        val root = getDemoWorkspaceDir(context)
        val now = System.currentTimeMillis()
        var createdCount = 0

        for (spec in DEMO_SPECS) {
            val folder = File(root, spec.subDir)
            if (!folder.exists()) folder.mkdirs()

            val file = File(folder, spec.fileName)
            try {
                RandomAccessFile(file, "rw").use { raf ->
                    val targetSizeBytes = spec.sizeMb.toLong() * 1024L * 1024L
                    raf.setLength(targetSizeBytes)
                    // Write header byte to ensure physical footprint exists
                    raf.seek(0)
                    raf.write("SMART_STORAGE_AUDITOR_DEMO_HEADER".toByteArray())
                }
                val pastTimestamp = now - (spec.idleDays.toLong() * 24L * 60L * 60L * 1000L)
                file.setLastModified(pastTimestamp)
                createdCount++
            } catch (_: Exception) {
            }
        }
        return createdCount
    }

    fun hasDemoFiles(context: Context): Boolean {
        val root = getDemoWorkspaceDir(context)
        return (root.listFiles()?.isNotEmpty() == true)
    }

    fun clearDemoFiles(context: Context): Boolean {
        val root = getDemoWorkspaceDir(context)
        return root.deleteRecursively()
    }
}
