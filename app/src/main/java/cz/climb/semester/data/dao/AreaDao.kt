package cz.climb.semester.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import cz.climb.semester.data.entity.AreaEntity

@Dao
interface AreaDao {
    @Query("SELECT * FROM areas ORDER BY favorite DESC, name ASC")
    fun observeAreas(): Flow<List<AreaEntity>>

    @Query("SELECT * FROM areas WHERE id = :areaId LIMIT 1")
    suspend fun getAreaById(areaId: String): AreaEntity?

    @Query("SELECT COUNT(*) FROM areas")
    suspend fun countAreas(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(area: AreaEntity)

    @Query(
        """
        UPDATE areas
        SET name = :name,
            type = :type,
            favorite = :favorite,
            previewUri = :previewUri,
            latitude = :latitude,
            longitude = :longitude,
            synced = 0
        WHERE id = :areaId
        """
    )
    suspend fun update(
        areaId: String,
        name: String,
        type: String,
        favorite: Boolean,
        previewUri: String?,
        latitude: Double?,
        longitude: Double?,
    )

    @Query("UPDATE areas SET favorite = :favorite, synced = 0 WHERE id = :areaId")
    suspend fun setFavorite(areaId: String, favorite: Boolean)

    @Query("DELETE FROM areas WHERE id = :areaId")
    suspend fun delete(areaId: String)
}
