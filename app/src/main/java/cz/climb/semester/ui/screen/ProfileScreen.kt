package cz.climb.semester.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import java.io.File
import cz.climb.semester.ui.components.SectionCard
import cz.climb.semester.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val preferences = viewModel.preferences.collectAsStateWithLifecycle().value
    val captures = viewModel.captures.collectAsStateWithLifecycle().value
    val sessionsCount = viewModel.sessionsCount.collectAsStateWithLifecycle().value
    var displayName by rememberSaveable(preferences.displayName) { mutableStateOf(preferences.displayName) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard(title = "Profil") {
                Text(
                    text = "Základní nastavení aplikace a rychlý přehled uložených dat.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Jméno v aplikaci") },
                )
                Button(onClick = { viewModel.updateDisplayName(displayName) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Uložit jméno")
                }
            }
        }
        item {
            SectionCard(title = "Vzhled") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Switch(
                        checked = preferences.darkTheme,
                        onCheckedChange = viewModel::updateDarkTheme,
                    )
                    Text("Tmavý motiv", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        item {
            SectionCard(title = "Přehled dat") {
                Text("Uložených session: $sessionsCount")
                Text("Počet fotek z kamery: ${captures.size}")
                Text(
                    text = captures.firstOrNull()?.let { "Poslední fotka: ${File(it.filePath).name}" }
                        ?: "Zatím žádná fotka",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
