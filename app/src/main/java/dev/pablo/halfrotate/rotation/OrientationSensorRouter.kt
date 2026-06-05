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
import android.view.Surface

class OrientationSensorRouter(
    context: Context,
    private val onApplyRotation: (Int) -> Unit,
) {
    private val appContext = context.applicationContext

    private var preset: OrientationPreset = OrientationPreset.PortraitAndLandscape
    private var sensorActive: Boolean = false
    private var currentLockedRotation: Int = Surface.ROTATION_0
    private var lastAllowedRotation: Int = Surface.ROTATION_0
    private var pendingRotation: Int? = null
    private var pendingSinceMs: Long = 0L

    private val listener = object : OrientationEventListener(appContext) {
        override fun onOrientationChanged(orientation: Int) {
            if (!sensorActive) return
            if (orientation == RotationLogic.ORIENTATION_UNKNOWN) return
            val bucket = RotationLogic.sensorDegreesToBucket(
                orientation,
                currentLockedRotation,
            )
            if (!RotationLogic.isAllowed(bucket, preset)) return
            if (bucket == currentLockedRotation) {
                pendingRotation = null
                return
            }
            val now = System.currentTimeMillis()
            if (pendingRotation != bucket) {
                pendingRotation = bucket
                pendingSinceMs = now
                return
            }
            if (RotationLogic.shouldApplyTransition(
                    currentLockedRotation,
                    pendingRotation,
                    pendingSinceMs,
                    now,
                )
            ) {
                applyRotation(bucket)
            }
        }
    }

    fun start(
        preset: OrientationPreset,
        sensorActive: Boolean,
        initialRotation: Int,
    ) {
        this.preset = preset
        this.sensorActive = sensorActive
        currentLockedRotation = initialRotation
        if (RotationLogic.isAllowed(initialRotation, preset)) {
            lastAllowedRotation = initialRotation
        }
        pendingRotation = null
        if (listener.canDetectOrientation()) {
            listener.enable()
        }
    }

    fun stop() {
        listener.disable()
        pendingRotation = null
        sensorActive = false
    }

    fun updateConfig(
        preset: OrientationPreset,
        sensorActive: Boolean,
    ) {
        onPresetChanged(preset, sensorActive)
    }

    fun onPresetChanged(
        preset: OrientationPreset,
        sensorActive: Boolean = this.sensorActive,
    ) {
        this.preset = preset
        this.sensorActive = sensorActive
        if (!RotationLogic.isAllowed(currentLockedRotation, preset)) {
            val target = RotationLogic.nearestAllowed(
                currentLockedRotation,
                preset,
                lastAllowedRotation,
            )
            applyRotation(target)
        }
    }

    fun snapIfNeeded(preset: OrientationPreset) {
        this.preset = preset
        if (!RotationLogic.isAllowed(currentLockedRotation, preset)) {
            val target = RotationLogic.nearestAllowed(
                currentLockedRotation,
                preset,
                lastAllowedRotation,
            )
            applyRotation(target)
        }
    }

    private fun applyRotation(rotation: Int) {
        val userRotation = RotationLogic.bucketToUserRotation(rotation)
        currentLockedRotation = userRotation
        lastAllowedRotation = userRotation
        pendingRotation = null
        onApplyRotation(userRotation)
    }
}
