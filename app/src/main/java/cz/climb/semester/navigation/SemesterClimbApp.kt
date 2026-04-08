package cz.climb.semester.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cz.climb.semester.MainViewModel
import cz.climb.semester.ui.screen.AddAreaScreen
import cz.climb.semester.ui.screen.AddAscentScreen
import cz.climb.semester.ui.screen.AddRouteScreen
import cz.climb.semester.ui.screen.AreasScreen
import cz.climb.semester.ui.screen.CameraScreen
import cz.climb.semester.ui.screen.MapScreen
import cz.climb.semester.ui.screen.ProfileScreen
import cz.climb.semester.ui.screen.RouteDetailScreen
import cz.climb.semester.ui.screen.RoutesScreen
import cz.climb.semester.ui.screen.SessionScreen
import cz.climb.semester.ui.theme.SemesterClimbTheme

@Composable
fun SemesterClimbApp(mainViewModel: MainViewModel) {
    val preferences by mainViewModel.preferences.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = bottomDestinations.any { currentDestination?.hierarchy?.any { destination -> destination.route == it.route } == true }

    SemesterClimbTheme(darkTheme = preferences.darkTheme) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ) {
                        bottomDestinations.forEach { destination ->
                            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Text(destination.emoji) },
                                label = { Text(destination.label) },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppDestination.Home.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(AppDestination.Home.route) {
                    RoutesScreen(
                        onAddRoute = { navController.navigate(AppDestination.AddRoute.route) },
                        onOpenRoute = { routeId -> navController.navigate("${AppDestination.RouteDetail.route}/$routeId") },
                    )
                }
                composable(AppDestination.Logbook.route) {
                    SessionScreen(
                        onOpenRoute = { routeId -> navController.navigate("${AppDestination.RouteDetail.route}/$routeId") },
                        onAddAscent = { routeId -> navController.navigate("${AppDestination.AddAscent.route}/$routeId") },
                        onEditAscent = { routeId, ascentId ->
                            navController.navigate("${AppDestination.AddAscent.route}/$routeId/$ascentId")
                        },
                    )
                }
                composable(AppDestination.Map.route) {
                    MapScreen(
                        onAddArea = { navController.navigate(AppDestination.AddArea.route) },
                        onEditArea = { areaId -> navController.navigate("${AppDestination.AddArea.route}/$areaId") },
                        onManageAreas = { navController.navigate(AppDestination.Areas.route) },
                    )
                }
                composable(AppDestination.Areas.route) {
                    AreasScreen(
                        onAddArea = { navController.navigate(AppDestination.AddArea.route) },
                        onEditArea = { areaId -> navController.navigate("${AppDestination.AddArea.route}/$areaId") },
                    )
                }
                composable(AppDestination.Profile.route) { ProfileScreen() }
                composable(AppDestination.AddArea.route) {
                    AddAreaScreen(
                        areaId = null,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(
                    route = "${AppDestination.AddArea.route}/{areaId}",
                    arguments = listOf(navArgument("areaId") { type = NavType.StringType }),
                ) { backStackEntry ->
                    AddAreaScreen(
                        areaId = backStackEntry.arguments?.getString("areaId"),
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(AppDestination.AddRoute.route) {
                    AddRouteScreen(
                        routeId = null,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(
                    route = "${AppDestination.AddRoute.route}/{routeId}",
                    arguments = listOf(navArgument("routeId") { type = NavType.StringType }),
                ) { backStackEntry ->
                    AddRouteScreen(
                        routeId = backStackEntry.arguments?.getString("routeId"),
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(
                    route = "${AppDestination.RouteDetail.route}/{routeId}",
                    arguments = listOf(navArgument("routeId") { type = NavType.StringType }),
                ) { backStackEntry ->
                    val routeId = backStackEntry.arguments?.getString("routeId") ?: return@composable
                    RouteDetailScreen(
                        routeId = routeId,
                        onEditRoute = { navController.navigate("${AppDestination.AddRoute.route}/$it") },
                        onAddAscent = { navController.navigate("${AppDestination.AddAscent.route}/$it") },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(
                    route = "${AppDestination.AddAscent.route}/{routeId}",
                    arguments = listOf(navArgument("routeId") { type = NavType.StringType }),
                ) { backStackEntry ->
                    val routeId = backStackEntry.arguments?.getString("routeId") ?: return@composable
                    AddAscentScreen(
                        routeId = routeId,
                        ascentId = null,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(
                    route = "${AppDestination.AddAscent.route}/{routeId}/{ascentId}",
                    arguments = listOf(
                        navArgument("routeId") { type = NavType.StringType },
                        navArgument("ascentId") { type = NavType.StringType },
                    ),
                ) { backStackEntry ->
                    val routeId = backStackEntry.arguments?.getString("routeId") ?: return@composable
                    AddAscentScreen(
                        routeId = routeId,
                        ascentId = backStackEntry.arguments?.getString("ascentId"),
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(AppDestination.Camera.route) {
                    CameraScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    route = "${AppDestination.Camera.route}/{routeId}",
                    arguments = listOf(navArgument("routeId") { type = NavType.StringType }),
                ) { backStackEntry ->
                    CameraScreen(
                        onBack = { navController.popBackStack() },
                        presetRouteId = backStackEntry.arguments?.getString("routeId"),
                    )
                }
            }
        }
    }
}
