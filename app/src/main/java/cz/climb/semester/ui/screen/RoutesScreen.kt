package cz.climb.semester.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.climb.semester.ui.components.SectionCard
import cz.climb.semester.viewmodel.RoutesViewModel

@Composable
fun RoutesScreen(
    onAddRoute: () -> Unit,
    onOpenRoute: (String) -> Unit,
    viewModel: RoutesViewModel = hiltViewModel(),
) {
    val routes = viewModel.routes.collectAsStateWithLifecycle().value
    var search by rememberSaveable { mutableStateOf("") }
    var favoritesOnly by rememberSaveable { mutableStateOf(false) }

    val filteredRoutes = routes.filter { route ->
        val matchesSearch = search.isBlank() ||
            route.name.contains(search, ignoreCase = true) ||
            route.grade.contains(search, ignoreCase = true) ||
            (route.areaName?.contains(search, ignoreCase = true) == true) ||
            (route.sectorName?.contains(search, ignoreCase = true) == true)
        val matchesFavorite = !favoritesOnly || route.favorite
        matchesSearch && matchesFavorite
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard(title = "Cesty") {
                Text(
                    text = "Přehled všech uložených cest, jejich obtížností a umístění.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Hledat podle názvu, oblasti nebo sektoru") },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = favoritesOnly,
                        onClick = { favoritesOnly = !favoritesOnly },
                        label = { Text(if (favoritesOnly) "Jen oblíbené" else "Všechny") },
                    )
                    Text(
                        text = "${filteredRoutes.size} / ${routes.size} cest",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Button(onClick = onAddRoute, modifier = Modifier.fillMaxWidth()) {
                    Text("Přidat novou cestu")
                }
            }
        }
        items(items = filteredRoutes, key = { it.id }) { route ->
            SectionCard(title = "${route.name} • ${route.grade}") {
                Text(
                    text = buildString {
                        append(route.areaName ?: "Bez oblasti")
                        route.cragName?.let { append(" • "); append(it) }
                        route.sectorName?.let { append(" • "); append(it) }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = buildString {
                        append(if (route.favorite) "Oblíbená cesta" else "Běžná cesta")
                        if (route.ascentCount > 0) {
                            append(" • ")
                            append(route.ascentCount)
                            append(" výstupů")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(onClick = { onOpenRoute(route.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Otevřít detail")
                }
            }
        }
    }
}
