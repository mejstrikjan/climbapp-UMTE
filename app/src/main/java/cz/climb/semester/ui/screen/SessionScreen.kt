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
import cz.climb.semester.viewmodel.SessionViewModel

@Composable
fun SessionScreen(
    onOpenRoute: (String) -> Unit,
    onAddAscent: (String) -> Unit,
    onEditAscent: (String, String) -> Unit,
    viewModel: SessionViewModel = hiltViewModel(),
) {
    val activeSession = viewModel.activeSession.collectAsStateWithLifecycle().value
    val routes = viewModel.routes.collectAsStateWithLifecycle().value
    val ascents = viewModel.ascents.collectAsStateWithLifecycle().value
    val sessions = viewModel.sessions.collectAsStateWithLifecycle().value
    var sessionName by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard(title = "Deník a session") {
                Text(
                    text = activeSession?.name ?: "Aktivní session neběží",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = activeSession?.date ?: "Spusť session a nové výstupy se budou přiřazovat automaticky.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = sessionName,
                    onValueChange = { sessionName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Název nové session") },
                )
                Button(
                    onClick = {
                        if (activeSession == null) {
                            viewModel.startSession(sessionName)
                            sessionName = ""
                        } else {
                            viewModel.endActiveSession()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (activeSession == null) "Spustit session" else "Ukončit session")
                }
            }
        }
        item {
            SectionCard(title = "Rychlé přidání výstupu") {
                if (routes.isEmpty()) {
                    Text("Nejdřív přidej cestu.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    routes.take(6).forEach { route ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onOpenRoute(route.id) }, modifier = Modifier.weight(1f)) {
                                Text("${route.name} (${route.grade})")
                            }
                            Button(onClick = { onAddAscent(route.id) }) {
                                Text("Zapsat")
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionCard(title = "Historie session") {
                if (sessions.isEmpty()) {
                    Text("Zatím žádné session.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    sessions.take(6).forEach { session ->
                        Text(
                            text = buildString {
                                append(session.name)
                                append(" • ")
                                append(session.date)
                                if (session.active) append(" • aktivní")
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
        items(items = ascents, key = { it.id }) { ascent ->
            SectionCard(title = "${ascent.routeName} • ${ascent.style}") {
                Text(ascent.date, style = MaterialTheme.typography.bodySmall)
                if (ascent.notes.isNotBlank()) {
                    Text(ascent.notes, style = MaterialTheme.typography.bodyMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onOpenRoute(ascent.routeId) }, modifier = Modifier.weight(1f)) {
                        Text("Detail cesty")
                    }
                    Button(onClick = { onEditAscent(ascent.routeId, ascent.id) }) {
                        Text("Upravit")
                    }
                    Button(onClick = { viewModel.deleteAscent(ascent.id) }) {
                        Text("Smazat")
                    }
                }
            }
        }
    }
}
