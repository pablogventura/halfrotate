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

class RotationLogicTest {

    @Test
    fun allowedRotations() {
        assertEquals(true, RotationLogic.isAllowed(0))
        assertEquals(true, RotationLogic.isAllowed(1))
        assertEquals(false, RotationLogic.isAllowed(2))
        assertEquals(false, RotationLogic.isAllowed(3))
    }

    @Test
    fun correctIfNeeded_maps180to0() {
        assertEquals(0, RotationLogic.correctIfNeeded(2))
    }

    @Test
    fun correctIfNeeded_maps270to90() {
        assertEquals(1, RotationLogic.correctIfNeeded(3))
    }

    @Test
    fun correctIfNeeded_noChangeForAllowed() {
        assertNull(RotationLogic.correctIfNeeded(0))
        assertNull(RotationLogic.correctIfNeeded(1))
    }
}
