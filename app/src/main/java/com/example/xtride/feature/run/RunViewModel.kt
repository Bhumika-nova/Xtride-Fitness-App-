package com.example.xtride.feature.run

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xtride.data.local.entity.RoutePointEntity
import com.example.xtride.data.local.entity.RunSessionEntity
import com.example.xtride.data.repository.RunRepository
import com.example.xtride.service.LocationTrackingService
import com.example.xtride.service.TrackingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class RunTab {
    LIVE_TRACK,
    HISTORY
}

data class RunUiState(
    val selectedTab: RunTab = RunTab.LIVE_TRACK,
    val selectedActivityType: String = "RUN", // "WALK", "RUN", "HIKE"
    val historyFilter: String = "ALL",        // "ALL", "WALK", "RUN", "HIKE"
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val durationSeconds: Long = 0L,
    val distanceMeters: Float = 0f,
    val currentPaceSecondsPerKm: Int = 0,
    val cadenceSpm: Int = 0,
    val caloriesBurned: Int = 0,
    val pathPoints: List<Pair<Double, Double>> = emptyList(),
    val showStopDialog: Boolean = false,
    val snackbarMessage: String? = null
)

class RunViewModel(
    private val runRepo: RunRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RunUiState())
    val uiState: StateFlow<RunUiState> = _uiState.asStateFlow()

    val pastSessions: StateFlow<List<RunSessionEntity>> = runRepo.allRunSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Observe TrackingManager flows and sync with UiState
        viewModelScope.launch {
            TrackingManager.isTracking.collect { tracking ->
                _uiState.value = _uiState.value.copy(isTracking = tracking)
            }
        }
        viewModelScope.launch {
            TrackingManager.isPaused.collect { paused ->
                _uiState.value = _uiState.value.copy(isPaused = paused)
            }
        }
        viewModelScope.launch {
            TrackingManager.activityType.collect { type ->
                _uiState.value = _uiState.value.copy(selectedActivityType = type)
            }
        }
        viewModelScope.launch {
            TrackingManager.durationSeconds.collect { duration ->
                _uiState.value = _uiState.value.copy(durationSeconds = duration)
            }
        }
        viewModelScope.launch {
            TrackingManager.distanceMeters.collect { distance ->
                _uiState.value = _uiState.value.copy(distanceMeters = distance)
            }
        }
        viewModelScope.launch {
            TrackingManager.currentPaceSecondsPerKm.collect { pace ->
                _uiState.value = _uiState.value.copy(currentPaceSecondsPerKm = pace)
            }
        }
        viewModelScope.launch {
            TrackingManager.cadenceSpm.collect { cadence ->
                _uiState.value = _uiState.value.copy(cadenceSpm = cadence)
            }
        }
        viewModelScope.launch {
            TrackingManager.caloriesBurned.collect { calories ->
                _uiState.value = _uiState.value.copy(caloriesBurned = calories)
            }
        }
        viewModelScope.launch {
            TrackingManager.pathPoints.collect { points ->
                _uiState.value = _uiState.value.copy(pathPoints = points)
            }
        }
    }

    fun selectTab(tab: RunTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun selectActivityType(type: String) {
        if (!_uiState.value.isTracking) {
            _uiState.value = _uiState.value.copy(selectedActivityType = type)
            TrackingManager.setActivityType(type)
        }
    }

    fun selectHistoryFilter(filter: String) {
        _uiState.value = _uiState.value.copy(historyFilter = filter)
    }

    fun startTracking(context: Context) {
        val currentType = _uiState.value.selectedActivityType
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START
            putExtra(LocationTrackingService.EXTRA_ACTIVITY_TYPE, currentType)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun pauseTracking(context: Context) {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_PAUSE
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun resumeTracking(context: Context) {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_RESUME
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun showStopConfirmation(show: Boolean) {
        _uiState.value = _uiState.value.copy(showStopDialog = show)
    }

    fun stopAndSaveActivity(context: Context) {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP
        }
        ContextCompat.startForegroundService(context, intent)

        val duration = TrackingManager.durationSeconds.value
        val distance = TrackingManager.distanceMeters.value
        val calories = TrackingManager.caloriesBurned.value
        val pace = TrackingManager.currentPaceSecondsPerKm.value
        val cadence = TrackingManager.cadenceSpm.value
        val currentType = TrackingManager.activityType.value
        val points = TrackingManager.pathPoints.value
        val now = System.currentTimeMillis()
        val startTime = now - (duration * 1000L)

        viewModelScope.launch {
            if (distance >= 10f) {
                val session = RunSessionEntity(
                    activityType = currentType,
                    startTimeStamp = startTime,
                    endTimeStamp = now,
                    totalDistanceMeters = distance,
                    durationSeconds = duration,
                    avgPaceSecondsPerKm = pace,
                    totalCalories = calories,
                    avgCadenceSpm = cadence,
                    elevationGainMeters = 0f
                )
                val routePointEntities = points.map { (lat, lng) ->
                    RoutePointEntity(
                        sessionId = 0,
                        latitude = lat,
                        longitude = lng,
                        timestamp = now
                    )
                }
                runRepo.saveRunSession(session, routePointEntities)
                _uiState.value = _uiState.value.copy(
                    showStopDialog = false,
                    snackbarMessage = "${currentType.lowercase().replaceFirstChar { it.uppercase() }} session saved successfully!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    showStopDialog = false,
                    snackbarMessage = "Activity was too short to record."
                )
            }
            TrackingManager.reset()
        }
    }

    fun discardActivity(context: Context) {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP
        }
        ContextCompat.startForegroundService(context, intent)
        val currentType = TrackingManager.activityType.value
        TrackingManager.reset()
        _uiState.value = _uiState.value.copy(
            showStopDialog = false,
            snackbarMessage = "${currentType.lowercase().replaceFirstChar { it.uppercase() }} discarded."
        )
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            runRepo.deleteRunSession(sessionId)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Activity record removed.")
        }
    }

    fun dismissSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }
}
