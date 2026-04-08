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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import cz.climb.semester.viewmodel.AddAreaViewModel

private val areaTypes = listOf("sport", "boulder", "trad", "indoor")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAreaScreen(
    areaId: String?,
    onBack: () -> Unit,
    viewModel: AddAreaViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var name by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf("sport") }
    var favorite by rememberSaveable { mutableStateOf(false) }
    var latitude by rememberSaveable { mutableStateOf("") }
    var longitude by rememberSaveable { mutableStateOf("") }
    val selectedMarkers = if (latitude.toDoubleOrNull() != null && longitude.toDoubleOrNull() != null) {
        listOf(
            MapMarker(
                latitude = latitude.toDoubleOrNull() ?: 50.0755,
                longitude = longitude.toDoubleOrNull() ?: 14.4378,
                label = if (name.isBlank()) "Vybraný bod" else name,
                isSelected = true,
            ),
        )
    } else {
        emptyList()
    }

    LaunchedEffect(uiState.area?.id) {
        uiState.area?.let { area ->
            name = area.name
            selectedType = area.type
            favorite = area.favorite
            latitude = area.latitude?.toString().orEmpty()
            longitude = area.longitude?.toString().orEmpty()
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard(title = if (areaId == null) "Nová oblast" else "Upravit oblast") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Název oblasti") },
                )
                Text("Typ oblasti", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    areaTypes.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type) },
                        )
                    }
                }
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Latitude") },
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Longitude") },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Switch(checked = favorite, onCheckedChange = { favorite = it })
                    Text(
                        text = if (favorite) "Oblíbená oblast" else "Přidat mezi oblíbené",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Button(
                    onClick = {
                        viewModel.saveArea(
                            areaId = areaId,
                            name = name,
                            type = selectedType,
                            favorite = favorite,
                            latitude = latitude.toDoubleOrNull(),
                            longitude = longitude.toDoubleOrNull(),
                        )
                        onBack()
                    },
                    enabled = name.isNotBlank(),
                ) {
                    Text(if (areaId == null) "Vytvořit oblast" else "Uložit oblast")
                }
                if (areaId != null) {
                    Button(onClick = {
                        viewModel.deleteArea(areaId)
                        onBack()
                    }) {
                        Text("Smazat oblast")
                    }
                }
            }
        }
        item {
            MapyRestMapCard(
                title = "Náhled oblasti",
                latitude = latitude.toDoubleOrNull() ?: 50.0755,
                longitude = longitude.toDoubleOrNull() ?: 14.4378,
                hint = if (latitude.toDoubleOrNull() == null || longitude.toDoubleOrNull() == null) {
                    "Mapu můžeš posouvat prstem, přibližovat dvěma prsty a klepnutím vybrat polohu oblasti."
                } else {
                    "Mapu můžeš posouvat prstem, přibližovat dvěma prsty a klepnutím změnit polohu. Vybraný bod je označený."
                },
                markers = selectedMarkers,
                onMapClick = { nextLatitude, nextLongitude ->
                    latitude = "%.6f".format(nextLatitude)
                    longitude = "%.6f".format(nextLongitude)
                },
            )
        }
    }
}
