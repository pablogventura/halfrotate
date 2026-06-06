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
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class RotationPolicyMatrixTest(
    private val mode: HorizontalMode,
    private val physicalBucket: Int,
    private val expected: Int,
) {

    private val allowed = AllowedRotations(mode).toSet()

    @Test
    fun correctionForDisallowed() {
        assertEquals(
            expected,
            RotationLogic.correctionForDisallowed(physicalBucket, allowed),
        )
    }

    @Test
    fun targetRotationForSensor() {
        val degrees = physicalBucket * RotationLogic.ORIENTATION_DEGREES_90
        assertEquals(
            expected,
            RotationLogic.targetRotationForSensor(
                degrees = degrees,
                currentRotation = RotationLogic.ROTATION_PORTRAIT,
                allowed = allowed,
                lastAllowed = null,
            ),
        )
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "mode={0} physical={1} -> {2}")
        fun data(): Collection<Array<Any>> = listOf(
            row(HorizontalMode.LANDSCAPE_90, 0, 0),
            row(HorizontalMode.LANDSCAPE_90, 1, 1),
            row(HorizontalMode.LANDSCAPE_90, 2, 0),
            row(HorizontalMode.LANDSCAPE_90, 3, 0),
            row(HorizontalMode.REVERSE_LANDSCAPE_270, 0, 0),
            row(HorizontalMode.REVERSE_LANDSCAPE_270, 1, 0),
            row(HorizontalMode.REVERSE_LANDSCAPE_270, 2, 0),
            row(HorizontalMode.REVERSE_LANDSCAPE_270, 3, 3),
        )

        private fun row(mode: HorizontalMode, physical: Int, expected: Int): Array<Any> =
            arrayOf(mode, physical, expected)
    }
}

class RotationLogicTest {

    private val defaultAllowed = AllowedRotations.Default.toSet()

    @Test
    fun defaultAllowed_portraitAndLandscape90Only() {
        assertEquals(setOf(0, 1), defaultAllowed)
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
    fun targetRotationForSensor_maps270toPortraitByDefault() {
        assertEquals(
            0,
            RotationLogic.targetRotationForSensor(
                270,
                currentRotation = 1,
                allowed = defaultAllowed,
                lastAllowed = 1,
            ),
        )
    }

    @Test
    fun targetRotationForSensor_maps90toPortraitWhenOnly270Allowed() {
        val allowed = AllowedRotations(HorizontalMode.REVERSE_LANDSCAPE_270).toSet()
        assertEquals(
            0,
            RotationLogic.targetRotationForSensor(
                90,
                currentRotation = 0,
                allowed = allowed,
                lastAllowed = 0,
            ),
        )
    }
}
