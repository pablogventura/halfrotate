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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RotationLogicTest {

    @Test
    fun portraitAndLandscape_allowedSet() {
        val allowed = RotationLogic.allowedRotations(OrientationPreset.PortraitAndLandscape)
        assertEquals(setOf(0, 1), allowed)
    }

    @Test
    fun portraitOnly_allowedSet() {
        assertEquals(setOf(0), RotationLogic.allowedRotations(OrientationPreset.PortraitOnly))
    }

    @Test
    fun landscapeOnly_allowedSet() {
        assertEquals(setOf(1), RotationLogic.allowedRotations(OrientationPreset.LandscapeOnly))
    }

    @Test
    fun allExceptUpsideDown_allowedSet() {
        assertEquals(setOf(0, 1, 3), RotationLogic.allowedRotations(OrientationPreset.AllExceptUpsideDown))
    }

    @Test
    fun isAllowed_portraitAndLandscape() {
        assertTrue(RotationLogic.isAllowed(0, OrientationPreset.PortraitAndLandscape))
        assertTrue(RotationLogic.isAllowed(1, OrientationPreset.PortraitAndLandscape))
        assertFalse(RotationLogic.isAllowed(2, OrientationPreset.PortraitAndLandscape))
        assertFalse(RotationLogic.isAllowed(3, OrientationPreset.PortraitAndLandscape))
    }

    @Test
    fun nearestAllowed_180to0_portraitAndLandscape() {
        assertEquals(0, RotationLogic.nearestAllowed(2, OrientationPreset.PortraitAndLandscape, null))
    }

    @Test
    fun nearestAllowed_270to90_portraitAndLandscape() {
        assertEquals(1, RotationLogic.nearestAllowed(3, OrientationPreset.PortraitAndLandscape, null))
    }

    @Test
    fun nearestAllowed_tieBreakUsesLastAllowed() {
        // 180° is equidistant from 0 and 270 in AllExceptUpsideDown
        assertEquals(
            3,
            RotationLogic.nearestAllowed(2, OrientationPreset.AllExceptUpsideDown, lastAllowed = 3),
        )
        assertEquals(
            1,
            RotationLogic.nearestAllowed(2, OrientationPreset.AllExceptUpsideDown, lastAllowed = 0),
        )
    }

    @Test
    fun nearestAllowed_tieBreakUsesLowerIndexWhenNoHistory() {
        assertEquals(1, RotationLogic.nearestAllowed(2, OrientationPreset.AllExceptUpsideDown, null))
    }

    @Test
    fun nearestAllowed_portraitOnlySnapsToPortrait() {
        assertEquals(0, RotationLogic.nearestAllowed(1, OrientationPreset.PortraitOnly, null))
    }

    @Test
    fun orientationEventToRotation_mapsConstants() {
        assertNull(RotationLogic.orientationEventToRotation(RotationLogic.ORIENTATION_UNKNOWN))
        assertEquals(0, RotationLogic.orientationEventToRotation(RotationLogic.ORIENTATION_DEGREES_0))
        assertEquals(1, RotationLogic.orientationEventToRotation(RotationLogic.ORIENTATION_DEGREES_90))
        assertEquals(2, RotationLogic.orientationEventToRotation(RotationLogic.ORIENTATION_DEGREES_180))
        assertEquals(3, RotationLogic.orientationEventToRotation(RotationLogic.ORIENTATION_DEGREES_270))
    }

    @Test
    fun sensorDegreesToBucket_classifiesQuadrants() {
        assertEquals(0, RotationLogic.sensorDegreesToBucket(0))
        assertEquals(0, RotationLogic.sensorDegreesToBucket(359))
        assertEquals(1, RotationLogic.sensorDegreesToBucket(90))
        assertEquals(2, RotationLogic.sensorDegreesToBucket(180))
        assertEquals(3, RotationLogic.sensorDegreesToBucket(270))
    }

    @Test
    fun sensorDegreesToBucket_hysteresisKeepsPortraitNearBoundary() {
        assertEquals(0, RotationLogic.sensorDegreesToBucket(25, currentBucket = 0))
        assertEquals(0, RotationLogic.sensorDegreesToBucket(40, currentBucket = 0))
    }

    @Test
    fun sensorDegreesToBucket_hysteresisSwitchesAfterThreshold() {
        assertEquals(1, RotationLogic.sensorDegreesToBucket(62, currentBucket = 0))
    }

    @Test
    fun sensorDegreesToBucket_hysteresisKeepsLandscapeNearBoundary() {
        assertEquals(1, RotationLogic.sensorDegreesToBucket(58, currentBucket = 1))
    }

    @Test
    fun bucketToUserRotation_isIdentity() {
        assertEquals(2, RotationLogic.bucketToUserRotation(2))
    }

    @Test
    fun shouldApplyTransition_requiresStability() {
        assertFalse(
            RotationLogic.shouldApplyTransition(
                currentRotation = 0,
                pendingRotation = 1,
                pendingSinceMs = 1000L,
                nowMs = 1200L,
            ),
        )
        assertTrue(
            RotationLogic.shouldApplyTransition(
                currentRotation = 0,
                pendingRotation = 1,
                pendingSinceMs = 1000L,
                nowMs = 1300L,
            ),
        )
    }

    @Test
    fun shouldApplyTransition_sameRotationNeverApplies() {
        assertFalse(
            RotationLogic.shouldApplyTransition(
                currentRotation = 1,
                pendingRotation = 1,
                pendingSinceMs = 0L,
                nowMs = 10_000L,
            ),
        )
    }

    @Test
    fun circularDistance() {
        assertEquals(1, RotationLogic.circularDistance(0, 1))
        assertEquals(1, RotationLogic.circularDistance(0, 3))
        assertEquals(2, RotationLogic.circularDistance(0, 2))
    }
}
