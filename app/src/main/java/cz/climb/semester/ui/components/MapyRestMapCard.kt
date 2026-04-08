package cz.climb.semester.ui.components

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    var zoom by rememberSaveable { mutableIntStateOf(11) }

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
                    zoom = zoom,
                    markers = markers,
                    onMapClick = onMapClick,
                )
            },
            update = { webView ->
                (webView.getTag(MAP_CLICK_BRIDGE_TAG) as? MapClickBridge)?.onMapClick = onMapClick
                webView.loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/",
                    buildMapHtml(
                        latitude = latitude,
                        longitude = longitude,
                        zoom = zoom,
                        markers = markers,
                        clickable = onMapClick != null,
                    ),
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
    zoom: Int,
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
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_POINTER_DOWN ->
                view.parent?.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP ->
                view.parent?.requestDisallowInterceptTouchEvent(false)
        }
        false
    }
    webView.loadDataWithBaseURL(
        "https://appassets.androidplatform.net/",
        buildMapHtml(
            latitude = latitude,
            longitude = longitude,
            zoom = zoom,
            markers = markers,
            clickable = onMapClick != null,
        ),
        "text/html",
        "utf-8",
        null,
    )
    return webView
}

private fun buildMapHtml(
    latitude: Double,
    longitude: Double,
    zoom: Int,
    markers: List<MapMarker>,
    clickable: Boolean,
): String {
    val markerData = markers.joinToString(
        separator = ",\n",
        prefix = "[",
        postfix = "]",
    ) { marker ->
        "{ latitude: ${marker.latitude}, longitude: ${marker.longitude}, label: '${escapeJsString(marker.label)}', isSelected: ${marker.isSelected.toString().lowercase()} }"
    }

    return """
        <!DOCTYPE html>
        <html lang="cs">
          <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
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
                height: 320px;
                overflow: hidden;
                background: #ede4d7;
                border-radius: 18px;
                touch-action: none;
              }
              .tiles-layer, .markers-layer {
                position: absolute;
                inset: 0;
              }
              .tiles-layer img {
                position: absolute;
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
                pointer-events: none;
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
                z-index: 6;
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
                z-index: 6;
                background: rgba(29, 79, 145, 0.92);
                color: white;
                border-radius: 10px;
                padding: 4px 8px;
                font-size: 10px;
                font-weight: 700;
              }
              .picker-crosshair {
                position: absolute;
                left: calc(50% - 13px);
                top: calc(50% - 13px);
                width: 26px;
                height: 26px;
                border-radius: 50%;
                border: 3px solid #1d4f91;
                background: rgba(255,255,255,0.2);
                box-shadow: 0 0 0 6px rgba(29, 79, 145, 0.14);
                z-index: 5;
                pointer-events: none;
              }
              .picker-crosshair::before,
              .picker-crosshair::after {
                content: '';
                position: absolute;
                background: #1d4f91;
                left: 50%;
                top: 50%;
                transform: translate(-50%, -50%);
              }
              .picker-crosshair::before {
                width: 2px;
                height: 16px;
              }
              .picker-crosshair::after {
                width: 16px;
                height: 2px;
              }
              .zoom-indicator {
                position: absolute;
                right: 8px;
                bottom: 8px;
                z-index: 6;
                background: rgba(31, 26, 23, 0.88);
                color: white;
                border-radius: 10px;
                padding: 6px 10px;
                font-size: 11px;
                font-weight: 700;
              }
            </style>
          </head>
          <body>
            <div class="frame" id="frame">
              <div class="credit">
                <a href="https://api.mapy.com/copyright">Mapové podklady Mapy.com</a>
              </div>
              ${if (clickable) """<div class="picker-hint">Táhni mapou, přibliž dvěma prsty, klepni pro výběr polohy</div><div class="picker-crosshair"></div>""" else ""}
              <div class="tiles-layer" id="tilesLayer"></div>
              <div class="markers-layer" id="markersLayer"></div>
              <div class="zoom-indicator" id="zoomIndicator"></div>
            </div>
            <script>
              const tileSize = 256;
              const frame = document.getElementById('frame');
              const tilesLayer = document.getElementById('tilesLayer');
              const markersLayer = document.getElementById('markersLayer');
              const zoomIndicator = document.getElementById('zoomIndicator');
              const clickable = ${if (clickable) "true" else "false"};
              const markers = $markerData;
              let zoom = $zoom;
              let center = geoToWorld($latitude, $longitude, zoom);
              let pointers = new Map();
              let dragStart = null;
              let hadPinch = false;
              let pinchBaseDistance = null;

              function worldSize(z) {
                return tileSize * Math.pow(2, z);
              }

              function wrapX(x, z) {
                const size = worldSize(z);
                return ((x % size) + size) % size;
              }

              function clampY(y, z) {
                const size = worldSize(z);
                return Math.max(0, Math.min(size - 1, y));
              }

              function geoToWorld(lat, lon, z) {
                const size = worldSize(z);
                const x = ((lon + 180.0) / 360.0) * size;
                const latRad = lat * Math.PI / 180.0;
                const y = (1.0 - Math.log(Math.tan(latRad) + (1.0 / Math.cos(latRad))) / Math.PI) / 2.0 * size;
                return { x, y };
              }

              function worldToGeo(x, y, z) {
                const size = worldSize(z);
                const lon = (wrapX(x, z) / size) * 360.0 - 180.0;
                const n = Math.PI - (2.0 * Math.PI * y / size);
                const lat = (180.0 / Math.PI) * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
                return { latitude: lat, longitude: lon };
              }

              function escapeHtml(text) {
                return text
                  .replace(/&/g, '&amp;')
                  .replace(/</g, '&lt;')
                  .replace(/>/g, '&gt;')
                  .replace(/"/g, '&quot;')
                  .replace(/'/g, '&#39;');
              }

              function screenPointToWorld(localX, localY) {
                return {
                  x: center.x + (localX - frame.clientWidth / 2.0),
                  y: center.y + (localY - frame.clientHeight / 2.0),
                };
              }

              function zoomAroundPoint(localX, localY, nextZoom) {
                if (nextZoom === zoom) {
                  return;
                }
                const worldPoint = screenPointToWorld(localX, localY);
                const geoPoint = worldToGeo(worldPoint.x, worldPoint.y, zoom);
                const newWorldPoint = geoToWorld(geoPoint.latitude, geoPoint.longitude, nextZoom);
                zoom = nextZoom;
                center = {
                  x: wrapX(newWorldPoint.x - (localX - frame.clientWidth / 2.0), zoom),
                  y: clampY(newWorldPoint.y - (localY - frame.clientHeight / 2.0), zoom),
                };
              }

              function render() {
                const width = frame.clientWidth;
                const height = frame.clientHeight;
                const size = worldSize(zoom);
                const topLeftX = center.x - width / 2.0;
                const topLeftY = center.y - height / 2.0;
                const startTileX = Math.floor(topLeftX / tileSize) - 1;
                const endTileX = Math.floor((topLeftX + width) / tileSize) + 1;
                const startTileY = Math.floor(topLeftY / tileSize) - 1;
                const endTileY = Math.floor((topLeftY + height) / tileSize) + 1;
                let tileHtml = '';
                for (let ty = startTileY; ty <= endTileY; ty += 1) {
                  if (ty < 0 || ty >= Math.pow(2, zoom)) continue;
                  for (let tx = startTileX; tx <= endTileX; tx += 1) {
                    const wrappedX = ((tx % Math.pow(2, zoom)) + Math.pow(2, zoom)) % Math.pow(2, zoom);
                    const left = (tx * tileSize) - topLeftX;
                    const top = (ty * tileSize) - topLeftY;
                    tileHtml += '<img style="left:' + left + 'px;top:' + top + 'px" src="https://api.mapy.com/v1/maptiles/basic/256/' + zoom + '/' + wrappedX + '/' + ty + '?apikey=${BuildConfig.MAPY_API_KEY}&lang=cs" alt="Mapa dlaždice" />';
                  }
                }
                tilesLayer.innerHTML = tileHtml;

                let markerHtml = '';
                markers.forEach((marker) => {
                  const world = geoToWorld(marker.latitude, marker.longitude, zoom);
                  let screenX = world.x - topLeftX;
                  while (screenX < -32) screenX += size;
                  while (screenX > width + 32) screenX -= size;
                  const screenY = world.y - topLeftY;
                  if (screenY < -40 || screenY > height + 40) return;
                  markerHtml += '<div class="marker ' + (marker.isSelected ? 'marker-selected' : '') + '" style="left:' + (screenX - 12) + 'px;top:' + (screenY - 24) + 'px;"><span>' + escapeHtml(marker.label) + '</span></div>';
                });
                markersLayer.innerHTML = markerHtml;
                zoomIndicator.textContent = 'Zoom ' + zoom;
              }

              function pointerDistance() {
                const values = Array.from(pointers.values());
                if (values.length !== 2) return null;
                const dx = values[0].x - values[1].x;
                const dy = values[0].y - values[1].y;
                return Math.sqrt(dx * dx + dy * dy);
              }

              function pointerMidpoint() {
                const values = Array.from(pointers.values());
                if (values.length !== 2) return null;
                return {
                  x: (values[0].x + values[1].x) / 2.0,
                  y: (values[0].y + values[1].y) / 2.0,
                };
              }

              frame.addEventListener('pointerdown', (event) => {
                pointers.set(event.pointerId, {
                  x: event.clientX,
                  y: event.clientY,
                  localX: event.clientX - frame.getBoundingClientRect().left,
                  localY: event.clientY - frame.getBoundingClientRect().top,
                });
                if (pointers.size === 1) {
                  dragStart = {
                    pointerId: event.pointerId,
                    x: event.clientX,
                    y: event.clientY,
                    centerX: center.x,
                    centerY: center.y,
                    moved: false,
                  };
                  hadPinch = false;
                  pinchBaseDistance = null;
                } else if (pointers.size === 2) {
                  hadPinch = true;
                  dragStart = null;
                  pinchBaseDistance = pointerDistance();
                }
              });

              frame.addEventListener('pointermove', (event) => {
                if (!pointers.has(event.pointerId)) return;
                pointers.set(event.pointerId, {
                  x: event.clientX,
                  y: event.clientY,
                  localX: event.clientX - frame.getBoundingClientRect().left,
                  localY: event.clientY - frame.getBoundingClientRect().top,
                });

                if (pointers.size === 1 && dragStart && dragStart.pointerId === event.pointerId) {
                  const dx = event.clientX - dragStart.x;
                  const dy = event.clientY - dragStart.y;
                  if (Math.abs(dx) > 4 || Math.abs(dy) > 4) {
                    dragStart.moved = true;
                  }
                  center = {
                    x: wrapX(dragStart.centerX - dx, zoom),
                    y: clampY(dragStart.centerY - dy, zoom),
                  };
                  render();
                } else if (pointers.size === 2) {
                  const distance = pointerDistance();
                  const midpoint = pointerMidpoint();
                  if (distance != null && midpoint != null) {
                    if (pinchBaseDistance == null) {
                      pinchBaseDistance = distance;
                    } else if (distance > pinchBaseDistance * 1.12 && zoom < 17) {
                      zoomAroundPoint(midpoint.x - frame.getBoundingClientRect().left, midpoint.y - frame.getBoundingClientRect().top, zoom + 1);
                      pinchBaseDistance = distance;
                      render();
                    } else if (distance < pinchBaseDistance * 0.88 && zoom > 7) {
                      zoomAroundPoint(midpoint.x - frame.getBoundingClientRect().left, midpoint.y - frame.getBoundingClientRect().top, zoom - 1);
                      pinchBaseDistance = distance;
                      render();
                    }
                  }
                }
              });

              function finishPointer(event) {
                const pointer = pointers.get(event.pointerId);
                const shouldSelect = pointers.size === 1 && dragStart && dragStart.pointerId === event.pointerId && !dragStart.moved && !hadPinch && clickable;
                pointers.delete(event.pointerId);

                if (shouldSelect && pointer) {
                  const world = screenPointToWorld(pointer.localX, pointer.localY);
                  const result = worldToGeo(world.x, world.y, zoom);
                  if (window.AndroidMapBridge && window.AndroidMapBridge.onMapClick) {
                    window.AndroidMapBridge.onMapClick(String(result.latitude), String(result.longitude));
                  }
                }

                if (pointers.size === 1) {
                  const remainingId = Array.from(pointers.keys())[0];
                  const remaining = pointers.get(remainingId);
                  if (remaining) {
                    dragStart = {
                      pointerId: remainingId,
                      x: remaining.x,
                      y: remaining.y,
                      centerX: center.x,
                      centerY: center.y,
                      moved: false,
                    };
                  }
                } else {
                  dragStart = null;
                }

                if (pointers.size < 2) {
                  pinchBaseDistance = null;
                }
                if (pointers.size === 0) {
                  hadPinch = false;
                }
              }

              frame.addEventListener('pointerup', finishPointer);
              frame.addEventListener('pointercancel', finishPointer);
              frame.addEventListener('pointerleave', (event) => {
                if (dragStart && dragStart.pointerId === event.pointerId) {
                  finishPointer(event);
                }
              });

              render();
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

private fun escapeJsString(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", " ")
        .replace("\r", " ")
}

private fun latLonToTilePoint(latitude: Double, longitude: Double, zoom: Int): Pair<Double, Double> {
    val scale = 2.0.pow(zoom.toDouble())
    val x = (longitude + 180.0) / 360.0 * scale
    val latRad = latitude * PI / 180.0
    val y = (1.0 - ln(tan(latRad) + (1.0 / cos(latRad))) / PI) / 2.0 * scale
    return x to y
}
