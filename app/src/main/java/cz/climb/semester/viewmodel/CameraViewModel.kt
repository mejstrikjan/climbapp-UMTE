package cz.climb.semester.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import cz.climb.semester.data.entity.CaptureEntity
import cz.climb.semester.data.entity.RouteListItem
import cz.climb.semester.data.repository.CaptureRepository
import cz.climb.semester.data.repository.RouteRepository

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val captureRepository: CaptureRepository,
    routeRepository: RouteRepository,
) : ViewModel() {
    val captures: StateFlow<List<CaptureEntity>> = captureRepository.captures.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val routes: StateFlow<List<RouteListItem>> = routeRepository.routes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun saveCapture(path: String, routeId: String?) {
        viewModelScope.launch {
            captureRepository.saveCapture(path, routeId)
        }
    }
}
