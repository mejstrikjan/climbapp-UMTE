package cz.climb.semester.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import cz.climb.semester.data.dao.CragDao
import cz.climb.semester.data.entity.CragEntity
import cz.climb.semester.di.IoDispatcher

@Singleton
class CragRepository @Inject constructor(
    private val cragDao: CragDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    val crags: Flow<List<CragEntity>> = cragDao.observeCrags()

    suspend fun addCrag(name: String, areaId: String?): String? = withContext(ioDispatcher) {
        val sanitizedName = name.trim()
        if (sanitizedName.isEmpty()) return@withContext null
        val entity = CragEntity(
            id = generateEntityId(),
            name = sanitizedName,
            areaId = areaId,
            createdAt = nowIsoString(),
        )
        cragDao.insert(entity)
        entity.id
    }
}
