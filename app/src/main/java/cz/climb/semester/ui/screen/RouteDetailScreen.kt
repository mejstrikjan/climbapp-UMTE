package cz.climb.semester.ui.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.climb.semester.ui.components.SectionCard
import cz.climb.semester.viewmodel.RouteDetailViewModel

@Composable
fun RouteDetailScreen(
    routeId: String,
    onEditRoute: (String) -> Unit,
    onAddAscent: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: RouteDetailViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val route = uiState.route

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard(title = route?.name ?: "Detail cesty") {
                Text(
                    text = route?.let { "${it.grade} • ${it.type}" } ?: "Načítání...",
                    style = MaterialTheme.typography.bodyLarge,
                )
                route?.photoUri?.let { path ->
                    BitmapFactory.decodeFile(path)?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = route.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                        )
                    }
                }
                Text(route?.areaName ?: "Bez oblasti", style = MaterialTheme.typography.bodyMedium)
                Text(route?.description?.ifBlank { "Bez popisu" } ?: "Bez popisu", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { onEditRoute(routeId) }) { Text("Upravit cestu") }
                Button(onClick = { onAddAscent(routeId) }) { Text("Zaznamenat výstup") }
                Button(onClick = { viewModel.toggleFavorite() }) {
                    Text(if (route?.favorite == true) "Odebrat z oblíbených" else "Přidat do oblíbených")
                }
                Button(onClick = {
                    viewModel.deleteRoute()
                    onBack()
                }) { Text("Smazat cestu") }
            }
        }
        item {
            SectionCard(title = "Výstupy") {
                Text(
                    text = if (uiState.ascents.isEmpty()) "Zatím žádné výstupy" else "Celkem ${uiState.ascents.size} záznamů",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        items(items = uiState.ascents, key = { it.id }) { ascent ->
            SectionCard(title = "${ascent.style} • ${ascent.date}") {
                if (ascent.notes.isNotBlank()) {
                    Text(ascent.notes, style = MaterialTheme.typography.bodyMedium)
                }
                Button(onClick = { viewModel.deleteAscent(ascent.id) }) {
                    Text("Smazat výstup")
                }
            }
        }
    }
}
