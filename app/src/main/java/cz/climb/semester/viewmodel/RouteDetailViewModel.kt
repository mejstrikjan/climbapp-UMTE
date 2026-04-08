package cz.climb.semester.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import cz.climb.semester.data.entity.AscentListItem
import cz.climb.semester.data.entity.RouteListItem
import cz.climb.semester.data.repository.RouteRepository
import cz.climb.semester.data.repository.SessionRepository

data class RouteDetailUiState(
    val route: RouteListItem? = null,
    val ascents: List<AscentListItem> = emptyList(),
)

@HiltViewModel
class RouteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routeRepository: RouteRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    private val routeId: String = checkNotNull(savedStateHandle["routeId"])

    val uiState: StateFlow<RouteDetailUiState> = combine(
        flowOf(Unit),
        sessionRepository.observeAscentsByRoute(routeId),
    ) { _, ascents ->
        RouteDetailUiState(
            route = routeRepository.getRouteById(routeId),
            ascents = ascents,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RouteDetailUiState(),
    )

    fun toggleFavorite() {
        viewModelScope.launch {
            val route = routeRepository.getRouteById(routeId) ?: return@launch
            routeRepository.toggleFavorite(route.id, !route.favorite)
        }
    }

    fun deleteRoute() {
        viewModelScope.launch {
            routeRepository.deleteRoute(routeId)
        }
    }

    fun deleteAscent(ascentId: String) {
        viewModelScope.launch {
            sessionRepository.deleteAscent(ascentId)
        }
    }
}
