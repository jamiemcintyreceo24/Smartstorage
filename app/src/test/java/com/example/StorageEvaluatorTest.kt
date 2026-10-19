package com.example

import com.example.data.model.FileAction
import com.example.data.model.FileCandidate
import com.example.domain.StorageEvaluator
import com.example.domain.StorageScanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.ln

class StorageEvaluatorTest {

    @Test
    fun testPruneScoreFormula() {
        val sizeGb = 1.45
        val daysIdle = 180
        val utility = 1

        val expected = (sizeGb * ln(1.0 + daysIdle)) / utility
        val actual = StorageEvaluator.calculatePruneScore(sizeGb, daysIdle, utility)

        assertEquals(expected, actual, 0.01)
    }

    @Test
    fun testPruneScorePriorityOrdering() {
        // High burden disposable file: 2.0 GB, 120 days idle, utility 1
        val disposableScore = StorageEvaluator.calculatePruneScore(2.0, 120, 1)

        // Low burden vital document: 0.05 GB, 30 days idle, utility 9
        val vitalScore = StorageEvaluator.calculatePruneScore(0.05, 30, 9)

        // Disposable file MUST have a significantly higher prune score than vital document
        assertTrue("Disposable installer score ($disposableScore) should exceed vital doc score ($vitalScore)", disposableScore > vitalScore)
    }

    @Test
    fun testVitalDocumentRule() {
        val candidate = FileCandidate(
            fileName = "tax_returns_fy2023.pdf",
            filePath = "/storage/emulated/0/Documents/tax_returns_fy2023.pdf",
            sizeBytes = 25L * 1024L * 1024L,
            sizeGb = 0.024,
            daysIdle = 150,
            lastModified = "2024-01-01",
            extension = ".pdf",
            parentDir = "Documents"
        )

        val audited = StorageEvaluator.evaluateCandidate(candidate)
        assertEquals(FileAction.Keep, audited.action)
        assertEquals(9, audited.utilityScore)
    }

    @Test
    fun testDisposableBloatRule() {
        val candidate = FileCandidate(
            fileName = "ubuntu-24.04-desktop.iso",
            filePath = "/storage/emulated/0/Downloads/ubuntu-24.04-desktop.iso",
            sizeBytes = 1500L * 1024L * 1024L,
            sizeGb = 1.465,
            daysIdle = 90,
            lastModified = "2024-03-01",
            extension = ".iso",
            parentDir = "Downloads"
        )

        val audited = StorageEvaluator.evaluateCandidate(candidate)
        assertEquals(FileAction.Immediate_Delete, audited.action)
        assertTrue(audited.utilityScore <= 3)
    }

    @Test
    fun testOSBoundaryProtection() {
        // System paths must NOT be safe
        assertFalse(StorageScanner.isPathSafe(File("/system/bin/sh")))
        assertFalse(StorageScanner.isPathSafe(File("/vendor/lib/libtest.so")))
        assertFalse(StorageScanner.isPathSafe(File("/apex/com.android.runtime")))

        // Protected system file names & extensions must NOT be safe
        assertFalse(StorageScanner.isPathSafe(File("/data/local/tmp/classes.dex")))
        assertFalse(StorageScanner.isPathSafe(File("/storage/emulated/0/download/driver.sys")))
        assertFalse(StorageScanner.isPathSafe(File("/storage/emulated/0/download/AndroidManifest.xml")))

        // User files should be safe
        assertTrue(StorageScanner.isPathSafe(File("/storage/emulated/0/Download/archive.zip")))
        assertTrue(StorageScanner.isPathSafe(File("/storage/emulated/0/Movies/clip.mp4")))
    }

    @Test
    fun testLimitedUsageAppPrioritization() {
        val dormantScore = StorageEvaluator.calculatePruneScore(1.93, 148, 1)
        val activeScore = StorageEvaluator.calculatePruneScore(0.20, 2, 9)

        // Dormant app must have a much higher prune score than an active app
        assertTrue("Dormant app ($dormantScore) should rank far higher than active app ($activeScore)", dormantScore > activeScore)
    }
}
