package com.byd.dilink.extras.core.data.repository

import com.byd.dilink.extras.core.data.dao.HazardDao
import com.byd.dilink.extras.core.data.entities.HazardRecord
import kotlinx.coroutines.flow.Flow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class HazardRepository(private val hazardDao: HazardDao) {

    fun getAllHazards(): Flow<List<HazardRecord>> = hazardDao.getAll()

    suspend fun getAllHazardsOnce(): List<HazardRecord> = hazardDao.getAllOnce()

    fun getHazardsByType(type: String): Flow<List<HazardRecord>> = hazardDao.getByType(type)

    fun getHazardCount(): Flow<Int> = hazardDao.getCount()

    suspend fun insertHazard(record: HazardRecord): Long = hazardDao.insert(record)

    suspend fun deleteHazard(record: HazardRecord) = hazardDao.delete(record)

    suspend fun deleteHazardById(id: Long) = hazardDao.deleteById(id)

    suspend fun deleteExpiredHazards(cutoffTimestamp: Long) = hazardDao.deleteOlderThan(cutoffTimestamp)

    suspend fun deleteAllHazards() = hazardDao.deleteAll()

    suspend fun getNearbyHazards(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double
    ): List<HazardRecord> {
        val allHazards = hazardDao.getNearby()
        return allHazards.filter { hazard ->
            haversineDistance(latitude, longitude, hazard.latitude, hazard.longitude) <= radiusMeters
        }
    }

    suspend fun getHazardsWithDistance(
        latitude: Double,
        longitude: Double
    ): List<Pair<HazardRecord, Double>> {
        val allHazards = hazardDao.getNearby()
        return allHazards.map { hazard ->
            val distance = haversineDistance(latitude, longitude, hazard.latitude, hazard.longitude)
            hazard to distance
        }.sortedBy { it.second }
    }

    companion object {
        private const val EARTH_RADIUS_METERS = 6371000.0

        fun haversineDistance(
            lat1: Double, lon1: Double,
            lat2: Double, lon2: Double
        ): Double {
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return EARTH_RADIUS_METERS * c
        }

        fun bearing(
            lat1: Double, lon1: Double,
            lat2: Double, lon2: Double
        ): Double {
            val lat1Rad = Math.toRadians(lat1)
            val lat2Rad = Math.toRadians(lat2)
            val dLon = Math.toRadians(lon2 - lon1)
            val y = sin(dLon) * cos(lat2Rad)
            val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)
            val bearing = Math.toDegrees(atan2(y, x))
            return (bearing + 360) % 360
        }

        fun bearingToDirection(bearing: Double): String {
            val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
            val index = ((bearing + 22.5) / 45).toInt() % 8
            return directions[index]
        }

        fun perpendicularDistance(
            pointLat: Double, pointLon: Double,
            lineLat1: Double, lineLon1: Double,
            lineLat2: Double, lineLon2: Double
        ): Double {
            val d13 = haversineDistance(lineLat1, lineLon1, pointLat, pointLon)
            val bearing13 = Math.toRadians(bearing(lineLat1, lineLon1, pointLat, pointLon))
            val bearing12 = Math.toRadians(bearing(lineLat1, lineLon1, lineLat2, lineLon2))
            return kotlin.math.abs(d13 * sin(bearing13 - bearing12))
        }
    }
}
