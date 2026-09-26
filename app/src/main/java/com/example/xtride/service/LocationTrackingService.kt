package com.example.xtride.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.xtride.MainActivity
import com.example.xtride.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class LocationTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var notificationManager: NotificationManager

    private var serviceJob = Job()
    private var serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var timerJob: Job? = null

    private var lastLocation: Location? = null
    private var totalDistanceMeters = 0f
    private var secondsElapsed = 0L

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (TrackingManager.isPaused.value) return

            for (location in result.locations) {
                // Discard inaccurate GPS points (> 20 meters accuracy error)
                if (location.hasAccuracy() && location.accuracy > 20f) continue

                val prev = lastLocation
                if (prev != null) {
                    val distance = prev.distanceTo(location)
                    // Avoid GPS bounce when stationary
                    if (distance >= 1.5f) {
                        totalDistanceMeters += distance
                    }
                }
                lastLocation = location

                TrackingManager.addPoint(location.latitude, location.longitude)

                // Compute real-time pace, cadence, and calories
                val distanceKm = totalDistanceMeters / 1000f
                val paceSecondsPerKm = if (distanceKm > 0.03f && secondsElapsed > 0) {
                    (secondsElapsed / distanceKm).toInt()
                } else 0

                val speedMps = location.speed
                val currentType = TrackingManager.activityType.value
                val cadence = if (speedMps > 0.4f) {
                    when (currentType) {
                        "WALK" -> (100 + (speedMps * 12).toInt()).coerceIn(90, 135)
                        "HIKE" -> (95 + (speedMps * 10).toInt()).coerceIn(80, 125)
                        else -> (152 + (speedMps * 6.5).toInt()).coerceIn(145, 185) // RUN
                    }
                } else 0

                val calMultiplier = when (currentType) {
                    "WALK" -> 48f
                    "HIKE" -> 82f
                    else -> 70f
                }
                val calories = (distanceKm * calMultiplier).toInt()

                TrackingManager.updateMetrics(
                    distance = totalDistanceMeters,
                    speedMps = speedMps,
                    paceSecPerKm = paceSecondsPerKm,
                    cadence = cadence,
                    calories = calories
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val activityType = intent.getStringExtra(EXTRA_ACTIVITY_TYPE) ?: "RUN"
                startTracking(activityType)
            }
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTracking(activityType: String) {
        if (TrackingManager.isTracking.value) {
            startForegroundNotification()
            return
        }

        if (serviceJob.isCancelled || !serviceScope.isActive) {
            serviceJob = Job()
            serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
        }

        TrackingManager.reset()
        TrackingManager.setActivityType(activityType)
        TrackingManager.setTracking(true)
        TrackingManager.setPaused(false)
        totalDistanceMeters = 0f
        secondsElapsed = 0L
        lastLocation = null

        startForegroundNotification()
        startLocationUpdates()
        startTimer()
    }

    private fun pauseTracking() {
        TrackingManager.setPaused(true)
        updateNotification()
    }

    private fun resumeTracking() {
        TrackingManager.setPaused(false)
        updateNotification()
    }

    private fun stopTracking() {
        timerJob?.cancel()
        timerJob = null
        stopLocationUpdates()
        TrackingManager.setTracking(false)
        TrackingManager.setPaused(false)

        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                if (!TrackingManager.isPaused.value) {
                    secondsElapsed++
                    TrackingManager.setDuration(secondsElapsed)

                    // Re-calculate pace periodically
                    val distanceKm = totalDistanceMeters / 1000f
                    if (distanceKm > 0.03f) {
                        val pace = (secondsElapsed / distanceKm).toInt()
                        val currentType = TrackingManager.activityType.value
                        val calMultiplier = when (currentType) {
                            "WALK" -> 48f
                            "HIKE" -> 82f
                            else -> 70f
                        }
                        val calories = (distanceKm * calMultiplier).toInt()
                        TrackingManager.updateMetrics(
                            distance = totalDistanceMeters,
                            speedMps = TrackingManager.currentSpeedMps.value,
                            paceSecPerKm = pace,
                            cadence = TrackingManager.cadenceSpm.value,
                            calories = calories
                        )
                    }

                    // Update notification text every 2 seconds to reduce IPC overhead
                    if (secondsElapsed % 2L == 0L) {
                        updateNotification()
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!hasLocationPermissions()) return

        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
                .setMinUpdateIntervalMillis(1500L)
                .setMinUpdateDistanceMeters(2f)
                .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("LocationTrackingService", "Location permission missing: ${e.message}")
        }
    }

    private fun stopLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e("LocationTrackingService", "Failed to remove location updates: ${e.message}")
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startForegroundNotification() {
        try {
            val notification = buildNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("LocationTrackingService", "Failed to start foreground notification: ${e.message}")
        }
    }

    private fun updateNotification() {
        if (TrackingManager.isTracking.value) {
            try {
                notificationManager.notify(NOTIFICATION_ID, buildNotification())
            } catch (e: Exception) {
                Log.e("LocationTrackingService", "Failed to update notification: ${e.message}")
            }
        }
    }

    private fun buildNotification(): Notification {
        val distanceKm = totalDistanceMeters / 1000f
        val distanceValStr = String.format(Locale.US, "%.2f", distanceKm)
        val timeText = TrackingManager.formatDuration(secondsElapsed)
        val paceValStr = TrackingManager.formatPace(TrackingManager.currentPaceSecondsPerKm.value)
        val cadenceVal = TrackingManager.cadenceSpm.value
        val cadenceValStr = if (cadenceVal > 0) cadenceVal.toString() else "--"
        val isPaused = TrackingManager.isPaused.value
        val activityType = TrackingManager.activityType.value.uppercase()
        val activityName = when (activityType) {
            "WALK" -> "Outdoor Walk"
            "HIKE" -> "Outdoor Hike"
            else -> "Outdoor Run"
        }
        val paceLabel = when (activityType) {
            "WALK" -> "Walking"
            "HIKE" -> "Hiking"
            else -> "Running"
        }

        // Tapping notification body opens app to the Track screen
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_TAB", "TRACK")
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action toggle (Pause / Resume)
        val toggleIntent = Intent(this, LocationTrackingService::class.java).apply {
            action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Finish: opens app directly to the Track screen with stop/save confirmation dialog
        val finishIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_TAB", "TRACK")
            putExtra("SHOW_FINISH_DIALOG", true)
        }
        val finishPendingIntent = PendingIntent.getActivity(
            this,
            2,
            finishIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Custom Expanded RemoteViews
        val expandedViews = RemoteViews(packageName, R.layout.notification_tracking_expanded).apply {
            setTextViewText(R.id.tv_notification_title, "xtride • $activityName")
            setViewVisibility(R.id.tv_gps_active, if (isPaused) View.GONE else View.VISIBLE)
            setViewVisibility(R.id.tv_gps_paused, if (isPaused) View.VISIBLE else View.GONE)
            setTextViewText(R.id.btn_notification_pause, if (isPaused) "Resume" else "Pause")

            setTextViewText(R.id.tv_distance_value, distanceValStr)
            setTextViewText(R.id.tv_time_value, timeText)
            setTextViewText(R.id.tv_pace_label, paceLabel)
            setTextViewText(R.id.tv_pace_value, paceValStr)
            setTextViewText(R.id.tv_cadence_value, cadenceValStr)

            setOnClickPendingIntent(R.id.btn_notification_pause, togglePendingIntent)
            setOnClickPendingIntent(R.id.btn_notification_finish, finishPendingIntent)
            setOnClickPendingIntent(R.id.notification_root, openAppPendingIntent)
        }

        // Custom Collapsed RemoteViews
        val collapsedViews = RemoteViews(packageName, R.layout.notification_tracking_collapsed).apply {
            setTextViewText(R.id.tv_collapsed_title, "xtride • $activityName")
            setViewVisibility(R.id.tv_collapsed_gps_active, if (isPaused) View.GONE else View.VISIBLE)
            setViewVisibility(R.id.tv_collapsed_gps_paused, if (isPaused) View.VISIBLE else View.GONE)

            setTextViewText(R.id.tv_collapsed_distance, "$distanceValStr km")
            setTextViewText(R.id.tv_collapsed_time, timeText)
            setTextViewText(R.id.tv_collapsed_pace, "$paceValStr /km")

            setOnClickPendingIntent(R.id.notification_collapsed_root, openAppPendingIntent)
        }

        val statusText = if (isPaused) "PAUSED" else "TRACKING"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsedViews)
            .setCustomBigContentView(expandedViews)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setContentTitle("xtride • $activityName • $statusText")
            .setContentText("$distanceValStr km  |  $timeText  |  $paceValStr/km")
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Outdoor Activity GPS Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live activity stats including distance, time, and pace."
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        stopLocationUpdates()
    }

    companion object {
        const val CHANNEL_ID = "xtride_run_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START = "com.example.xtride.ACTION_START"
        const val ACTION_PAUSE = "com.example.xtride.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.xtride.ACTION_RESUME"
        const val ACTION_STOP = "com.example.xtride.ACTION_STOP"

        const val EXTRA_ACTIVITY_TYPE = "EXTRA_ACTIVITY_TYPE"
    }
}
