package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RecurringEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringDao {
    @Query("SELECT * FROM recurring_subscriptions")
    fun getAllRecurring(): Flow<List<RecurringEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(item: RecurringEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RecurringEntity>)

    @Update
    suspend fun updateRecurring(item: RecurringEntity)

    @Delete
    suspend fun deleteRecurring(item: RecurringEntity)

    @Query("DELETE FROM recurring_subscriptions")
    suspend fun clearAll()
}
