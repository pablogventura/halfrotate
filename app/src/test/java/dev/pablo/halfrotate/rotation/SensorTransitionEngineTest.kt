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
import org.junit.Assert.assertTrue
import org.junit.Test

class SensorTransitionEngineTest {

    private val defaultAllowed = AllowedRotations.Default.toSet()
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

    @Test
    fun disallowed270_snapsToPortraitAfterStability() {
        val engine = SensorTransitionEngine()
        engine.start(
            initialRotation = RotationLogic.ROTATION_LANDSCAPE,
            allowed = defaultAllowed,
            sensorActive = true,
        )

        assertTrue(engine.onSensorDegrees(270, nowMs = 0L) is TransitionResult.None)
        val result = engine.onSensorDegrees(270, nowMs = 400L)
        assertEquals(TransitionResult.Apply(RotationLogic.ROTATION_PORTRAIT), result)
    }

    @Test
    fun disallowed90_snapsToPortraitWhenPortraitAnd270Allowed() {
        val engine = SensorTransitionEngine()
        engine.start(
            initialRotation = RotationLogic.ROTATION_REVERSE_LANDSCAPE,
            allowed = portraitAnd270,
            sensorActive = true,
        )

        engine.onSensorDegrees(90, nowMs = 0L)
        val result = engine.onSensorDegrees(90, nowMs = 400L)
        assertEquals(TransitionResult.Apply(RotationLogic.ROTATION_PORTRAIT), result)
    }

    @Test
    fun disallowed90_snapsTo270WhenOnly270Allowed() {
        val engine = SensorTransitionEngine()
        engine.start(
            initialRotation = RotationLogic.ROTATION_PORTRAIT,
            allowed = reverseLandscapeOnly,
            sensorActive = true,
        )

        engine.onSensorDegrees(90, nowMs = 0L)
        val result = engine.onSensorDegrees(90, nowMs = 400L)
        assertEquals(TransitionResult.Apply(RotationLogic.ROTATION_REVERSE_LANDSCAPE), result)
    }

    @Test
    fun bothHorizontalsAllowed_keeps270() {
        val both = AllowedRotations(landscape = true, reverseLandscape = true).toSet()
        val engine = SensorTransitionEngine()
        engine.start(
            initialRotation = RotationLogic.ROTATION_PORTRAIT,
            allowed = both,
            sensorActive = true,
        )

        engine.onSensorDegrees(270, nowMs = 0L)
        val result = engine.onSensorDegrees(270, nowMs = 400L)
        assertEquals(TransitionResult.Apply(RotationLogic.ROTATION_REVERSE_LANDSCAPE), result)
    }

    @Test
    fun allowedChange_snapsWhenDisallowed() {
        val engine = SensorTransitionEngine()
        engine.start(
            initialRotation = RotationLogic.ROTATION_LANDSCAPE,
            allowed = defaultAllowed,
            sensorActive = true,
        )

        val result = engine.onAllowedChanged(
            allowed = AllowedRotations(portrait = true, landscape = false).toSet(),
            sensorActive = true,
        )

        assertEquals(TransitionResult.Apply(RotationLogic.ROTATION_PORTRAIT), result)
    }
}
