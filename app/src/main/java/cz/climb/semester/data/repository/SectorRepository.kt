package cz.climb.semester.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import cz.climb.semester.data.dao.SectorDao
import cz.climb.semester.data.entity.SectorEntity
import cz.climb.semester.di.IoDispatcher

@Singleton
class SectorRepository @Inject constructor(
    private val sectorDao: SectorDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    val sectors: Flow<List<SectorEntity>> = sectorDao.observeSectors()

    suspend fun addSector(name: String, areaId: String?, cragId: String?): String? = withContext(ioDispatcher) {
        val sanitizedName = name.trim()
        if (sanitizedName.isEmpty()) return@withContext null
        val entity = SectorEntity(
            id = generateEntityId(),
            name = sanitizedName,
            areaId = areaId,
            cragId = cragId,
            createdAt = nowIsoString(),
        )
        sectorDao.insert(entity)
        entity.id
    }
}
