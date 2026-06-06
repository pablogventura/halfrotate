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
class RotationLogicCorrectionTest(
    private val allowed: Set<Int>,
    private val forbidden: Int,
    private val expected: Int,
) {

    @Test
    fun correctionForDisallowed() {
        assertEquals(
            expected,
            RotationLogic.correctionForDisallowed(forbidden, allowed, lastAllowed = null),
        )
    }

    companion object {
        private val portraitAnd90 = AllowedRotations.Default.toSet()
        private val portraitAnd270 = AllowedRotations(
            portrait = true,
            landscape = false,
            reverseLandscape = true,
        ).toSet()
        private val reverseLandscapeOnly = AllowedRotations(
            portrait = false,
            landscape = false,
            reverseLandscape = true,
        ).toSet()
        private val landscape90Only = AllowedRotations(
            portrait = false,
            landscape = true,
        ).toSet()
        private val bothHorizontals = AllowedRotations(
            landscape = true,
            reverseLandscape = true,
        ).toSet()

        @JvmStatic
        @Parameterized.Parameters(name = "allowed={0} forbidden={1} -> {2}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(portraitAnd90, 3, 0),
            arrayOf(portraitAnd90, 2, 1),
            arrayOf(portraitAnd270, 1, 0),
            arrayOf(reverseLandscapeOnly, 1, 3),
            arrayOf(landscape90Only, 3, 1),
            arrayOf(bothHorizontals, 1, 1),
            arrayOf(bothHorizontals, 3, 3),
        )
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
    fun targetRotationForSensor_maps90toPortraitWhenOnly270AllowedWithPortrait() {
        val allowed = AllowedRotations(
            portrait = true,
            landscape = false,
            reverseLandscape = true,
        ).toSet()
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

    @Test
    fun circularDistance() {
        assertEquals(1, RotationLogic.circularDistance(0, 1))
        assertEquals(1, RotationLogic.circularDistance(0, 3))
        assertEquals(2, RotationLogic.circularDistance(0, 2))
    }
}
