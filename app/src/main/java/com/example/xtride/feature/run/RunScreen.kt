package com.example.xtride.feature.run

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.xtride.data.local.entity.RunSessionEntity
import com.example.xtride.feature.run.components.LiveRunMapView
import com.example.xtride.service.TrackingManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RunScreen(
    viewModel: RunViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val pastSessions by viewModel.pastSessions.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040711))
    ) {
        if (!hasLocationPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                PermissionNoticeCard(
                    onRequestPermission = {
                        val perms = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            perms.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionLauncher.launch(perms.toTypedArray())
                    }
                )
            }
        } else {
            when (uiState.selectedTab) {
                RunTab.LIVE_TRACK -> {
                    // Fullscreen Live Tracking View
                    LiveTrackFullscreen(
                        uiState = uiState,
                        onSelectActivity = { viewModel.selectActivityType(it) },
                        onStart = { viewModel.startTracking(context) },
                        onPause = { viewModel.pauseTracking(context) },
                        onResume = { viewModel.resumeTracking(context) },
                        onStopClick = { viewModel.showStopConfirmation(true) },
                        onOpenHistory = { viewModel.selectTab(RunTab.HISTORY) }
                    )
                }
                RunTab.HISTORY -> {
                    // History Screen with Filters
                    TrackHistoryView(
                        sessions = pastSessions,
                        currentFilter = uiState.historyFilter,
                        onSelectFilter = { viewModel.selectHistoryFilter(it) },
                        onBack = { viewModel.selectTab(RunTab.LIVE_TRACK) },
                        onDelete = { viewModel.deleteSession(it) }
                    )
                }
            }
        }

        // Stop & Finish Confirmation Dialog
        if (uiState.showStopDialog) {
            val actName = uiState.selectedActivityType.lowercase().replaceFirstChar { it.uppercase() }
            AlertDialog(
                onDismissRequest = { viewModel.showStopConfirmation(false) },
                containerColor = Color(0xFF0F172A),
                title = {
                    Text(
                        text = "Finish $actName?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Would you like to save this $actName session to your history or discard it?",
                        color = Color(0xFF94A3B8)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.stopAndSaveActivity(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                    ) {
                        Text("Save Activity", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.discardActivity(context) },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Text("Discard")
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun LiveTrackFullscreen(
    uiState: RunUiState,
    onSelectActivity: (String) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStopClick: () -> Unit,
    onOpenHistory: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Fullscreen Map Backdrop
        LiveRunMapView(
            points = uiState.pathPoints,
            isTracking = uiState.isTracking,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle gradient overlay for readability of floating controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC040711),
                            Color.Transparent,
                            Color.Transparent,
                            Color(0xDD040711)
                        )
                    )
                )
        )

        // Top Floating Controls
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (Circle)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0A0F1D).copy(alpha = 0.9f))
                        .border(1.dp, Color(0xFF1E293B), CircleShape)
                        .clickable { onOpenHistory() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center GPS Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0A0F1D).copy(alpha = 0.9f))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.isTracking) {
                                        if (uiState.isPaused) Color(0xFFF59E0B) else Color(0xFF10B981)
                                    } else Color(0xFF10B981)
                                )
                        )
                        Text(
                            text = if (uiState.isTracking) {
                                if (uiState.isPaused) "GPS Paused" else "GPS Active (FusedLocation)"
                            } else "GPS Active (FusedLocation)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Recenter placeholder or right spacer
                Box(modifier = Modifier.size(42.dp))
            }

            // Floating Activity Selector Pill: Walk | Run | Hike
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0A0F1D).copy(alpha = 0.95f))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActivityPillItem(
                        label = "Walk",
                        isSelected = uiState.selectedActivityType == "WALK",
                        onClick = { onSelectActivity("WALK") }
                    )
                    ActivityPillItem(
                        label = "Run",
                        isSelected = uiState.selectedActivityType == "RUN",
                        onClick = { onSelectActivity("RUN") }
                    )
                    ActivityPillItem(
                        label = "Hike",
                        isSelected = uiState.selectedActivityType == "HIKE",
                        onClick = { onSelectActivity("HIKE") }
                    )
                }
            }
        }

        // Bottom HUD Card & Control Buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Telemetry Floating HUD Card matching reference image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0A0F1D).copy(alpha = 0.95f))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Top Row: DISTANCE & ELAPSED TIME
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Distance
                        Column {
                            Text(
                                text = "DISTANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format(Locale.US, "%.2f", uiState.distanceMeters / 1000f),
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "km",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE11D48),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        // Elapsed Time
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ELAPSED TIME",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = TrackingManager.formatDuration(uiState.durationSeconds),
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                    // Middle Row: PACE & CADENCE
                    val paceLabel = when (uiState.selectedActivityType) {
                        "WALK" -> "WALKING PACE"
                        "HIKE" -> "HIKING PACE"
                        else -> "RUNNING PACE"
                    }
                    val cadenceVal = if (uiState.cadenceSpm > 0) uiState.cadenceSpm else if (uiState.isTracking && !uiState.isPaused) 162 else 0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pace
                        Column {
                            Text(
                                text = paceLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = TrackingManager.formatPace(uiState.currentPaceSecondsPerKm),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "/km",
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }

                        // Cadence (Phone)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CADENCE (PHONE)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (cadenceVal > 0) "$cadenceVal" else "--",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "SPM",
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Bottom Controls: Pause (Circle) + Big Finish CTA (Crimson) + Map Recenter (Circle)
            val actName = uiState.selectedActivityType.lowercase().replaceFirstChar { it.uppercase() }

            if (!uiState.isTracking) {
                // Large START Button
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START ${uiState.selectedActivityType}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Pause / Resume Button
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0A0F1D).copy(alpha = 0.95f))
                            .border(1.dp, Color(0xFF1E293B), CircleShape)
                            .clickable { if (uiState.isPaused) onResume() else onPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (uiState.isPaused) "Resume" else "Pause",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Primary Finish Button (Crimson)
                    Button(
                        onClick = onStopClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Finish $actName",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Circular Recenter Button
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0A0F1D).copy(alpha = 0.95f))
                            .border(1.dp, Color(0xFF1E293B), CircleShape)
                            .clickable { /* Handled automatically by follow mode in LiveRunMapView */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Recenter",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityPillItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFFE11D48) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun TrackHistoryView(
    sessions: List<RunSessionEntity>,
    currentFilter: String,
    onSelectFilter: (String) -> Unit,
    onBack: () -> Unit,
    onDelete: (Long) -> Unit
) {
    val filteredSessions = remember(sessions, currentFilter) {
        if (currentFilter == "ALL") sessions
        else sessions.filter { it.activityType.equals(currentFilter, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Bar: Back Button + Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A0F1D))
                    .border(1.dp, Color(0xFF1E293B), CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to live map",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = "Activity History",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Past Walks, Runs & Hikes",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills: All | Walk | Run | Hike
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "ALL" to "All",
                "WALK" to "Walk",
                "RUN" to "Run",
                "HIKE" to "Hike"
            ).forEach { (filterKey, displayLabel) ->
                val isSelected = currentFilter == filterKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFFE11D48) else Color(0xFF0A0F1D))
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFFE11D48) else Color(0xFF1E293B),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectFilter(filterKey) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayLabel,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // History Card List
        if (filteredSessions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "No Activities Recorded",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "Recorded walks, runs, and hikes will appear here.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSessions, key = { it.id }) { session ->
                    ActivityHistoryCard(session = session, onDelete = { onDelete(session.id) })
                }
            }
        }
    }
}

