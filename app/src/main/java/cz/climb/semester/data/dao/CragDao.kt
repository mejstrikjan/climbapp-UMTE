package cz.climb.semester.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import cz.climb.semester.data.entity.CragEntity

@Dao
interface CragDao {
    @Query("SELECT * FROM crags ORDER BY name ASC")
    fun observeCrags(): Flow<List<CragEntity>>

    @Query("SELECT * FROM crags WHERE areaId = :areaId ORDER BY name ASC")
    fun observeCragsByArea(areaId: String): Flow<List<CragEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crag: CragEntity)

    @Query("DELETE FROM crags WHERE id = :cragId")
    suspend fun delete(cragId: String)
}
