package cz.climb.semester.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import cz.climb.semester.ui.components.MapMarker
import cz.climb.semester.ui.components.MapyRestMapCard
import cz.climb.semester.ui.components.SectionCard
import cz.climb.semester.viewmodel.AreasViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapScreen(
    onAddArea: () -> Unit,
    onEditArea: (String) -> Unit,
    onManageAreas: () -> Unit,
    viewModel: AreasViewModel = hiltViewModel(),
) {
    val areas = viewModel.areas.collectAsStateWithLifecycle().value
    val defaultArea = areas.firstOrNull { it.favorite } ?: areas.firstOrNull()
    var selectedAreaId by rememberSaveable(defaultArea?.id) {
        mutableStateOf(defaultArea?.id)
    }
    val selectedArea = areas.firstOrNull { it.id == selectedAreaId } ?: defaultArea
    val areasWithLocation = areas.filter { it.latitude != null && it.longitude != null }
    val fallbackLatitude = 50.0755
    val fallbackLongitude = 14.4378
    val mapLatitude = selectedArea?.latitude ?: areasWithLocation.firstOrNull()?.latitude ?: fallbackLatitude
    val mapLongitude = selectedArea?.longitude ?: areasWithLocation.firstOrNull()?.longitude ?: fallbackLongitude
    val markers = areasWithLocation.map { area ->
        MapMarker(
            latitude = area.latitude ?: fallbackLatitude,
            longitude = area.longitude ?: fallbackLongitude,
            label = area.name,
            isSelected = selectedArea?.id == area.id,
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            MapyRestMapCard(
                title = selectedArea?.let { "Mapa oblasti ${it.name}" } ?: "Mapa oblastí",
                latitude = mapLatitude,
                longitude = mapLongitude,
                markers = markers,
                hint = when {
                    selectedArea == null -> "Zatím nejsou uložené žádné oblasti. Přidej první a mapa se vystředí na ni."
                    selectedArea.latitude == null || selectedArea.longitude == null ->
                        "Vybraná oblast ještě nemá souřadnice, proto je mapa vystředěná na jinou uloženou oblast nebo na Prahu."
                    else -> "Mapa zobrazuje všechny oblasti se souřadnicemi. Vybraná oblast je zvýrazněná modře a mapou lze táhnout prstem."
                },
            )
        }
        item {
            SectionCard(title = "Vyber oblast") {
                Text(
                    text = "Klepni na oblast níže a mapa se na ni vystředí.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (areas.isEmpty()) {
                    Text(
                        text = "Aplikace je zatím prázdná. Přidej oblast v záložce Oblasti.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(onClick = onAddArea) {
                        Text("Přidat první oblast")
                    }
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        areas.forEach { area ->
                            FilterChip(
                                selected = selectedArea?.id == area.id,
                                onClick = { selectedAreaId = area.id },
                                label = {
                                    Text(
                                        buildString {
                                            append(area.name)
                                            append(" • ")
                                            append(area.type)
                                            if (area.favorite) {
                                                append(" • oblíbená")
                                            }
                                        },
                                    )
                                },
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onManageAreas, modifier = Modifier.weight(1f)) {
                            Text("Správa oblastí")
                        }
                        selectedArea?.let { area ->
                            Button(onClick = { onEditArea(area.id) }, modifier = Modifier.weight(1f)) {
                                Text("Upravit oblast")
                            }
                        }
                    }
                }
            }
        }
        item {
            Button(onClick = onAddArea, modifier = Modifier.fillMaxWidth()) {
                Text("+ Oblast")
            }
        }
        if (areasWithLocation.isNotEmpty()) {
            item {
                SectionCard(title = "Oblasti na mapě") {
                    Text(
                        text = "${areasWithLocation.size} oblastí má souřadnice a vykresluje se jako marker.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    areasWithLocation.forEach { area ->
                        Text(
                            text = buildString {
                                append(if (selectedArea?.id == area.id) "• " else "  ")
                                append(area.name)
                                append(" — ")
                                append(area.latitude)
                                append(", ")
                                append(area.longitude)
                            },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
