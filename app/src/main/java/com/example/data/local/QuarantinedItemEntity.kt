package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quarantined_items")
data class QuarantinedItemEntity(
    @PrimaryKey val id: String,
    val originalPath: String,
    val quarantinePath: String,
    val fileName: String,
    val sizeBytes: Long,
    val sizeGb: Double,
    val daysIdle: Int,
    val utilityScore: Int,
    val rationale: String,
    val timestamp: Long
)
