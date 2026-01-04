package com.patrolshield.data.local.dao

import androidx.room.*
import com.patrolshield.data.local.entities.ShiftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: ShiftEntity)

    @Query("SELECT * FROM active_shift LIMIT 1")
    fun getActiveShift(): Flow<ShiftEntity?>

    @Query("DELETE FROM active_shift")
    suspend fun clearShift()
}
