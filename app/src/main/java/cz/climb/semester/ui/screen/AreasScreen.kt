package cz.climb.semester.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import cz.climb.semester.viewmodel.AreasViewModel

@Composable
fun AreasScreen(
    onAddArea: () -> Unit,
    onEditArea: (String) -> Unit,
    viewModel: AreasViewModel = hiltViewModel(),
) {
    val areas = viewModel.areas.collectAsStateWithLifecycle().value
    var search by rememberSaveable { mutableStateOf("") }

    val filteredAreas = areas.filter {
        search.isBlank() || it.name.contains(search, ignoreCase = true) || it.type.contains(search, ignoreCase = true)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard(title = "Oblasti") {
                Text(
                    text = "Správa lezeckých oblastí, indoor hal a jejich základních údajů.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = onAddArea, modifier = Modifier.fillMaxWidth()) {
                    Text("Přidat oblast")
                }
            }
        }
        item {
            SectionCard(title = "Filtrovat oblasti") {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Hledat podle názvu nebo typu") },
                )
                Text(
                    text = "${filteredAreas.size} / ${areas.size} oblastí",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        items(items = filteredAreas, key = { it.id }) { area ->
            SectionCard(title = area.name) {
                Text("Typ: ${area.type}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = if (area.latitude != null && area.longitude != null) {
                        "Souřadnice: ${area.latitude}, ${area.longitude}"
                    } else {
                        "Souřadnice zatím nejsou zadané"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = if (area.favorite) "Oblíbená oblast" else "Běžná oblast",
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onEditArea(area.id) }, modifier = Modifier.weight(1f)) {
                        Text("Upravit")
                    }
                    Button(onClick = { viewModel.toggleFavorite(area) }) {
                        Text(if (area.favorite) "Odebrat" else "Oblíbit")
                    }
                    Button(onClick = { viewModel.deleteArea(area.id) }) {
                        Text("Smazat")
                    }
                }
            }
        }
    }
}
