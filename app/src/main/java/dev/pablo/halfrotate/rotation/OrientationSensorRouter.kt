/*
 * HalfRotate — limit auto-rotation to portrait and landscape.
 * Copyright (C) 2026 Pablo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package dev.pablo.halfrotate.rotation

import android.content.Context
import android.view.OrientationEventListener

class OrientationSensorRouter(
    context: Context,
    private val onApplyRotation: (Int) -> Unit,
) {
    private val appContext = context.applicationContext
    private val engine = SensorTransitionEngine()

    private val listener = object : OrientationEventListener(appContext) {
        override fun onOrientationChanged(orientation: Int) {
            val result = engine.onSensorDegrees(orientation, System.currentTimeMillis())
            if (result is TransitionResult.Apply) {
                onApplyRotation(result.rotation)
            }
        }
    }

    fun start(
        allowed: Set<Int>,
        sensorActive: Boolean,
        initialRotation: Int,
    ) {
        engine.start(initialRotation, allowed, sensorActive)
        if (listener.canDetectOrientation()) {
            listener.enable()
        }
        applyResult(engine.snapIfNeeded())
    }

    fun stop() {
        listener.disable()
    }

    fun updateConfig(
        allowed: Set<Int>,
        sensorActive: Boolean,
    ) {
        applyResult(engine.onAllowedChanged(allowed, sensorActive))
    }

    private fun applyResult(result: TransitionResult) {
        if (result is TransitionResult.Apply) {
            onApplyRotation(result.rotation)
        }
    }
}