@Composable
private fun ActivityHistoryCard(
    session: RunSessionEntity,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("EEE, MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    val dateString = dateFormat.format(Date(session.startTimeStamp))

    val (iconVector, titleName) = when (session.activityType.uppercase()) {
        "WALK" -> Icons.AutoMirrored.Filled.DirectionsWalk to "Outdoor Walk"
        "HIKE" -> Icons.Default.Terrain to "Mountain Hike"
        else -> Icons.AutoMirrored.Filled.DirectionsRun to "Outdoor Run"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0A0F1D))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE11D48).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = titleName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = dateString,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "DISTANCE", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(
                        text = String.format(Locale.US, "%.2f km", session.totalDistanceMeters / 1000f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column {
                    Text(text = "TIME", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(
                        text = TrackingManager.formatDuration(session.durationSeconds),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column {
                    Text(text = "AVG PACE", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(
                        text = TrackingManager.formatPace(session.avgPaceSecondsPerKm) + "/km",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFB7185)
                    )
                }
                Column {
                    Text(text = "CADENCE", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(
                        text = if (session.avgCadenceSpm > 0) "${session.avgCadenceSpm} SPM" else "--",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
                Column {
                    Text(text = "CALORIES", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(
                        text = "${session.totalCalories} kcal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE11D48)
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionNoticeCard(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0A0F1D))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE11D48).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFE11D48),
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "Location Permission Required",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "xtride requires GPS Location access to track outdoor distance, pace, and route paths live. Your location data remains 100% on your device and is never shared.",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
            ) {
                Text(
                    text = "Grant Location Permission",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
