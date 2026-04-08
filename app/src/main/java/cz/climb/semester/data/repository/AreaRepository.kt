package cz.climb.semester.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import cz.climb.semester.data.dao.AreaDao
import cz.climb.semester.data.entity.AreaEntity
import cz.climb.semester.di.IoDispatcher

@Singleton
class AreaRepository @Inject constructor(
    private val areaDao: AreaDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    val areas: Flow<List<AreaEntity>> = areaDao.observeAreas()

    suspend fun getAreaById(areaId: String): AreaEntity? = withContext(ioDispatcher) {
        areaDao.getAreaById(areaId)
    }

    suspend fun addArea(
        name: String,
        type: String,
        latitude: Double?,
        longitude: Double?,
        favorite: Boolean = false,
        previewUri: String? = null,
    ): String? = withContext(ioDispatcher) {
        val sanitizedName = name.trim()
        if (sanitizedName.isEmpty()) return@withContext null
        val entity = AreaEntity(
            id = generateEntityId(),
            name = sanitizedName,
            type = type,
            favorite = favorite,
            previewUri = previewUri,
            latitude = latitude,
            longitude = longitude,
            createdAt = nowIsoString(),
        )
        areaDao.insert(entity)
        entity.id
    }

    suspend fun updateArea(
        areaId: String,
        name: String,
        type: String,
        favorite: Boolean,
        previewUri: String?,
        latitude: Double?,
        longitude: Double?,
    ) = withContext(ioDispatcher) {
        val sanitizedName = name.trim()
        if (sanitizedName.isEmpty()) return@withContext
        areaDao.update(
            areaId = areaId,
            name = sanitizedName,
            type = type,
            favorite = favorite,
            previewUri = previewUri,
            latitude = latitude,
            longitude = longitude,
        )
    }

    suspend fun toggleFavorite(area: AreaEntity) = withContext(ioDispatcher) {
        areaDao.setFavorite(area.id, !area.favorite)
    }

    suspend fun deleteArea(areaId: String) = withContext(ioDispatcher) {
        areaDao.delete(areaId)
    }

    suspend fun hasAreas(): Boolean = withContext(ioDispatcher) {
        areaDao.countAreas() > 0
    }
}
