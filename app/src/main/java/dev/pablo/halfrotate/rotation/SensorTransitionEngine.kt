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
        if (RotationLogic.isAllowed(initialRotation, allowed)) {
            lastAllowedRotation = initialRotation
        }
        pendingRotation = null
    }

    fun onSensorDegrees(degrees: Int, nowMs: Long): TransitionResult {
        if (!sensorActive) return TransitionResult.None
        if (degrees == RotationLogic.ORIENTATION_UNKNOWN) return TransitionResult.None

        val target = RotationLogic.targetRotationForSensor(
            degrees,
            currentRotation,
            allowed,
            lastAllowedRotation,
        )
        if (target == currentRotation) {
            pendingRotation = null
            return TransitionResult.None
        }
        if (pendingRotation != target) {
            pendingRotation = target
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
        return applyRotation(target)
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
        if (RotationLogic.isAllowed(currentRotation, allowed)) {
            return TransitionResult.None
        }
        val target = RotationLogic.correctionForDisallowed(
            currentRotation,
            allowed,
            lastAllowedRotation,
        )
        return applyRotation(target)
    }

    private fun applyRotation(rotation: Int): TransitionResult.Apply {
        val userRotation = RotationLogic.bucketToUserRotation(rotation)
        currentRotation = userRotation
        lastAllowedRotation = userRotation
        pendingRotation = null
        return TransitionResult.Apply(userRotation)
    }
}
