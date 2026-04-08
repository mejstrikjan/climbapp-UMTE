package cz.climb.semester.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import cz.climb.semester.di.IoDispatcher

private val Context.userPreferencesDataStore by preferencesDataStore(name = "semester_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    val preferences: Flow<UserPreferences> = context.userPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            UserPreferences(
                displayName = prefs[DISPLAY_NAME] ?: "Student climber",
                darkTheme = prefs[DARK_THEME] ?: false,
                demoSeeded = prefs[DEMO_SEEDED] ?: false,
            )
        }

    suspend fun updateDisplayName(value: String) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[DISPLAY_NAME] = value.trim().ifBlank { "Student climber" }
        }
    }

    suspend fun updateDarkTheme(enabled: Boolean) = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[DARK_THEME] = enabled
        }
    }

    suspend fun markDemoSeeded() = withContext(ioDispatcher) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[DEMO_SEEDED] = true
        }
    }

    private companion object {
        val DISPLAY_NAME: Preferences.Key<String> = stringPreferencesKey("display_name")
        val DARK_THEME: Preferences.Key<Boolean> = booleanPreferencesKey("dark_theme")
        val DEMO_SEEDED: Preferences.Key<Boolean> = booleanPreferencesKey("demo_seeded")
    }
}
