package cz.climb.semester.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import cz.climb.semester.data.entity.AreaEntity
import cz.climb.semester.data.repository.AreaRepository

data class AddAreaUiState(
    val area: AreaEntity? = null,
)

@HiltViewModel
class AddAreaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val areaRepository: AreaRepository,
) : ViewModel() {
    private val areaId: String? = savedStateHandle["areaId"]

    val uiState: StateFlow<AddAreaUiState> = flow {
        emit(AddAreaUiState(area = areaId?.let { areaRepository.getAreaById(it) }))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddAreaUiState(),
    )

    fun saveArea(
        areaId: String?,
        name: String,
        type: String,
        favorite: Boolean,
        latitude: Double?,
        longitude: Double?,
    ) {
        viewModelScope.launch {
            if (areaId == null) {
                areaRepository.addArea(
                    name = name,
                    type = type,
                    latitude = latitude,
                    longitude = longitude,
                    favorite = favorite,
                )
            } else {
                areaRepository.updateArea(
                    areaId = areaId,
                    name = name,
                    type = type,
                    favorite = favorite,
                    previewUri = uiState.value.area?.previewUri,
                    latitude = latitude,
                    longitude = longitude,
                )
            }
        }
    }

    fun deleteArea(areaId: String) {
        viewModelScope.launch {
            areaRepository.deleteArea(areaId)
        }
    }
}
