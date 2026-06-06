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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dev.pablo.halfrotate.rotation.AllowedRotations
import dev.pablo.halfrotate.rotation.HorizontalMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FilterPreferencesTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createPrefs(fileName: String = "test_prefs.preferences_pb"): FilterPreferences {
        val file = tempFolder.newFile(fileName)
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { file })
        return FilterPreferences.forTest(dataStore)
    }

    @Test
    fun defaults() = runTest {
        val prefs = createPrefs()

        assertFalse(prefs.filterEnabled.first())
        assertEquals(AllowedRotations.Default, prefs.allowedRotations.first())
        assertEquals(HorizontalMode.LANDSCAPE_90, prefs.horizontalMode.first())
        assertNull(prefs.savedAccelerometerRotation.first())
        assertFalse(prefs.systemAutoRotateAtEnable.first())
    }

    @Test
    fun horizontalModePersists() = runTest {
        val prefs = createPrefs()

        prefs.setHorizontalMode(HorizontalMode.REVERSE_LANDSCAPE_270)

        assertEquals(
            AllowedRotations(HorizontalMode.REVERSE_LANDSCAPE_270),
            prefs.allowedRotations.first(),
        )
        assertEquals(setOf(0, 3), prefs.allowedRotations.first().toSet())
    }

    private fun createStore(fileName: String) =
        PreferenceDataStoreFactory.create(produceFile = { File(tempFolder.root, fileName) })

    @Test
    fun migratesLegacyBothOffToLandscape90() = runTest {
        val store = createStore("legacy_off_off.preferences_pb")
        store.edit { prefs ->
            prefs[KEY_ALLOW_PORTRAIT] = false
            prefs[KEY_ALLOW_LANDSCAPE] = false
            prefs[KEY_ALLOW_REVERSE_PORTRAIT] = false
            prefs[KEY_ALLOW_REVERSE_LANDSCAPE] = false
        }
        val legacyPrefs = FilterPreferences.forTest(store)

        assertEquals(
            AllowedRotations(HorizontalMode.LANDSCAPE_90),
            legacyPrefs.allowedRotations.first(),
        )
    }

    @Test
    fun migratesLegacyOnly270ToReverseLandscape270() = runTest {
        val store = createStore("legacy_270.preferences_pb")
        store.edit { prefs ->
            prefs[KEY_ALLOW_PORTRAIT] = true
            prefs[KEY_ALLOW_LANDSCAPE] = false
            prefs[KEY_ALLOW_REVERSE_PORTRAIT] = false
            prefs[KEY_ALLOW_REVERSE_LANDSCAPE] = true
        }
        val legacyPrefs = FilterPreferences.forTest(store)

        assertEquals(
            AllowedRotations(HorizontalMode.REVERSE_LANDSCAPE_270),
            legacyPrefs.allowedRotations.first(),
        )
    }

    companion object {
        private val KEY_ALLOW_PORTRAIT = booleanPreferencesKey("allow_portrait")
        private val KEY_ALLOW_LANDSCAPE = booleanPreferencesKey("allow_landscape")
        private val KEY_ALLOW_REVERSE_PORTRAIT = booleanPreferencesKey("allow_reverse_portrait")
        private val KEY_ALLOW_REVERSE_LANDSCAPE = booleanPreferencesKey("allow_reverse_landscape")
    }
}
