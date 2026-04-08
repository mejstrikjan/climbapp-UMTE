package cz.climb.semester.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import cz.climb.semester.data.entity.AreaEntity
import cz.climb.semester.data.entity.RouteListItem
import cz.climb.semester.data.repository.AreaRepository
import cz.climb.semester.data.repository.RouteRepository

@HiltViewModel
class RoutesViewModel @Inject constructor(
    areaRepository: AreaRepository,
    private val routeRepository: RouteRepository,
) : ViewModel() {
    val areas: StateFlow<List<AreaEntity>> = areaRepository.areas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val routes: StateFlow<List<RouteListItem>> = routeRepository.routes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun addRoute(name: String, grade: String, areaId: String?) {
        viewModelScope.launch {
            routeRepository.addRoute(name, grade, areaId, null, null, null)
        }
    }

    fun updateRoute(routeId: String, name: String, grade: String, areaId: String?) {
        viewModelScope.launch {
            routeRepository.updateRoute(routeId, name, grade, areaId, null, null, null)
        }
    }

    fun toggleFavorite(routeId: String, favorite: Boolean) {
        viewModelScope.launch {
            routeRepository.toggleFavorite(routeId, favorite)
        }
    }

    fun deleteRoute(routeId: String) {
        viewModelScope.launch {
            routeRepository.deleteRoute(routeId)
        }
    }
}
