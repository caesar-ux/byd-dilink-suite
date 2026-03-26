package com.byd.dilink.extras.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.byd.dilink.extras.core.data.entities.TasbeehSession
import kotlinx.coroutines.flow.Flow

@Dao
interface TasbeehDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: TasbeehSession): Long

    @Update
    suspend fun update(session: TasbeehSession)

    @Query("SELECT * FROM tasbeeh_sessions WHERE date = :dateTimestamp")
    fun getByDate(dateTimestamp: Long): Flow<List<TasbeehSession>>

    @Query("SELECT COALESCE(SUM(count), 0) FROM tasbeeh_sessions WHERE date = :dateTimestamp")
    fun getTotalToday(dateTimestamp: Long): Flow<Int>

    @Query("SELECT * FROM tasbeeh_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<TasbeehSession>>
}
