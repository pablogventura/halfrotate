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

import kotlin.math.abs
import kotlin.math.min

object RotationLogic {
    const val ROTATION_PORTRAIT = 0
    const val ROTATION_LANDSCAPE = 1
    const val ROTATION_REVERSE_PORTRAIT = 2
    const val ROTATION_REVERSE_LANDSCAPE = 3

    const val ORIENTATION_UNKNOWN = -1
    const val ORIENTATION_DEGREES_0 = 0
    const val ORIENTATION_DEGREES_90 = 90
    const val ORIENTATION_DEGREES_180 = 180
    const val ORIENTATION_DEGREES_270 = 270

    fun isAllowed(rotation: Int, allowed: Set<Int>): Boolean = rotation in allowed

    fun circularDistance(a: Int, b: Int): Int {
        val diff = abs(a - b)
        return min(diff, 4 - diff)
    }

    fun nearestAllowed(
        rotation: Int,
        allowed: Set<Int>,
        lastAllowed: Int?,
    ): Int = correctionForDisallowed(rotation, allowed, lastAllowed)

    /**
     * Maps a disallowed rotation to an allowed one.
     *
     * Policy:
     * - 90° and 270° never substitute for each other when one is disabled.
     * - A disabled horizontal snaps to portrait (0°) when portrait is allowed.
     * - Otherwise pick the nearest allowed bucket on the ring; tie-break with [lastAllowed].
     */
    fun correctionForDisallowed(
        rotation: Int,
        allowed: Set<Int>,
        lastAllowed: Int? = null,
    ): Int {
        if (rotation in allowed) return rotation
        if (allowed.isEmpty()) return ROTATION_PORTRAIT

        if (rotation.isHorizontal() && ROTATION_PORTRAIT in allowed) {
            return ROTATION_PORTRAIT
        }

        return allowed.minWith(
            compareBy<Int>({ circularDistance(rotation, it) })
                .thenBy { if (lastAllowed != null) circularDistance(it, lastAllowed) else it }
                .thenBy { it },
        )
    }

    private fun Int.isHorizontal(): Boolean =
        this == ROTATION_LANDSCAPE || this == ROTATION_REVERSE_LANDSCAPE

    fun orientationEventToRotation(orientation: Int): Int? {
        if (orientation == ORIENTATION_UNKNOWN || orientation !in 0..359) return null
        return sensorDegreesToBucket(orientation, currentBucket = null)
    }

    /** Maps physical angle (0–359°) to rotation bucket 0/1/2/3. */
    fun sensorDegreesToBucket(degrees: Int, currentBucket: Int? = null): Int {
        val normalized = normalizeDegrees(degrees)
        if (currentBucket == null) {
            return baseBucket(normalized)
        }
        if (isWithinBucketHysteresis(normalized, currentBucket)) {
            return currentBucket
        }
        return baseBucket(normalized)
    }

    fun bucketToUserRotation(bucket: Int): Int = bucket

    fun targetRotationForSensor(
        degrees: Int,
        currentRotation: Int,
        allowed: Set<Int>,
        lastAllowed: Int?,
    ): Int {
        val bucket = sensorDegreesToBucket(degrees, currentRotation)
        return correctionForDisallowed(bucket, allowed, lastAllowed)
    }

    private fun normalizeDegrees(degrees: Int): Int = ((degrees % 360) + 360) % 360

    private fun baseBucket(degrees: Int): Int = when {
        degrees >= 315 || degrees < 45 -> ROTATION_PORTRAIT
        degrees < 135 -> ROTATION_LANDSCAPE
        degrees < 225 -> ROTATION_REVERSE_PORTRAIT
        else -> ROTATION_REVERSE_LANDSCAPE
    }

    private fun isWithinBucketHysteresis(degrees: Int, bucket: Int): Boolean {
        val center = bucket * ORIENTATION_DEGREES_90
        val distance = angularDistanceDegrees(degrees, center)
        return distance <= (SECTOR_HALF_WIDTH - HYSTERESIS_DEGREES)
    }

    private fun angularDistanceDegrees(a: Int, b: Int): Int {
        val diff = abs(a - b)
        return min(diff, 360 - diff)
    }

    fun shouldApplyTransition(
        currentRotation: Int,
        pendingRotation: Int?,
        pendingSinceMs: Long,
        nowMs: Long,
        stabilityMs: Long = STABILITY_MS,
    ): Boolean {
        if (pendingRotation == null || pendingRotation == currentRotation) return false
        return nowMs - pendingSinceMs >= stabilityMs
    }

    const val STABILITY_MS = 300L
    const val HYSTERESIS_DEGREES = 15
    private const val SECTOR_HALF_WIDTH = 45
}
