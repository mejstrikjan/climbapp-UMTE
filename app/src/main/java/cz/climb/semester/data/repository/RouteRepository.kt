package cz.climb.semester.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import cz.climb.semester.data.dao.RouteDao
import cz.climb.semester.data.entity.RouteEntity
import cz.climb.semester.data.entity.RouteListItem
import cz.climb.semester.di.IoDispatcher

@Singleton
class RouteRepository @Inject constructor(
    private val routeDao: RouteDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    val routes: Flow<List<RouteListItem>> = routeDao.observeRoutes()

    suspend fun getRouteById(routeId: String): RouteListItem? = withContext(ioDispatcher) {
        routeDao.getRouteById(routeId)
    }

    suspend fun addRoute(
        name: String,
        grade: String,
        areaId: String?,
        cragId: String?,
        sectorId: String?,
        photoUri: String?,
    ): String? = withContext(ioDispatcher) {
        val sanitizedName = name.trim()
        if (sanitizedName.isEmpty()) return@withContext null
        val now = nowIsoString()
        val entity = RouteEntity(
            id = generateEntityId(),
            name = sanitizedName,
            grade = grade.trim().ifEmpty { "6" },
            areaId = areaId,
            cragId = cragId,
            sectorId = sectorId,
            photoUri = photoUri,
            createdAt = now,
            updatedAt = now,
        )
        routeDao.insert(entity)
        entity.id
    }

    suspend fun updateRoute(
        routeId: String,
        name: String,
        grade: String,
        areaId: String?,
        cragId: String?,
        sectorId: String?,
        photoUri: String?,
    ) = withContext(ioDispatcher) {
        val sanitizedName = name.trim()
        if (sanitizedName.isEmpty()) return@withContext
        routeDao.update(
            routeId,
            sanitizedName,
            grade.trim().ifEmpty { "6" },
            areaId,
            cragId,
            sectorId,
            photoUri,
            nowIsoString(),
        )
    }

    suspend fun toggleFavorite(routeId: String, favorite: Boolean) = withContext(ioDispatcher) {
        routeDao.setFavorite(routeId, favorite, nowIsoString())
    }

    suspend fun attachPhoto(routeId: String, photoUri: String) = withContext(ioDispatcher) {
        routeDao.updatePhotoUri(routeId, photoUri, nowIsoString())
    }

    suspend fun deleteRoute(routeId: String) = withContext(ioDispatcher) {
        routeDao.delete(routeId)
    }

    suspend fun hasRoutes(): Boolean = withContext(ioDispatcher) {
        routeDao.countRoutes() > 0
    }
}
