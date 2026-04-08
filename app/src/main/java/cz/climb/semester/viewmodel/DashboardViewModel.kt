package cz.climb.semester.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import cz.climb.semester.data.entity.AreaEntity
import cz.climb.semester.data.entity.AscentListItem
import cz.climb.semester.data.entity.RouteListItem
import cz.climb.semester.data.entity.SessionEntity
import cz.climb.semester.data.preferences.UserPreferences
import cz.climb.semester.data.preferences.UserPreferencesRepository
import cz.climb.semester.data.repository.AreaRepository
import cz.climb.semester.data.repository.CaptureRepository
import cz.climb.semester.data.repository.RouteRepository
import cz.climb.semester.data.repository.SessionRepository

data class DashboardUiState(
    val displayName: String = "Student climber",
    val areaCount: Int = 0,
    val routeCount: Int = 0,
    val ascentCount: Int = 0,
    val captureCount: Int = 0,
    val activeSessionName: String? = null,
    val highlightedAreaName: String = "Praha",
    val mapLatitude: Double = 50.0755,
    val mapLongitude: Double = 14.4378,
    val mapHint: String = "Přidej nebo označ oblíbenou oblast, ať se mapa vystředí přesněji.",
    val favoriteAreas: List<String> = emptyList(),
    val favoriteRoutes: List<String> = emptyList(),
    val recentAscents: List<String> = emptyList(),
)

private data class DashboardSnapshot(
    val areas: List<AreaEntity> = emptyList(),
    val routes: List<RouteListItem> = emptyList(),
    val ascents: List<AscentListItem> = emptyList(),
    val activeSession: SessionEntity? = null,
    val captureCount: Int = 0,
    val preferences: UserPreferences = UserPreferences(),
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    areaRepository: AreaRepository,
    routeRepository: RouteRepository,
    sessionRepository: SessionRepository,
    captureRepository: CaptureRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                combine(
                    areaRepository.areas,
                    routeRepository.routes,
                    sessionRepository.ascents,
                    sessionRepository.activeSession,
                ) { areas, routes, ascents, activeSession ->
                    DashboardSnapshot(
                        areas = areas,
                        routes = routes,
                        ascents = ascents,
                        activeSession = activeSession,
                    )
                },
                combine(
                    captureRepository.captures,
                    userPreferencesRepository.preferences,
                ) { captures, preferences ->
                    captures.size to preferences
                },
            ) { snapshot, meta ->
                snapshot.copy(
                    captureCount = meta.first,
                    preferences = meta.second,
                )
            }.collectLatest { snapshot ->
                _uiState.value = snapshot.toUiState()
            }
        }
    }
}

private fun DashboardSnapshot.toUiState(): DashboardUiState {
    val area = areas.firstOrNull { it.favorite } ?: areas.firstOrNull()
    val hasCoordinates = area?.latitude != null && area.longitude != null

    return DashboardUiState(
        displayName = preferences.displayName,
        areaCount = areas.size,
        routeCount = routes.size,
        ascentCount = ascents.size,
        captureCount = captureCount,
        activeSessionName = activeSession?.name,
        highlightedAreaName = area?.name ?: "Praha",
        mapLatitude = area?.latitude ?: 50.0755,
        mapLongitude = area?.longitude ?: 14.4378,
        mapHint = when {
            area == null -> "Zatím nejsou uložené oblasti, proto je zobrazená Praha."
            hasCoordinates -> "Mapa je vystředěná na oblíbenou nebo první oblast."
            else -> "Vybraná oblast nemá souřadnice, proto je zobrazená Praha."
        },
        favoriteAreas = areas.filter { it.favorite }.take(4).map { it.name },
        favoriteRoutes = routes.filter { it.favorite }.take(4).map { "${it.name} (${it.grade})" },
        recentAscents = ascents.take(4).map { "${it.routeName} • ${it.style}" },
    )
}
