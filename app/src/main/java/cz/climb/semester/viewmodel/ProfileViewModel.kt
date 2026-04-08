package cz.climb.semester.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import cz.climb.semester.data.entity.CaptureEntity
import cz.climb.semester.data.preferences.UserPreferences
import cz.climb.semester.data.preferences.UserPreferencesRepository
import cz.climb.semester.data.repository.CaptureRepository
import cz.climb.semester.data.repository.SessionRepository

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    captureRepository: CaptureRepository,
    sessionRepository: SessionRepository,
) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = userPreferencesRepository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences(),
    )

    val captures: StateFlow<List<CaptureEntity>> = captureRepository.captures.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val sessionsCount: StateFlow<Int> = sessionRepository.sessions
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun updateDisplayName(value: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateDisplayName(value)
        }
    }

    fun updateDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateDarkTheme(enabled)
        }
    }
}
