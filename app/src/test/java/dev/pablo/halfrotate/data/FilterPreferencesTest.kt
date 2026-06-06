/*
 * HalfRotate — limit auto-rotation to portrait and landscape.
 * Copyright (C) 2026 Pablo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package dev.pablo.halfrotate.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import dev.pablo.halfrotate.rotation.AllowedRotations
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FilterPreferencesTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createPrefs(): FilterPreferences {
        val file = tempFolder.newFile("test_prefs.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { file },
        )
        return FilterPreferences.forTest(dataStore)
    }

    @Test
    fun defaults() = runTest {
        val prefs = createPrefs()

        assertFalse(prefs.filterEnabled.first())
        assertEquals(AllowedRotations.Default, prefs.allowedRotations.first())
        assertFalse(prefs.forceSystemAutoRotate.first())
        assertNull(prefs.savedAccelerometerRotation.first())
        assertFalse(prefs.systemAutoRotateAtEnable.first())
    }

    @Test
    fun allowedRotationsPersistIndependently() = runTest {
        val prefs = createPrefs()

        prefs.setAllowedRotations(
            AllowedRotations(
                portrait = true,
                landscape = false,
                reverseLandscape = true,
            ),
        )

        assertEquals(
            AllowedRotations(
                portrait = true,
                landscape = false,
                reverseLandscape = true,
            ),
            prefs.allowedRotations.first(),
        )
    }

    @Test
    fun cannotDisableLastRotation() = runTest {
        val prefs = createPrefs()

        prefs.setAllowedRotations(AllowedRotations(portrait = true, landscape = false))
        prefs.setRotationToggle(AllowedRotations.TOGGLE_PORTRAIT, enabled = false)

        assertEquals(
            AllowedRotations(portrait = true, landscape = false),
            prefs.allowedRotations.first(),
        )
    }
}
