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
import cz.climb.semester.data.repository.AreaRepository

@HiltViewModel
class AreasViewModel @Inject constructor(
    private val areaRepository: AreaRepository,
) : ViewModel() {
    val areas: StateFlow<List<AreaEntity>> = areaRepository.areas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun addArea(name: String, type: String, latitude: Double?, longitude: Double?) {
        viewModelScope.launch {
            areaRepository.addArea(name, type, latitude, longitude)
        }
    }

    fun toggleFavorite(area: AreaEntity) {
        viewModelScope.launch {
            areaRepository.toggleFavorite(area)
        }
    }

    fun deleteArea(areaId: String) {
        viewModelScope.launch {
            areaRepository.deleteArea(areaId)
        }
    }
}
