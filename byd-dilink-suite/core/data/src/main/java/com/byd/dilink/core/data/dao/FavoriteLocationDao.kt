package com.byd.dilink.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.byd.dilink.core.data.entities.FavoriteLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: FavoriteLocation): Long

    @Update
    suspend fun update(location: FavoriteLocation)

    @Delete
    suspend fun delete(location: FavoriteLocation)

    @Query("SELECT * FROM favorite_locations ORDER BY name ASC")
    fun getAll(): Flow<List<FavoriteLocation>>

    @Query("SELECT * FROM favorite_locations WHERE id = :id")
    suspend fun getById(id: Long): FavoriteLocation?

    @Query("DELETE FROM favorite_locations WHERE id = :id")
    suspend fun deleteById(id: Long)
}
