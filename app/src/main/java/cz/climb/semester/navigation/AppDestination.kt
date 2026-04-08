package cz.climb.semester.navigation

sealed class AppDestination(
    val route: String,
    val label: String,
    val emoji: String,
) {
    data object Home : AppDestination("home", "Cesty", "🏠")
    data object Logbook : AppDestination("logbook", "Deník", "📖")
    data object Map : AppDestination("map", "Mapa", "🗺️")
    data object Areas : AppDestination("areas", "Oblasti", "🪨")
    data object Profile : AppDestination("profile", "Profil", "👤")
    data object AddArea : AppDestination("add_area", "Nová oblast", "🪨")
    data object AddRoute : AppDestination("add_route", "Nová cesta", "➕")
    data object AddAscent : AppDestination("add_ascent", "Nový výstup", "🧗")
    data object RouteDetail : AppDestination("route_detail", "Detail cesty", "📄")
    data object Camera : AppDestination("camera", "Kamera", "📷")
}

val bottomDestinations = listOf(
    AppDestination.Home,
    AppDestination.Logbook,
    AppDestination.Map,
    AppDestination.Profile,
)
