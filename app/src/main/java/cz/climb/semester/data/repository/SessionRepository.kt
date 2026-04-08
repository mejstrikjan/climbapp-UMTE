package cz.climb.semester.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import cz.climb.semester.data.dao.AscentDao
import cz.climb.semester.data.dao.SessionDao
import cz.climb.semester.data.entity.AscentEntity
import cz.climb.semester.data.entity.AscentListItem
import cz.climb.semester.data.entity.SessionEntity
import cz.climb.semester.di.IoDispatcher

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao,
    private val ascentDao: AscentDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    val activeSession: Flow<SessionEntity?> = sessionDao.observeActiveSession()
    val sessions: Flow<List<SessionEntity>> = sessionDao.observeSessions()
    val ascents: Flow<List<AscentListItem>> = ascentDao.observeAscents()

    fun observeAscentsByRoute(routeId: String): Flow<List<AscentListItem>> = ascentDao.observeAscentsByRoute(routeId)

    suspend fun getAscentById(ascentId: String): AscentEntity? = withContext(ioDispatcher) {
        ascentDao.getAscentById(ascentId)
    }

    suspend fun startSession(name: String) = withContext(ioDispatcher) {
        val now = nowIsoString()
        val date = todayStoredDate()
        sessionDao.endActiveSessions(now, now)
        sessionDao.insert(
            SessionEntity(
                id = generateEntityId(),
                name = name.trim().ifBlank { defaultSessionName(date) },
                date = date,
                autoName = name.isBlank(),
                startedAt = now,
                active = true,
                createdAt = now,
                updatedAt = now,
            )
        )
    }

    suspend fun endActiveSession() = withContext(ioDispatcher) {
        val now = nowIsoString()
        sessionDao.endActiveSessions(now, now)
    }

    suspend fun addAscent(routeId: String, style: String, notes: String) = withContext(ioDispatcher) {
        addAscent(
            routeId = routeId,
            date = todayStoredDate(),
            style = style,
            category = "",
            sessionId = sessionDao.getActiveSession()?.id,
            success = true,
            notes = notes,
        )
    }

    suspend fun addAscent(
        routeId: String,
        date: String,
        style: String,
        category: String,
        sessionId: String?,
        success: Boolean,
        notes: String,
    ) = withContext(ioDispatcher) {
        val now = nowIsoString()
        ascentDao.insert(
            AscentEntity(
                id = generateEntityId(),
                routeId = routeId,
                sessionId = sessionId,
                date = date,
                style = style,
                category = category,
                success = success,
                notes = notes.trim(),
                createdAt = now,
                updatedAt = now,
            )
        )
    }

    suspend fun updateAscent(
        ascentId: String,
        date: String,
        style: String,
        category: String,
        sessionId: String?,
        success: Boolean,
        notes: String,
    ) = withContext(ioDispatcher) {
        ascentDao.update(
            ascentId = ascentId,
            date = date,
            style = style,
            category = category,
            sessionId = sessionId,
            success = success,
            notes = notes.trim(),
            updatedAt = nowIsoString(),
        )
    }

    suspend fun deleteAscent(ascentId: String) = withContext(ioDispatcher) {
        ascentDao.delete(ascentId)
    }

    private fun defaultSessionName(date: String): String {
        return "Session $date"
    }
}
