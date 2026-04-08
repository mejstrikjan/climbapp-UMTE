package cz.climb.semester

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import cz.climb.semester.data.preferences.UserPreferences
import cz.climb.semester.data.preferences.UserPreferencesRepository
import cz.climb.semester.data.repository.AreaRepository
import cz.climb.semester.data.repository.CragRepository
import cz.climb.semester.data.repository.RouteRepository
import cz.climb.semester.data.repository.SectorRepository

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val areaRepository: AreaRepository,
    private val cragRepository: CragRepository,
    private val sectorRepository: SectorRepository,
    private val routeRepository: RouteRepository,
) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = userPreferencesRepository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences(),
    )

    init {
        viewModelScope.launch {
            seedDemoDataIfNeeded()
        }
    }

    private suspend fun seedDemoDataIfNeeded() {
        val prefs = userPreferencesRepository.preferences.first()
        if (prefs.demoSeeded || areaRepository.hasAreas() || routeRepository.hasRoutes()) {
            return
        }

        val labak = areaRepository.addArea("BigWall Praha", "indoor", 50.1096, 14.4407)
        val alkazar = areaRepository.addArea("Alkazar", "sport", 49.9497, 14.1289)
        val petrohrad = areaRepository.addArea("Petrohrad", "boulder", 50.1275, 13.4383)
        val alkazarVez = cragRepository.addCrag("Věž", alkazar)
        val alkazarPlotny = cragRepository.addCrag("Plotny", alkazar)
        val labakStena = sectorRepository.addSector("Boulder zóna", labak, null)
        val alkazarLevy = sectorRepository.addSector("Levý sektor", alkazar, alkazarVez)
        val alkazarPravy = sectorRepository.addSector("Pravý sektor", alkazar, alkazarPlotny)

        routeRepository.addRoute("Modrá hrana", "6a", labak, null, labakStena, null)
        routeRepository.addRoute("Převis vpravo", "6c+", labak, null, labakStena, null)
        routeRepository.addRoute("Plotna pod věží", "7a", alkazar, alkazarVez, alkazarLevy, null)
        routeRepository.addRoute("Stěna za hranou", "6b+", alkazar, alkazarPlotny, alkazarPravy, null)
        routeRepository.addRoute("Střední boulder", "6B", petrohrad, null, null, null)

        userPreferencesRepository.markDemoSeeded()
    }
}
