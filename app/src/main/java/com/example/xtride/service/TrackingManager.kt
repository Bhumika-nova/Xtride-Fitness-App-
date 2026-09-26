package com.example.xtride.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Singleton state holder for live outdoor activity telemetry (Walk, Run, Hike).
 * Connects LocationTrackingService, TrackViewModel, and Compose UI seamlessly.
 */
object TrackingManager {

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _activityType = MutableStateFlow("RUN") // "RUN", "WALK", "HIKE"
    val activityType: StateFlow<String> = _activityType.asStateFlow()

    private val _durationSeconds = MutableStateFlow(0L)
    val durationSeconds: StateFlow<Long> = _durationSeconds.asStateFlow()

    private val _distanceMeters = MutableStateFlow(0f)
    val distanceMeters: StateFlow<Float> = _distanceMeters.asStateFlow()

    private val _currentPaceSecondsPerKm = MutableStateFlow(0)
    val currentPaceSecondsPerKm: StateFlow<Int> = _currentPaceSecondsPerKm.asStateFlow()

    private val _cadenceSpm = MutableStateFlow(0)
    val cadenceSpm: StateFlow<Int> = _cadenceSpm.asStateFlow()

    private val _caloriesBurned = MutableStateFlow(0)
    val caloriesBurned: StateFlow<Int> = _caloriesBurned.asStateFlow()

    private val _currentSpeedMps = MutableStateFlow(0f)
    val currentSpeedMps: StateFlow<Float> = _currentSpeedMps.asStateFlow()

    private val _pathPoints = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())
    val pathPoints: StateFlow<List<Pair<Double, Double>>> = _pathPoints.asStateFlow()

    fun setTracking(tracking: Boolean) {
        _isTracking.value = tracking
    }

    fun setPaused(paused: Boolean) {
        _isPaused.value = paused
    }

    fun setActivityType(type: String) {
        _activityType.value = type
    }

    fun setDuration(seconds: Long) {
        _durationSeconds.value = seconds
    }

    fun updateMetrics(
        distance: Float,
        speedMps: Float,
        paceSecPerKm: Int,
        cadence: Int,
        calories: Int
    ) {
        _distanceMeters.value = distance
        _currentSpeedMps.value = speedMps
        _currentPaceSecondsPerKm.value = paceSecPerKm
        _cadenceSpm.value = cadence
        _caloriesBurned.value = calories
    }

    fun addPoint(lat: Double, lng: Double) {
        _pathPoints.value = _pathPoints.value + (lat to lng)
    }

    fun reset() {
        _isTracking.value = false
        _isPaused.value = false
        _durationSeconds.value = 0L
        _distanceMeters.value = 0f
        _currentPaceSecondsPerKm.value = 0
        _cadenceSpm.value = 0
        _caloriesBurned.value = 0
        _currentSpeedMps.value = 0f
        _pathPoints.value = emptyList()
    }

    fun formatDuration(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatPace(paceSecPerKm: Int): String {
        if (paceSecPerKm <= 0 || paceSecPerKm > 3600) return "_'_\""
        val minutes = paceSecPerKm / 60
        val seconds = paceSecPerKm % 60
        return String.format(Locale.US, "%d'%02d\"", minutes, seconds)
    }
}
