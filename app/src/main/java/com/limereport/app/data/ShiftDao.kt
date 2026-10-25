package com.limereport.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts ORDER BY startMillis DESC")
    fun observeAll(): Flow<List<Shift>>

    @Query("SELECT * FROM shifts WHERE id = :id")
    suspend fun get(id: Long): Shift?

    @Upsert
    suspend fun upsert(shift: Shift)

    @Delete
    suspend fun delete(shift: Shift)
}
