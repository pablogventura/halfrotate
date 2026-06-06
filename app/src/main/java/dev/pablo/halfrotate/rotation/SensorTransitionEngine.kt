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

sealed class TransitionResult {
    data object None : TransitionResult()

    data class Apply(val rotation: Int) : TransitionResult()
}

class SensorTransitionEngine {
    private var allowed: Set<Int> = AllowedRotations.Default.toSet()
    private var sensorActive: Boolean = false
    private var currentRotation: Int = RotationLogic.ROTATION_PORTRAIT
    private var lastAllowedRotation: Int = RotationLogic.ROTATION_PORTRAIT
    private var pendingRotation: Int? = null
    private var pendingSinceMs: Long = 0L

    fun start(
        initialRotation: Int,
        allowed: Set<Int>,
        sensorActive: Boolean,
    ) {
        this.allowed = allowed
        this.sensorActive = sensorActive
        currentRotation = initialRotation
        val initialBucket = RotationLogic.displayRotationToSensorBucket(initialRotation)
        if (RotationLogic.isAllowed(initialBucket, allowed)) {
            lastAllowedRotation = initialRotation
        }
        pendingRotation = null
    }

    fun onSensorDegrees(degrees: Int, nowMs: Long): TransitionResult {
        if (!sensorActive) return TransitionResult.None
        if (degrees == RotationLogic.ORIENTATION_UNKNOWN) return TransitionResult.None

        val targetBucket = RotationLogic.targetRotationForSensor(
            degrees,
            currentRotation,
            allowed,
            lastAllowedRotation,
        )
        val targetDisplay = RotationLogic.sensorBucketToDisplayRotation(targetBucket)
        if (targetDisplay == currentRotation) {
            pendingRotation = null
            return TransitionResult.None
        }
        if (pendingRotation != targetDisplay) {
            pendingRotation = targetDisplay
            pendingSinceMs = nowMs
            return TransitionResult.None
        }
        if (!RotationLogic.shouldApplyTransition(
                currentRotation,
                pendingRotation,
                pendingSinceMs,
                nowMs,
            )
        ) {
            return TransitionResult.None
        }
        return applyRotation(targetBucket)
    }

    fun onAllowedChanged(
        allowed: Set<Int>,
        sensorActive: Boolean,
    ): TransitionResult {
        this.allowed = allowed
        this.sensorActive = sensorActive
        return snapIfNeeded()
    }

    fun snapIfNeeded(): TransitionResult {
        val currentBucket = RotationLogic.displayRotationToSensorBucket(currentRotation)
        if (RotationLogic.isAllowed(currentBucket, allowed)) {
            return TransitionResult.None
        }
        val targetBucket = RotationLogic.correctionForDisallowed(
            currentBucket,
            allowed,
            lastAllowedRotation,
        )
        return applyRotation(targetBucket)
    }

    private fun applyRotation(sensorBucket: Int): TransitionResult.Apply {
        val displayRotation = RotationLogic.sensorBucketToDisplayRotation(sensorBucket)
        currentRotation = displayRotation
        lastAllowedRotation = displayRotation
        pendingRotation = null
        return TransitionResult.Apply(displayRotation)
    }
}
