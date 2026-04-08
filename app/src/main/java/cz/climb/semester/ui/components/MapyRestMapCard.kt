package cz.climb.semester.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.view.MotionEvent
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import cz.climb.semesterlite.BuildConfig
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.tan

data class MapMarker(
    val latitude: Double,
    val longitude: Double,
    val label: String,
    val isSelected: Boolean = false,
)

@Composable
fun MapyRestMapCard(
    title: String,
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    hint: String? = null,
    markers: List<MapMarker> = emptyList(),
    onMapClick: ((Double, Double) -> Unit)? = null,
) {
    SectionCard(title = title, modifier = modifier) {
        if (BuildConfig.MAPY_API_KEY.isBlank()) {
            Text(
                text = "Mapy.com API klíč chybí. Doplňen je z původní .env.local aplikace.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            return@SectionCard
        }

        hint?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            factory = { context ->
                createMapyWebView(
                    webView = WebView(context),
                    latitude = latitude,
                    longitude = longitude,
                    markers = markers,
                    onMapClick = onMapClick,
                )
            },
            update = { webView ->
                (webView.getTag(MAP_CLICK_BRIDGE_TAG) as? MapClickBridge)?.onMapClick = onMapClick
                webView.loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/",
                    buildMapHtml(latitude = latitude, longitude = longitude, markers = markers, clickable = onMapClick != null),
                    "text/html",
                    "utf-8",
                    null,
                )
            },
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createMapyWebView(
    webView: WebView,
    latitude: Double,
    longitude: Double,
    markers: List<MapMarker>,
    onMapClick: ((Double, Double) -> Unit)?,
): WebView {
    with(webView.settings) {
        javaScriptEnabled = true
        domStorageEnabled = true
        builtInZoomControls = false
        displayZoomControls = false
        cacheMode = WebSettings.LOAD_DEFAULT
        userAgentString = BuildConfig.MAPY_USER_AGENT
    }
    webView.isVerticalScrollBarEnabled = false
    webView.isHorizontalScrollBarEnabled = false
    webView.webViewClient = WebViewClient()
    val bridge = MapClickBridge(onMapClick)
    webView.addJavascriptInterface(bridge, "AndroidMapBridge")
    webView.setTag(MAP_CLICK_BRIDGE_TAG, bridge)
    webView.setOnTouchListener { view, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> view.parent?.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.parent?.requestDisallowInterceptTouchEvent(false)
        }
        false
    }
    webView.loadDataWithBaseURL(
        "https://appassets.androidplatform.net/",
        buildMapHtml(latitude = latitude, longitude = longitude, markers = markers, clickable = onMapClick != null),
        "text/html",
        "utf-8",
        null,
    )
    return webView
}

private fun buildMapHtml(latitude: Double, longitude: Double, markers: List<MapMarker>, clickable: Boolean): String {
    val zoom = 11
    val tileSize = 256
    val containerWidth = 768
    val containerHeight = 320
    val point = latLonToTilePoint(latitude, longitude, zoom)
    val tileX = point.first.toInt()
    val tileY = point.second.toInt()
    val offsetX = ((point.first - tileX) * tileSize)
    val offsetY = ((point.second - tileY) * tileSize)
    val shiftX = 256 + offsetX - (containerWidth / 2.0)
    val shiftY = 256 + offsetY - (containerHeight / 2.0)

    val tileHtml = buildString {
        for (row in -1..1) {
            for (column in -1..1) {
                val x = tileX + column
                val y = tileY + row
                append(
                    """
                    <img
                      src="https://api.mapy.com/v1/maptiles/basic/256/$zoom/$x/$y?apikey=${BuildConfig.MAPY_API_KEY}&lang=cs"
                      alt="Mapa dlaždice"
                    />
                    """.trimIndent(),
                )
            }
        }
    }

    val markerHtml = markers.mapNotNull { marker ->
        val markerPoint = latLonToTilePoint(marker.latitude, marker.longitude, zoom)
        val markerX = ((markerPoint.first - point.first) * tileSize) + (containerWidth / 2.0)
        val markerY = ((markerPoint.second - point.second) * tileSize) + (containerHeight / 2.0)
        if (markerX !in -24.0..(containerWidth + 24.0) || markerY !in -36.0..(containerHeight + 24.0)) {
            null
        } else {
            """
            <div class="marker ${if (marker.isSelected) "marker-selected" else ""}" style="left:${markerX - 12}px; top:${markerY - 24}px;">
              <span>${marker.label}</span>
            </div>
            """.trimIndent()
        }
    }.joinToString("\n")

    return """
        <!DOCTYPE html>
        <html lang="cs">
          <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <style>
              html, body {
                margin: 0;
                padding: 0;
                background: #ede4d7;
                overflow: hidden;
                font-family: sans-serif;
              }
              .frame {
                position: relative;
                width: 100%;
                height: ${containerHeight}px;
                overflow: hidden;
                background: #ede4d7;
                border-radius: 18px;
              }
              .map-layer {
                position: absolute;
                left: 0;
                top: 0;
                width: 100%;
                height: 100%;
                touch-action: none;
                transform: translate3d(0px, 0px, 0px);
              }
              .tiles {
                position: absolute;
                left: -${shiftX}px;
                top: -${shiftY}px;
                width: 768px;
                height: 768px;
                display: grid;
                grid-template-columns: repeat(3, 256px);
                grid-template-rows: repeat(3, 256px);
              }
              .tiles img {
                width: 256px;
                height: 256px;
                display: block;
              }
              .marker {
                position: absolute;
                width: 24px;
                height: 24px;
                border-radius: 12px;
                background: #d9682e;
                border: 3px solid white;
                box-shadow: 0 6px 14px rgba(0, 0, 0, 0.25);
                z-index: 3;
              }
              .marker-selected {
                background: #1d4f91;
                width: 28px;
                height: 28px;
                border-radius: 14px;
                margin-left: -2px;
                margin-top: -2px;
              }
              .marker::after {
                content: '';
                position: absolute;
                left: 6px;
                top: 17px;
                width: 7px;
                height: 10px;
                background: inherit;
                clip-path: polygon(50% 100%, 0 0, 100% 0);
              }
              .marker span {
                position: absolute;
                left: 50%;
                bottom: 34px;
                transform: translateX(-50%);
                white-space: nowrap;
                background: rgba(255, 248, 241, 0.96);
                border-radius: 10px;
                padding: 4px 8px;
                font-size: 10px;
                font-weight: 700;
                color: #1f1a17;
              }
              .credit {
                position: absolute;
                left: 8px;
                top: 8px;
                z-index: 4;
                background: rgba(255, 248, 241, 0.94);
                border-radius: 10px;
                padding: 4px 8px;
                font-size: 10px;
                font-weight: 700;
              }
              .credit a {
                color: #1f1a17;
                text-decoration: none;
              }
              .picker-hint {
                position: absolute;
                right: 8px;
                top: 8px;
                z-index: 4;
                background: rgba(29, 79, 145, 0.92);
                color: white;
                border-radius: 10px;
                padding: 4px 8px;
                font-size: 10px;
                font-weight: 700;
              }
            </style>
          </head>
          <body>
            <div class="frame">
              <div class="credit">
                <a href="https://api.mapy.com/copyright">Mapové podklady Mapy.com</a>
              </div>
              ${if (clickable) """<div class="picker-hint">Klepni do mapy pro výběr polohy</div>""" else ""}
              <div class="map-layer">
                <div class="tiles">$tileHtml</div>
                $markerHtml
              </div>
            </div>
            <script>
              const frame = document.querySelector('.frame');
              const mapLayer = document.querySelector('.map-layer');
              let translateX = 0;
              let translateY = 0;
              let startX = 0;
              let startY = 0;
              let dragging = false;
              let moved = false;
              const tileSize = 256;
              const zoom = $zoom;
              const baseTileX = ${point.first};
              const baseTileY = ${point.second};
              const frameWidth = ${containerWidth}.0;
              const frameHeight = ${containerHeight}.0;

              function clamp(value, min, max) {
                return Math.max(min, Math.min(max, value));
              }

              function applyTransform() {
                translateX = clamp(translateX, -170, 170);
                translateY = clamp(translateY, -180, 180);
                mapLayer.style.transform = 'translate3d(' + translateX + 'px, ' + translateY + 'px, 0)';
              }

              frame.addEventListener('pointerdown', (event) => {
                dragging = true;
                moved = false;
                startX = event.clientX - translateX;
                startY = event.clientY - translateY;
              });

              frame.addEventListener('pointermove', (event) => {
                if (!dragging) return;
                if (Math.abs((event.clientX - translateX) - startX) > 4 || Math.abs((event.clientY - translateY) - startY) > 4) {
                  moved = true;
                }
                translateX = event.clientX - startX;
                translateY = event.clientY - startY;
                applyTransform();
              });

              function yToLatitude(tileY) {
                const n = Math.PI - (2 * Math.PI * tileY / Math.pow(2, zoom));
                return (180 / Math.PI) * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
              }

              function clickToGeo(clientX, clientY) {
                const rect = frame.getBoundingClientRect();
                const localX = clientX - rect.left;
                const localY = clientY - rect.top;
                const tileX = baseTileX + ((localX - translateX) - (frameWidth / 2.0)) / tileSize;
                const tileY = baseTileY + ((localY - translateY) - (frameHeight / 2.0)) / tileSize;
                const longitude = (tileX / Math.pow(2, zoom)) * 360.0 - 180.0;
                const latitude = yToLatitude(tileY);
                return { latitude, longitude };
              }

              const stopDragging = (event) => {
                if (dragging && !moved && event && ${if (clickable) "true" else "false"}) {
                  const result = clickToGeo(event.clientX, event.clientY);
                  if (window.AndroidMapBridge && window.AndroidMapBridge.onMapClick) {
                    window.AndroidMapBridge.onMapClick(String(result.latitude), String(result.longitude));
                  }
                }
                dragging = false;
              };

              frame.addEventListener('pointerup', stopDragging);
              frame.addEventListener('pointercancel', stopDragging);
              frame.addEventListener('pointerleave', () => { dragging = false; });
            </script>
          </body>
        </html>
    """.trimIndent()
}

private const val MAP_CLICK_BRIDGE_TAG = 0x4D415059

private class MapClickBridge(
    var onMapClick: ((Double, Double) -> Unit)?,
) {
    @JavascriptInterface
    fun onMapClick(latitude: String, longitude: String) {
        val lat = latitude.toDoubleOrNull() ?: return
        val lon = longitude.toDoubleOrNull() ?: return
        onMapClick?.invoke(lat, lon)
    }
}

private fun latLonToTilePoint(latitude: Double, longitude: Double, zoom: Int): Pair<Double, Double> {
    val scale = 2.0.pow(zoom.toDouble())
    val x = (longitude + 180.0) / 360.0 * scale
    val latRad = latitude * PI / 180.0
    val y = (1.0 - ln(tan(latRad) + (1.0 / cos(latRad))) / PI) / 2.0 * scale
    return x to y
}
