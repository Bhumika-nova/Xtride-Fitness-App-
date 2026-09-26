package com.example.xtride.feature.run.components

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * Dedicated outdoor running & cycling map tile source.
 * Free, open, high-speed CDN with zero API key requirements and no 403 IP blocking.
 */
private val CyclOSMTileSource = object : OnlineTileSourceBase(
    "CyclOSM_Run",
    0, 19, 256, ".png",
    arrayOf(
        "https://a.tile-cyclosm.openstreetmap.fr/cyclosm/",
        "https://b.tile-cyclosm.openstreetmap.fr/cyclosm/",
        "https://c.tile-cyclosm.openstreetmap.fr/cyclosm/"
    )
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        return baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" +
                MapTileIndex.getX(pMapTileIndex) + "/" +
                MapTileIndex.getY(pMapTileIndex) + mImageFilenameEnding
    }
}

@Composable
fun LiveRunMapView(
    points: List<Pair<Double, Double>>,
    isTracking: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var followRunner by remember { mutableStateOf(true) }

    // Auto-follow latest GPS fix when new points arrive
    LaunchedEffect(points.size, followRunner) {
        if (followRunner && points.isNotEmpty() && mapViewRef != null) {
            val lastPoint = points.last()
            mapViewRef?.controller?.animateTo(GeoPoint(lastPoint.first, lastPoint.second))
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val prefs = ctx.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)
                Configuration.getInstance().load(ctx, prefs)
                Configuration.getInstance().userAgentValue = "XtrideRunnerApp/1.0 (fitness-tracking; support@xtride.org)"

                MapView(ctx).apply {
                    setTileSource(CyclOSMTileSource)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    controller.setZoom(17.5)

                    // Apply dark mode matrix to street map tiles
                    val inverseMatrix = ColorMatrix(
                        floatArrayOf(
                            -1.0f, 0.0f, 0.0f, 0.0f, 255f,
                            0.0f, -1.0f, 0.0f, 0.0f, 255f,
                            0.0f, 0.0f, -1.0f, 0.0f, 255f,
                            0.0f, 0.0f, 0.0f, 1.0f, 0f
                        )
                    )
                    val lr = 0.2126f
                    val lg = 0.7152f
                    val lb = 0.0722f
                    val grayscaleMatrix = ColorMatrix(
                        floatArrayOf(
                            lr, lg, lb, 0f, 0f,
                            lr, lg, lb, 0f, 0f,
                            lr, lg, lb, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    grayscaleMatrix.preConcat(inverseMatrix)
                    overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(grayscaleMatrix))

                    // Initial position (center on current point or default location)
                    if (points.isNotEmpty()) {
                        controller.setCenter(GeoPoint(points.last().first, points.last().second))
                    } else {
                        controller.setCenter(GeoPoint(28.6139, 77.2090))
                    }

                    mapViewRef = this
                }
            },
            update = { mapView ->
                // Clear dynamic overlays (markers & trail) and redraw
                mapView.overlays.removeAll { it is Polyline || it is Marker }

                if (points.isNotEmpty()) {
                    val geoPoints = points.map { GeoPoint(it.first, it.second) }

                    // Glowing Polyline Trail (Athletic Crimson #E11D48)
                    val trailPolyline = Polyline(mapView).apply {
                        setPoints(geoPoints)
                        outlinePaint.color = android.graphics.Color.parseColor("#E11D48")
                        outlinePaint.strokeWidth = 14f
                        outlinePaint.strokeCap = Paint.Cap.ROUND
                        outlinePaint.strokeJoin = Paint.Join.ROUND
                        outlinePaint.isAntiAlias = true
                    }
                    mapView.overlays.add(trailPolyline)

                    // Start Point Marker (Emerald Green #10B981)
                    val startMarker = Marker(mapView).apply {
                        position = geoPoints.first()
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = createCircleMarkerDrawable(
                            context = context,
                            color = android.graphics.Color.parseColor("#10B981"),
                            strokeColor = android.graphics.Color.WHITE,
                            sizeDp = 18
                        )
                        title = "Run Start"
                    }
                    mapView.overlays.add(startMarker)

                    // Live Current Position Marker (White center with crimson ring)
                    val currentMarker = Marker(mapView).apply {
                        position = geoPoints.last()
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = createRunnerMarkerDrawable(context)
                        title = "Current Position"
                    }
                    mapView.overlays.add(currentMarker)
                }

                mapView.invalidate()
            }
        )

        // Floating Map Controls (Recenter, Zoom In, Zoom Out)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Recenter / Follow Button
            IconButton(
                onClick = {
                    followRunner = true
                    if (points.isNotEmpty() && mapViewRef != null) {
                        val lastPoint = points.last()
                        mapViewRef?.controller?.animateTo(GeoPoint(lastPoint.first, lastPoint.second))
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A0F1D).copy(alpha = 0.9f))
                    .border(1.dp, Color(0xFF1E293B), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Recenter",
                    tint = if (followRunner) Color(0xFFE11D48) else Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Zoom In
            IconButton(
                onClick = { mapViewRef?.controller?.zoomIn() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A0F1D).copy(alpha = 0.9f))
                    .border(1.dp, Color(0xFF1E293B), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Zoom Out
            IconButton(
                onClick = { mapViewRef?.controller?.zoomOut() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A0F1D).copy(alpha = 0.9f))
                    .border(1.dp, Color(0xFF1E293B), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapViewRef?.onDetach()
        }
    }
}

private fun createCircleMarkerDrawable(
    context: Context,
    color: Int,
    strokeColor: Int,
    sizeDp: Int
): GradientDrawable {
    val density = context.resources.displayMetrics.density
    val sizePx = (sizeDp * density).toInt()
    val strokePx = (3 * density).toInt()

    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setStroke(strokePx, strokeColor)
        setSize(sizePx, sizePx)
    }
}

private fun createRunnerMarkerDrawable(context: Context): GradientDrawable {
    val density = context.resources.displayMetrics.density
    val sizePx = (24 * density).toInt()
    val strokePx = (5 * density).toInt()

    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(android.graphics.Color.WHITE)
        setStroke(strokePx, android.graphics.Color.parseColor("#E11D48"))
        setSize(sizePx, sizePx)
    }
}
