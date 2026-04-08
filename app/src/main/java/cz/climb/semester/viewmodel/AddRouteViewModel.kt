package cz.climb.semester.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import cz.climb.semester.data.entity.AreaEntity
import cz.climb.semester.data.entity.CragEntity
import cz.climb.semester.data.entity.RouteListItem
import cz.climb.semester.data.entity.SectorEntity
import cz.climb.semester.data.repository.AreaRepository
import cz.climb.semester.data.repository.CragRepository
import cz.climb.semester.data.repository.RouteRepository
import cz.climb.semester.data.repository.SectorRepository

data class AddRouteUiState(
    val areas: List<AreaEntity> = emptyList(),
    val crags: List<CragEntity> = emptyList(),
    val sectors: List<SectorEntity> = emptyList(),
    val route: RouteListItem? = null,
    val createdCragId: String? = null,
    val createdSectorId: String? = null,
)

@HiltViewModel
class AddRouteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    areaRepository: AreaRepository,
    private val cragRepository: CragRepository,
    private val sectorRepository: SectorRepository,
    private val routeRepository: RouteRepository,
) : ViewModel() {
    private val routeId: String? = savedStateHandle["routeId"]
    private val createdCragId = MutableStateFlow<String?>(null)
    private val createdSectorId = MutableStateFlow<String?>(null)
    private val routeFlow = flow { emit(routeId?.let { routeRepository.getRouteById(it) }) }

    val uiState: StateFlow<AddRouteUiState> = combine(
        combine(areaRepository.areas, cragRepository.crags, sectorRepository.sectors, routeFlow) { areas, crags, sectors, route ->
            AddRouteUiState(
                areas = areas,
                crags = crags,
                sectors = sectors,
                route = route,
            )
        },
        createdCragId,
        createdSectorId,
    ) { baseState, createdCragId, createdSectorId ->
        baseState.copy(createdCragId = createdCragId, createdSectorId = createdSectorId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddRouteUiState(),
    )

    fun saveRoute(
        routeId: String?,
        name: String,
        grade: String,
        areaId: String?,
        cragId: String?,
        sectorId: String?,
        photoUri: String?,
    ) {
        viewModelScope.launch {
            if (routeId == null) {
                routeRepository.addRoute(name, grade, areaId, cragId, sectorId, photoUri)
            } else {
                routeRepository.updateRoute(routeId, name, grade, areaId, cragId, sectorId, photoUri)
            }
        }
    }

    fun addCrag(name: String, areaId: String?) {
        viewModelScope.launch {
            createdCragId.value = null
            createdCragId.value = cragRepository.addCrag(name, areaId)
        }
    }

    fun addSector(name: String, areaId: String?, cragId: String?) {
        viewModelScope.launch {
            createdSectorId.value = null
            createdSectorId.value = sectorRepository.addSector(name, areaId, cragId)
        }
    }

    fun clearCreatedCrag() {
        createdCragId.value = null
    }

    fun clearCreatedSector() {
        createdSectorId.value = null
    }
}
