package cz.climb.semester.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import cz.climb.semester.data.dao.CaptureDao
import cz.climb.semester.data.entity.CaptureEntity
import cz.climb.semester.di.IoDispatcher

@Singleton
class CaptureRepository @Inject constructor(
    private val captureDao: CaptureDao,
    private val routeRepository: RouteRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    val captures: Flow<List<CaptureEntity>> = captureDao.observeCaptures()

    suspend fun saveCapture(path: String, routeId: String?) = withContext(ioDispatcher) {
        captureDao.insert(CaptureEntity(filePath = path, routeId = routeId))
        if (routeId != null) {
            routeRepository.attachPhoto(routeId, path)
        }
    }
}
