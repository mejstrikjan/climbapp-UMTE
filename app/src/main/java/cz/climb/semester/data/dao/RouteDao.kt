package cz.climb.semester.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import cz.climb.semester.data.entity.RouteEntity
import cz.climb.semester.data.entity.RouteListItem

@Dao
interface RouteDao {
    @Query(
        """
        SELECT routes.id, routes.name, routes.grade, routes.gradeSystem, routes.gradeIndex, routes.type,
               routes.description, routes.rating, routes.latitude, routes.longitude,
               routes.areaId, routes.cragId, routes.sectorId, routes.photoUri, routes.rockType,
               routes.indoorColor, routes.routeDate, routes.routeStatus, routes.favorite,
               areas.name AS areaName, crags.name AS cragName, sectors.name AS sectorName,
               route_stats.lastAscentDate AS lastAscentDate, COALESCE(route_stats.ascentCount, 0) AS ascentCount,
               routes.createdAt, routes.updatedAt, routes.synced
        FROM routes
        LEFT JOIN areas ON areas.id = routes.areaId
        LEFT JOIN crags ON crags.id = routes.cragId
        LEFT JOIN sectors ON sectors.id = routes.sectorId
        LEFT JOIN (
            SELECT routeId, MAX(date) AS lastAscentDate, COUNT(*) AS ascentCount
            FROM ascents
            GROUP BY routeId
        ) AS route_stats ON route_stats.routeId = routes.id
        ORDER BY routes.updatedAt DESC
        """
    )
    fun observeRoutes(): Flow<List<RouteListItem>>

    @Query(
        """
        SELECT routes.id, routes.name, routes.grade, routes.gradeSystem, routes.gradeIndex, routes.type,
               routes.description, routes.rating, routes.latitude, routes.longitude,
               routes.areaId, routes.cragId, routes.sectorId, routes.photoUri, routes.rockType,
               routes.indoorColor, routes.routeDate, routes.routeStatus, routes.favorite,
               areas.name AS areaName, crags.name AS cragName, sectors.name AS sectorName,
               route_stats.lastAscentDate AS lastAscentDate, COALESCE(route_stats.ascentCount, 0) AS ascentCount,
               routes.createdAt, routes.updatedAt, routes.synced
        FROM routes
        LEFT JOIN areas ON areas.id = routes.areaId
        LEFT JOIN crags ON crags.id = routes.cragId
        LEFT JOIN sectors ON sectors.id = routes.sectorId
        LEFT JOIN (
            SELECT routeId, MAX(date) AS lastAscentDate, COUNT(*) AS ascentCount
            FROM ascents
            GROUP BY routeId
        ) AS route_stats ON route_stats.routeId = routes.id
        WHERE routes.id = :routeId
        LIMIT 1
        """
    )
    suspend fun getRouteById(routeId: String): RouteListItem?

    @Query("SELECT COUNT(*) FROM routes")
    suspend fun countRoutes(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: RouteEntity)

    @Query(
        """
        UPDATE routes
        SET name = :name,
            grade = :grade,
            areaId = :areaId,
            cragId = :cragId,
            sectorId = :sectorId,
            photoUri = :photoUri,
            updatedAt = :updatedAt,
            synced = 0
        WHERE id = :routeId
        """
    )
    suspend fun update(
        routeId: String,
        name: String,
        grade: String,
        areaId: String?,
        cragId: String?,
        sectorId: String?,
        photoUri: String?,
        updatedAt: String,
    )

    @Query("UPDATE routes SET favorite = :favorite, updatedAt = :updatedAt, synced = 0 WHERE id = :routeId")
    suspend fun setFavorite(routeId: String, favorite: Boolean, updatedAt: String)

    @Query("UPDATE routes SET photoUri = :photoUri, updatedAt = :updatedAt, synced = 0 WHERE id = :routeId")
    suspend fun updatePhotoUri(routeId: String, photoUri: String, updatedAt: String)

    @Query("DELETE FROM routes WHERE id = :routeId")
    suspend fun delete(routeId: String)
}
