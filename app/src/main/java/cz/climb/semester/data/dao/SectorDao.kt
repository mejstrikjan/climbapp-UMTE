package cz.climb.semester.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import cz.climb.semester.data.entity.SectorEntity

@Dao
interface SectorDao {
    @Query("SELECT * FROM sectors ORDER BY name ASC")
    fun observeSectors(): Flow<List<SectorEntity>>

    @Query("SELECT * FROM sectors WHERE cragId = :cragId ORDER BY name ASC")
    fun observeSectorsByCrag(cragId: String): Flow<List<SectorEntity>>

    @Query("SELECT * FROM sectors WHERE areaId = :areaId AND cragId IS NULL ORDER BY name ASC")
    fun observeSectorsByArea(areaId: String): Flow<List<SectorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sector: SectorEntity)

    @Query("DELETE FROM sectors WHERE id = :sectorId")
    suspend fun delete(sectorId: String)
}
