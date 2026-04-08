package cz.climb.semester.ui.screen

import android.app.DatePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import cz.climb.semester.data.repository.todayStoredDate
import cz.climb.semester.ui.components.SectionCard
import cz.climb.semester.viewmodel.AddAscentViewModel

private val ascentStyles = listOf("onsight", "flash", "redpoint", "toprope", "project")
private val ascentCategories = listOf(
    "" to "Bez kategorie",
    "warmup" to "Warm-up",
    "project" to "Projekt",
    "training" to "Trénink",
)
private val storedDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val displayDateFormatter = DateTimeFormatter.ofPattern("d. M. yyyy")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAscentScreen(
    routeId: String,
    ascentId: String?,
    onBack: () -> Unit,
    viewModel: AddAscentViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var date by rememberSaveable { mutableStateOf(todayStoredDate()) }
    var style by rememberSaveable { mutableStateOf("redpoint") }
    var category by rememberSaveable { mutableStateOf("") }
    var success by rememberSaveable { mutableStateOf(true) }
    var selectedSessionId by rememberSaveable { mutableStateOf<String?>(null) }
    var notes by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.ascent?.id, uiState.activeSession?.id) {
        uiState.ascent?.let { ascent ->
            date = ascent.date
            style = ascent.style
            category = ascent.category
            success = ascent.success
            selectedSessionId = ascent.sessionId
            notes = ascent.notes
        } ?: run {
            selectedSessionId = uiState.activeSession?.id
        }
    }

    val parsedDate = runCatching { LocalDate.parse(date, storedDateFormatter) }.getOrDefault(LocalDate.now())
    val dateDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            date = LocalDate.of(year, month + 1, dayOfMonth).format(storedDateFormatter)
        },
        parsedDate.year,
        parsedDate.monthValue - 1,
        parsedDate.dayOfMonth,
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard(title = if (ascentId == null) "Zaznamenat výstup" else "Upravit výstup") {
                Text(
                    text = uiState.route?.let { "${it.name} (${it.grade})" } ?: routeId,
                    style = MaterialTheme.typography.titleMedium,
                )
                uiState.activeSession?.let { session ->
                    Text("Aktivní session: ${session.name}", style = MaterialTheme.typography.bodyMedium)
                }
                Button(onClick = { dateDialog.show() }) {
                    Text("Datum: ${parsedDate.format(displayDateFormatter)}")
                }
                Text("Styl výstupu", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ascentStyles.forEach { option ->
                        FilterChip(
                            selected = style == option,
                            onClick = { style = option },
                            label = { Text(option) },
                        )
                    }
                }
                Text("Kategorie", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ascentCategories.forEach { option ->
                        FilterChip(
                            selected = category == option.first,
                            onClick = { category = option.first },
                            label = { Text(option.second) },
                        )
                    }
                }
                Text("Session", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = selectedSessionId == null,
                        onClick = { selectedSessionId = null },
                        label = { Text("Bez session") },
                    )
                    uiState.sessions.take(6).forEach { session ->
                        FilterChip(
                            selected = selectedSessionId == session.id,
                            onClick = { selectedSessionId = session.id },
                            label = { Text(session.name) },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Switch(checked = success, onCheckedChange = { success = it })
                    Text(
                        text = if (success) "Slezeno" else "Pokus / nedolezeno",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Poznámky") },
                    minLines = 3,
                )
                Button(
                    onClick = {
                        viewModel.saveAscent(
                            ascentId = ascentId,
                            date = date,
                            style = style,
                            category = category,
                            sessionId = selectedSessionId,
                            success = success,
                            notes = notes,
                        )
                        onBack()
                    },
                ) {
                    Text(if (ascentId == null) "Uložit výstup" else "Uložit změny")
                }
            }
        }
    }
}
