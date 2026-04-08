package cz.climb.semester.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import cz.climb.semester.data.entity.AscentEntity
import cz.climb.semester.data.entity.AscentListItem

@Dao
interface AscentDao {
    @Query(
        """
        SELECT ascents.id, ascents.routeId, ascents.sessionId, ascents.date, ascents.style, ascents.category,
               ascents.success, ascents.notes, ascents.createdAt, ascents.updatedAt, ascents.synced,
               routes.name AS routeName, routes.grade AS routeGrade
        FROM ascents
        INNER JOIN routes ON routes.id = ascents.routeId
        ORDER BY ascents.date DESC, ascents.createdAt DESC
        """
    )
    fun observeAscents(): Flow<List<AscentListItem>>

    @Query(
        """
        SELECT ascents.id, ascents.routeId, ascents.sessionId, ascents.date, ascents.style, ascents.category,
               ascents.success, ascents.notes, ascents.createdAt, ascents.updatedAt, ascents.synced,
               routes.name AS routeName, routes.grade AS routeGrade
        FROM ascents
        INNER JOIN routes ON routes.id = ascents.routeId
        WHERE ascents.routeId = :routeId
        ORDER BY ascents.date DESC, ascents.createdAt DESC
        """
    )
    fun observeAscentsByRoute(routeId: String): Flow<List<AscentListItem>>

    @Query("SELECT * FROM ascents WHERE id = :ascentId LIMIT 1")
    suspend fun getAscentById(ascentId: String): AscentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ascent: AscentEntity)

    @Query(
        """
        UPDATE ascents
        SET date = :date,
            style = :style,
            category = :category,
            sessionId = :sessionId,
            success = :success,
            notes = :notes,
            updatedAt = :updatedAt,
            synced = 0
        WHERE id = :ascentId
        """
    )
    suspend fun update(
        ascentId: String,
        date: String,
        style: String,
        category: String,
        sessionId: String?,
        success: Boolean,
        notes: String,
        updatedAt: String,
    )

    @Query("DELETE FROM ascents WHERE id = :ascentId")
    suspend fun delete(ascentId: String)
}
