package cz.climb.semester.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import cz.climb.semester.data.entity.AscentListItem
import cz.climb.semester.data.entity.RouteListItem
import cz.climb.semester.data.entity.SessionEntity
import cz.climb.semester.data.repository.RouteRepository
import cz.climb.semester.data.repository.SessionRepository

@HiltViewModel
class SessionViewModel @Inject constructor(
    routeRepository: RouteRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    val activeSession: StateFlow<SessionEntity?> = sessionRepository.activeSession.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    val routes: StateFlow<List<RouteListItem>> = routeRepository.routes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val ascents: StateFlow<List<AscentListItem>> = sessionRepository.ascents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val sessions: StateFlow<List<SessionEntity>> = sessionRepository.sessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun startSession(name: String) {
        viewModelScope.launch {
            sessionRepository.startSession(name)
        }
    }

    fun endActiveSession() {
        viewModelScope.launch {
            sessionRepository.endActiveSession()
        }
    }

    fun addAscent(routeId: String, style: String, notes: String) {
        viewModelScope.launch {
            sessionRepository.addAscent(routeId, style, notes)
        }
    }

    fun deleteAscent(ascentId: String) {
        viewModelScope.launch {
            sessionRepository.deleteAscent(ascentId)
        }
    }
}
