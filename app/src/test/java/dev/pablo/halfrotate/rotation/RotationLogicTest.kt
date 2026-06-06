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

    private val defaultAllowed = AllowedRotations.Default.toSet()
    private val portraitOnly = AllowedRotations(portrait = true, landscape = false).toSet()
    private val reverseLandscapeOnly = AllowedRotations(
        portrait = false,
        landscape = false,
        reverseLandscape = true,
    ).toSet()

    @Test
    fun defaultAllowed_portraitAndLandscape90Only() {
        assertEquals(setOf(0, 1), defaultAllowed)
    }

    @Test
    fun correction_270to90_whenOnly90Allowed() {
        assertEquals(1, RotationLogic.correctionForDisallowed(3, defaultAllowed, null))
    }

    @Test
    fun correction_90to270_whenOnly270Allowed() {
        assertEquals(3, RotationLogic.correctionForDisallowed(1, reverseLandscapeOnly, null))
    }

    @Test
    fun correction_180toPortrait() {
        assertEquals(0, RotationLogic.correctionForDisallowed(2, defaultAllowed, null))
    }

    @Test
    fun correction_bothHorizontalsAllowed_noCrossCorrection() {
        val both = AllowedRotations(landscape = true, reverseLandscape = true).toSet()
        assertEquals(1, RotationLogic.correctionForDisallowed(1, both, null))
        assertEquals(3, RotationLogic.correctionForDisallowed(3, both, null))
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
    fun targetRotationForSensor_maps270to90ByDefault() {
        assertEquals(
            1,
            RotationLogic.targetRotationForSensor(
                270,
                currentRotation = 1,
                allowed = defaultAllowed,
                lastAllowed = 1,
            ),
        )
    }

    @Test
    fun targetRotationForSensor_maps90to270WhenOnly270Allowed() {
        assertEquals(
            3,
            RotationLogic.targetRotationForSensor(
                90,
                currentRotation = 3,
                allowed = reverseLandscapeOnly,
                lastAllowed = 3,
            ),
        )
    }
}
