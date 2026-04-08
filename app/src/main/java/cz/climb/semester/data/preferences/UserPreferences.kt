package cz.climb.semester.data.preferences

data class UserPreferences(
    val displayName: String = "Student climber",
    val darkTheme: Boolean = false,
    val demoSeeded: Boolean = false,
)
