package com.byd.dilink.core.data.repository

import com.byd.dilink.core.data.dao.FavoriteLocationDao
import com.byd.dilink.core.data.dao.ParkingDao
import com.byd.dilink.core.data.entities.FavoriteLocation
import com.byd.dilink.core.data.entities.ParkingRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParkingRepository @Inject constructor(
    private val parkingDao: ParkingDao,
    private val favoriteLocationDao: FavoriteLocationDao
) {
    val activeParking: Flow<ParkingRecord?> = parkingDao.getActive()
    val parkingHistory: Flow<List<ParkingRecord>> = parkingDao.getHistory()
    val favorites: Flow<List<FavoriteLocation>> = favoriteLocationDao.getAll()

    suspend fun saveParking(latitude: Double, longitude: Double, address: String? = null, notes: String? = null): Long {
        return parkingDao.insert(
            ParkingRecord(
                latitude = latitude,
                longitude = longitude,
                address = address,
                parkedAt = System.currentTimeMillis(),
                clearedAt = null,
                timerDurationMin = null,
                notes = notes
            )
        )
    }

    suspend fun clearParking(id: Long) {
        parkingDao.clearParking(id, System.currentTimeMillis())
    }

    suspend fun updateParking(record: ParkingRecord) {
        parkingDao.update(record)
    }

    suspend fun setTimer(id: Long, durationMin: Int) {
        val record = parkingDao.getActiveOnce()
        if (record != null && record.id == id) {
            parkingDao.update(record.copy(timerDurationMin = durationMin))
        }
    }

    suspend fun deleteHistoryRecord(record: ParkingRecord) {
        parkingDao.delete(record)
    }

    suspend fun clearAllHistory() {
        parkingDao.clearAllHistory()
    }

    suspend fun addFavorite(name: String, latitude: Double, longitude: Double, notes: String? = null): Long {
        return favoriteLocationDao.insert(
            FavoriteLocation(
                name = name,
                latitude = latitude,
                longitude = longitude,
                notes = notes
            )
        )
    }

    suspend fun updateFavorite(location: FavoriteLocation) {
        favoriteLocationDao.update(location)
    }

    suspend fun deleteFavorite(location: FavoriteLocation) {
        favoriteLocationDao.delete(location)
    }

    suspend fun deleteFavoriteById(id: Long) {
        favoriteLocationDao.deleteById(id)
    }
}
