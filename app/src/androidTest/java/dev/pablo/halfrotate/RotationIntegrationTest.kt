/*
 * HalfRotate — limit auto-rotation to portrait and landscape.
 * Copyright (C) 2026 Pablo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package dev.pablo.halfrotate

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.pablo.halfrotate.rotation.AllowedRotations
import dev.pablo.halfrotate.rotation.RotationController
import dev.pablo.halfrotate.rotation.RotationLogic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RotationIntegrationTest {

    private lateinit var controller: RotationController
    private var savedAccelerometer: Int = 0
    private var savedUserRotation: Int = RotationLogic.ROTATION_PORTRAIT

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        controller = RotationController(context)
        TestFilterHarness.grantPermissions()

        savedAccelerometer = controller.getAccelerometerRotation()
        savedUserRotation = controller.getUserRotation()
        controller.setAccelerometerRotation(true)
    }

    @After
    fun tearDown() {
        TestFilterHarness.disableFilter()
        controller.setAccelerometerRotation(savedAccelerometer == 1)
        controller.setUserRotation(savedUserRotation)
    }

    @Test
    fun lockOnEnable_setsAccelerometerRotationOff() {
        TestFilterHarness.enableFilter()

        assertEquals(0, controller.getAccelerometerRotation())
    }

    @Test
    fun restoreOnDisable_restoresPreviousAutoRotate() {
        controller.setAccelerometerRotation(true)
        TestFilterHarness.enableFilter()
        TestFilterHarness.disableFilter()

        assertEquals(1, controller.getAccelerometerRotation())
    }

    @Test
    fun portraitOnlySnap_correctsLandscapeToPortrait() {
        controller.setUserRotation(RotationLogic.ROTATION_LANDSCAPE)
        TestFilterHarness.setAllowedRotations(
            AllowedRotations(portrait = true, landscape = false, reverseLandscape = false),
        )
        TestFilterHarness.enableFilter()

        TestFilterHarness.waitUntil {
            controller.getUserRotation() == RotationLogic.ROTATION_PORTRAIT
        }

        assertEquals(RotationLogic.ROTATION_PORTRAIT, controller.getUserRotation())
    }

    @Test
    fun sensorPausedWhenForceOff_serviceRunsWithoutChangingRotation() {
        controller.setAccelerometerRotation(false)
        TestFilterHarness.setForceAutoRotate(false)
        TestFilterHarness.enableFilter()

        val rotationBefore = controller.getUserRotation()
        Thread.sleep(500L)

        assertEquals(rotationBefore, controller.getUserRotation())
    }
}
