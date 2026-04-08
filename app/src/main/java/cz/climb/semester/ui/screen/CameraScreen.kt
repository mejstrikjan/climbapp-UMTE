package cz.climb.semester.ui.screen

import android.Manifest
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.content.pm.PackageManager
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import cz.climb.semester.ui.components.SectionCard
import cz.climb.semester.viewmodel.CameraViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    presetRouteId: String? = null,
    viewModel: CameraViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val captures = viewModel.captures.collectAsStateWithLifecycle().value
    val routes = viewModel.routes.collectAsStateWithLifecycle().value
    var message by rememberSaveable { mutableStateOf("Kamera připravená.") }
    var routeSearch by rememberSaveable { mutableStateOf("") }
    var selectedRouteId by rememberSaveable { mutableStateOf<String?>(null) }
    var hasCameraPermission by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
        message = if (granted) "Povolení uděleno." else "Bez povolení nejde otevřít preview."
    }
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }

    LaunchedEffect(lifecycleOwner, hasCameraPermission) {
        if (hasCameraPermission) {
            cameraController.bindToLifecycle(lifecycleOwner)
        }
    }

    LaunchedEffect(presetRouteId, routes) {
        if (presetRouteId != null && routes.any { it.id == presetRouteId }) {
            selectedRouteId = presetRouteId
        }
    }

    val filteredRoutes = routes.filter { route ->
        routeSearch.isBlank() ||
            route.name.contains(routeSearch, ignoreCase = true) ||
            (route.areaName?.contains(routeSearch, ignoreCase = true) == true)
    }.take(12)

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard(title = "Kamera") {
                Button(onClick = onBack) {
                    Text("Zpět")
                }
                OutlinedTextField(
                    value = routeSearch,
                    onValueChange = { routeSearch = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Najít cestu pro fotku") },
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    filteredRoutes.forEach { route ->
                        Button(onClick = { selectedRouteId = route.id }) {
                            Text(
                                if (selectedRouteId == route.id) {
                                    "✓ ${route.name}"
                                } else {
                                    route.name
                                },
                            )
                        }
                    }
                }
                Text(
                    text = selectedRouteId?.let { routeId ->
                        val routeName = routes.firstOrNull { it.id == routeId }?.name ?: "Vybraná cesta"
                        "Fotka se uloží jako fotka cesty: $routeName"
                    } ?: "Můžeš vyfotit i bez přiřazení, ale pro fotku lezecké cesty nejdřív vyber cestu.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (!hasCameraPermission) {
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Povolit kameru")
                    }
                    Text(message)
                } else {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        factory = { previewContext ->
                            PreviewView(previewContext).apply {
                                controller = cameraController
                            }
                        },
                    )
                    Button(
                        onClick = {
                            val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
                            val targetFile = File(picturesDir, "capture-${System.currentTimeMillis()}.jpg")
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(targetFile).build()
                            cameraController.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        viewModel.saveCapture(targetFile.absolutePath, selectedRouteId)
                                        MediaScannerConnection.scanFile(
                                            context,
                                            arrayOf(targetFile.absolutePath),
                                            arrayOf("image/jpeg"),
                                            null,
                                        )
                                        val targetRoute = routes.firstOrNull { it.id == selectedRouteId }?.name
                                        message = if (targetRoute != null) {
                                            "Fotka uložená k cestě $targetRoute"
                                        } else {
                                            "Fotka uložená: ${targetFile.name}"
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        message = "Uložení selhalo: ${exception.message}"
                                    }
                                },
                            )
                        },
                    ) {
                        Text("Vyfotit")
                    }
                    Text(message)
                }
            }
        }
        items(items = captures, key = { it.id }) { capture ->
            SectionCard(title = File(capture.filePath).name) {
                capture.routeId?.let { routeId ->
                    val routeName = routes.firstOrNull { it.id == routeId }?.name
                    Text(
                        text = "Cesta: ${routeName ?: routeId}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                BitmapFactory.decodeFile(capture.filePath)?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = File(capture.filePath).name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                    )
                }
                Text(capture.filePath, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
