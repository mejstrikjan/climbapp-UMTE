package cz.climb.semester.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import cz.climb.semester.data.entity.AscentEntity
import cz.climb.semester.data.entity.RouteListItem
import cz.climb.semester.data.entity.SessionEntity
import cz.climb.semester.data.repository.RouteRepository
import cz.climb.semester.data.repository.SessionRepository
import cz.climb.semester.data.repository.todayStoredDate

data class AddAscentUiState(
    val route: RouteListItem? = null,
    val ascent: AscentEntity? = null,
    val activeSession: SessionEntity? = null,
    val sessions: List<SessionEntity> = emptyList(),
)

@HiltViewModel
class AddAscentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routeRepository: RouteRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    private val routeId: String = checkNotNull(savedStateHandle["routeId"])
    private val ascentId: String? = savedStateHandle["ascentId"]

    val uiState: StateFlow<AddAscentUiState> = combine(
        sessionRepository.activeSession,
        sessionRepository.sessions,
        flow { emit(routeRepository.getRouteById(routeId)) },
        flow { emit(ascentId?.let { sessionRepository.getAscentById(it) }) },
    ) { activeSession, sessions, route, ascent ->
        AddAscentUiState(
            route = route,
            ascent = ascent,
            activeSession = activeSession,
            sessions = sessions,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddAscentUiState(),
    )

    fun saveAscent(
        ascentId: String?,
        date: String,
        style: String,
        category: String,
        sessionId: String?,
        success: Boolean,
        notes: String,
    ) {
        viewModelScope.launch {
            if (ascentId == null) {
                sessionRepository.addAscent(
                    routeId = routeId,
                    date = date.ifBlank { todayStoredDate() },
                    style = style,
                    category = category,
                    sessionId = sessionId,
                    success = success,
                    notes = notes,
                )
            } else {
                sessionRepository.updateAscent(
                    ascentId = ascentId,
                    date = date.ifBlank { todayStoredDate() },
                    style = style,
                    category = category,
                    sessionId = sessionId,
                    success = success,
                    notes = notes,
                )
            }
        }
    }
}
