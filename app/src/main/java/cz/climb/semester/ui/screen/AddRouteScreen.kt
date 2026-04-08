package cz.climb.semester.ui.screen

import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File
import cz.climb.semester.ui.components.SectionCard
import cz.climb.semester.viewmodel.AddRouteViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRouteScreen(
    routeId: String?,
    onBack: () -> Unit,
    viewModel: AddRouteViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var name by rememberSaveable { mutableStateOf("") }
    var grade by rememberSaveable { mutableStateOf("6a") }
    var selectedAreaId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCragId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedSectorId by rememberSaveable { mutableStateOf<String?>(null) }
    var newCragName by rememberSaveable { mutableStateOf("") }
    var newSectorName by rememberSaveable { mutableStateOf("") }
    var photoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var photoMessage by rememberSaveable { mutableStateOf("Fotku můžeš pořídit rovnou tady ve formuláři.") }
    val pendingPhotoFile = remember { mutableStateOf<File?>(null) }

    val selectedArea = uiState.areas.firstOrNull { it.id == selectedAreaId }
    val isIndoorArea = selectedArea?.type == "indoor"
    val filteredCrags = uiState.crags.filter { it.areaId == selectedAreaId }
    val filteredSectors = uiState.sectors.filter { sector ->
        when {
            selectedCragId != null -> sector.cragId == selectedCragId
            selectedAreaId != null -> sector.areaId == selectedAreaId && sector.cragId == null
            else -> false
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val capturedFile = pendingPhotoFile.value
        if (success && capturedFile != null) {
            photoUri = capturedFile.absolutePath
            MediaScannerConnection.scanFile(
                context,
                arrayOf(capturedFile.absolutePath),
                arrayOf("image/jpeg"),
                null,
            )
            photoMessage = "Fotka byla uložená a bude přiřazená k cestě po uložení formuláře."
        } else {
            if (capturedFile?.exists() == true) {
                capturedFile.delete()
            }
            photoMessage = "Pořízení fotky bylo zrušené."
        }
        pendingPhotoFile.value = null
    }

    LaunchedEffect(uiState.route?.id) {
        uiState.route?.let {
            name = it.name
            grade = it.grade
            selectedAreaId = it.areaId
            selectedCragId = it.cragId
            selectedSectorId = it.sectorId
            photoUri = it.photoUri
        }
    }

    LaunchedEffect(selectedAreaId, isIndoorArea) {
        if (selectedAreaId == null) {
            selectedCragId = null
            selectedSectorId = null
            return@LaunchedEffect
        }
        if (selectedCragId != null && filteredCrags.none { it.id == selectedCragId }) {
            selectedCragId = null
        }
        if (isIndoorArea) {
            selectedCragId = null
        }
        if (selectedSectorId != null && filteredSectors.none { it.id == selectedSectorId }) {
            selectedSectorId = null
        }
    }

    LaunchedEffect(uiState.createdCragId) {
        uiState.createdCragId?.let {
            selectedCragId = it
            selectedSectorId = null
            newCragName = ""
            viewModel.clearCreatedCrag()
        }
    }

    LaunchedEffect(uiState.createdSectorId) {
        uiState.createdSectorId?.let {
            selectedSectorId = it
            newSectorName = ""
            viewModel.clearCreatedSector()
        }
    }

    fun launchInlinePhotoCapture() {
        val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
        val targetFile = File(picturesDir, "route-form-${System.currentTimeMillis()}.jpg")
        pendingPhotoFile.value = targetFile
        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            targetFile,
        )
        photoLauncher.launch(contentUri)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard(title = if (routeId == null) "Nová cesta" else "Upravit cestu") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Název cesty") },
                )
                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Obtížnost") },
                )

                Text("Oblast", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = selectedAreaId == null,
                        onClick = {
                            selectedAreaId = null
                            selectedCragId = null
                            selectedSectorId = null
                        },
                        label = { Text("Bez oblasti") },
                    )
                    uiState.areas.forEach { area ->
                        FilterChip(
                            selected = selectedAreaId == area.id,
                            onClick = {
                                selectedAreaId = area.id
                                selectedCragId = null
                                selectedSectorId = null
                            },
                            label = { Text("${area.name}${if (area.type == "indoor") " • indoor" else ""}") },
                        )
                    }
                }

                if (selectedAreaId != null && !isIndoorArea) {
                    Text("Skála", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = selectedCragId == null,
                            onClick = {
                                selectedCragId = null
                                selectedSectorId = null
                            },
                            label = { Text("Bez skály") },
                        )
                        filteredCrags.forEach { crag ->
                            FilterChip(
                                selected = selectedCragId == crag.id,
                                onClick = {
                                    selectedCragId = crag.id
                                    selectedSectorId = null
                                },
                                label = { Text(crag.name) },
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newCragName,
                            onValueChange = { newCragName = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Nová skála") },
                        )
                        Button(
                            onClick = { viewModel.addCrag(newCragName, selectedAreaId) },
                            enabled = newCragName.isNotBlank(),
                        ) {
                            Text("+")
                        }
                    }
                }

                if (selectedAreaId != null) {
                    Text("Sektor", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = selectedSectorId == null,
                            onClick = { selectedSectorId = null },
                            label = { Text("Bez sektoru") },
                        )
                        filteredSectors.forEach { sector ->
                            FilterChip(
                                selected = selectedSectorId == sector.id,
                                onClick = { selectedSectorId = sector.id },
                                label = { Text(sector.name) },
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newSectorName,
                            onValueChange = { newSectorName = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Nový sektor") },
                        )
                        Button(
                            onClick = { viewModel.addSector(newSectorName, selectedAreaId, if (isIndoorArea) null else selectedCragId) },
                            enabled = newSectorName.isNotBlank(),
                        ) {
                            Text("+")
                        }
                    }
                }

                photoUri?.let { path ->
                    BitmapFactory.decodeFile(path)?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Fotka cesty",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                        )
                    }
                }
                Text(photoMessage, style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { launchInlinePhotoCapture() }) {
                        Text("Vyfotit")
                    }
                    if (photoUri != null) {
                        Button(onClick = {
                            photoUri = null
                            photoMessage = "Fotka byla odebraná z formuláře."
                        }) {
                            Text("Odebrat fotku")
                        }
                    }
                }
                Button(
                    onClick = {
                        viewModel.saveRoute(
                            routeId = routeId,
                            name = name,
                            grade = grade,
                            areaId = selectedAreaId,
                            cragId = if (isIndoorArea) null else selectedCragId,
                            sectorId = selectedSectorId,
                            photoUri = photoUri,
                        )
                        onBack()
                    },
                    enabled = name.isNotBlank(),
                ) {
                    Text(if (routeId == null) "Uložit cestu" else "Uložit změny")
                }
            }
        }
    }
}
