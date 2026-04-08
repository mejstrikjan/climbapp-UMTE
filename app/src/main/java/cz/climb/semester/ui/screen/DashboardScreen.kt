package cz.climb.semester.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.climb.semester.ui.components.SectionCard
import cz.climb.semester.ui.components.StatBadge
import cz.climb.semester.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    onOpenCamera: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenAreas: () -> Unit,
    onOpenRoutes: () -> Unit,
    onOpenSession: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Ahoj ${uiState.displayName}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatBadge(label = "Oblasti", value = uiState.areaCount.toString())
                StatBadge(label = "Cesty", value = uiState.routeCount.toString())
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatBadge(label = "Výstupy", value = uiState.ascentCount.toString())
                StatBadge(label = "Fotky", value = uiState.captureCount.toString())
            }
        }
        item {
            SectionCard(title = "Rychlá navigace") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onOpenMap, modifier = Modifier.weight(1f)) { Text("Mapa") }
                    Button(onClick = onOpenAreas, modifier = Modifier.weight(1f)) { Text("Oblasti") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onOpenRoutes, modifier = Modifier.weight(1f)) { Text("Cesty") }
                    Button(onClick = onOpenSession, modifier = Modifier.weight(1f)) { Text("Session") }
                }
            }
        }
        item {
            SectionCard(title = "Aktivní session") {
                Text(
                    text = uiState.activeSessionName ?: "Session právě neběží.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        item {
            SectionCard(title = "Vybraná oblast") {
                Text(
                    text = uiState.highlightedAreaName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = uiState.mapHint,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = onOpenMap) {
                    Text("Otevřít mapu")
                }
            }
        }
        item {
            SectionCard(title = "Oblíbené oblasti") {
                Text(
                    text = uiState.favoriteAreas.ifEmpty { listOf("Zatím žádné oblíbené oblasti.") }.joinToString("\n"),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            SectionCard(title = "Oblíbené cesty") {
                Text(
                    text = uiState.favoriteRoutes.ifEmpty { listOf("Zatím žádné oblíbené cesty.") }.joinToString("\n"),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            SectionCard(title = "Poslední výstupy") {
                Text(
                    text = uiState.recentAscents.ifEmpty { listOf("Zatím není zapsaný žádný výstup.") }.joinToString("\n"),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            SectionCard(title = "Extra funkce") {
                Text(
                    text = "Kamera je zabudovaná jako bonus k aplikaci nad Room databází, Hilt DI a DataStore nastavením.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = onOpenCamera) {
                    Text("Otevřít kameru")
                }
            }
        }
    }
}
