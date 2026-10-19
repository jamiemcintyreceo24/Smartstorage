package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageDao {
    @Query("SELECT * FROM quarantined_items ORDER BY timestamp DESC")
    fun getAllQuarantined(): Flow<List<QuarantinedItemEntity>>

    @Query("SELECT * FROM quarantined_items WHERE id = :id LIMIT 1")
    suspend fun getQuarantinedById(id: String): QuarantinedItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuarantined(item: QuarantinedItemEntity)

    @Query("DELETE FROM quarantined_items WHERE id = :id")
    suspend fun deleteQuarantined(id: String)

    @Query("DELETE FROM quarantined_items")
    suspend fun clearAllQuarantined()

    @Query("SELECT COUNT(*) FROM quarantined_items")
    fun getQuarantinedCount(): Flow<Int>
}
