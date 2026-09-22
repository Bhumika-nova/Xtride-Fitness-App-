package com.example.xtride.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
class StepSensorManager(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    val isSensorAvailable: Boolean
        get() = stepCounterSensor != null

    //Emits raw hardware step counts as a reactive stream

    fun getRawStepCountFlow(): Flow<Int> = callbackFlow {
        if (stepCounterSensor == null) {
            close()
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val rawSteps = it.values[0].toInt()
                    trySend(rawSteps)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(
            listener,
            stepCounterSensor,
            SensorManager.SENSOR_DELAY_UI
        )
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}