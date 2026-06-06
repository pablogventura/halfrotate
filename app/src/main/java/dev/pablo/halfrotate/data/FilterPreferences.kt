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

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.pablo.halfrotate.rotation.AllowedRotations
import dev.pablo.halfrotate.rotation.HorizontalMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "halfrotate_prefs",
)

class FilterPreferences private constructor(
    private val dataStore: DataStore<Preferences>,
) {
    constructor(context: Context) : this(context.applicationContext.dataStore)

    val filterEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_FILTER_ENABLED] ?: false
    }

    val allowedRotations: Flow<AllowedRotations> = dataStore.data.map { prefs ->
        readAllowedRotations(prefs)
    }

    val horizontalMode: Flow<HorizontalMode> = allowedRotations.map { it.horizontalMode }

    val savedAccelerometerRotation: Flow<Int?> = dataStore.data.map { prefs ->
        if (prefs.contains(KEY_SAVED_ACCELEROMETER)) {
            prefs[KEY_SAVED_ACCELEROMETER]
        } else {
            null
        }
    }

    val systemAutoRotateAtEnable: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_SYSTEM_AUTO_ROTATE_AT_ENABLE] ?: false
    }

    suspend fun setFilterEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_FILTER_ENABLED] = enabled
        }
    }

    suspend fun setHorizontalMode(mode: HorizontalMode) {
        dataStore.edit { prefs ->
            writeHorizontalMode(prefs, mode)
        }
    }

    suspend fun setAllowedRotations(allowed: AllowedRotations) {
        setHorizontalMode(allowed.horizontalMode)
    }

    suspend fun saveAccelerometerState(wasEnabled: Boolean, rotationValue: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_SAVED_ACCELEROMETER] = rotationValue
            prefs[KEY_SYSTEM_AUTO_ROTATE_AT_ENABLE] = wasEnabled
        }
    }

    suspend fun clearAccelerometerState() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_SAVED_ACCELEROMETER)
            prefs.remove(KEY_SYSTEM_AUTO_ROTATE_AT_ENABLE)
        }
    }

    internal companion object {
        fun forTest(dataStore: DataStore<Preferences>): FilterPreferences =
            FilterPreferences(dataStore)

        private val KEY_FILTER_ENABLED = booleanPreferencesKey("filter_enabled")
        private val KEY_ORIENTATION_PRESET = stringPreferencesKey("orientation_preset")
        private val KEY_HORIZONTAL_MODE = stringPreferencesKey("horizontal_mode")
        private val KEY_ALLOW_PORTRAIT = booleanPreferencesKey("allow_portrait")
        private val KEY_ALLOW_HORIZONTAL = booleanPreferencesKey("allow_horizontal")
        private val KEY_ALLOW_UPSIDE_DOWN = booleanPreferencesKey("allow_upside_down")
        private val KEY_ALLOW_LANDSCAPE = booleanPreferencesKey("allow_landscape")
        private val KEY_ALLOW_REVERSE_PORTRAIT = booleanPreferencesKey("allow_reverse_portrait")
        private val KEY_ALLOW_REVERSE_LANDSCAPE = booleanPreferencesKey("allow_reverse_landscape")
        private val KEY_SAVED_ACCELEROMETER = intPreferencesKey("saved_accelerometer_rotation")
        private val KEY_SYSTEM_AUTO_ROTATE_AT_ENABLE =
            booleanPreferencesKey("system_auto_rotate_at_enable")

        private fun readAllowedRotations(prefs: Preferences): AllowedRotations {
            prefs[KEY_HORIZONTAL_MODE]?.let { name ->
                return AllowedRotations(
                    horizontalMode = HorizontalMode.valueOf(name),
                )
            }
            return when {
                prefs.contains(KEY_ALLOW_LANDSCAPE) || prefs.contains(KEY_ALLOW_REVERSE_LANDSCAPE) ->
                    AllowedRotations.fromLegacyFlags(
                        portrait = prefs[KEY_ALLOW_PORTRAIT] ?: true,
                        landscape = prefs[KEY_ALLOW_LANDSCAPE] ?: true,
                        reversePortrait = prefs[KEY_ALLOW_REVERSE_PORTRAIT] ?: false,
                        reverseLandscape = prefs[KEY_ALLOW_REVERSE_LANDSCAPE] ?: false,
                    )
                prefs.contains(KEY_ALLOW_HORIZONTAL) -> AllowedRotations.fromBundledHorizontal(
                    portrait = prefs[KEY_ALLOW_PORTRAIT] ?: true,
                    horizontal = prefs[KEY_ALLOW_HORIZONTAL] ?: true,
                    upsideDown = prefs[KEY_ALLOW_UPSIDE_DOWN] ?: false,
                )
                else -> AllowedRotations.fromLegacyPreset(prefs[KEY_ORIENTATION_PRESET])
            }
        }

        private fun writeHorizontalMode(prefs: MutablePreferences, mode: HorizontalMode) {
            prefs[KEY_HORIZONTAL_MODE] = mode.name
            prefs[KEY_ALLOW_PORTRAIT] = true
            prefs[KEY_ALLOW_LANDSCAPE] = mode == HorizontalMode.LANDSCAPE_90
            prefs[KEY_ALLOW_REVERSE_PORTRAIT] = false
            prefs[KEY_ALLOW_REVERSE_LANDSCAPE] = mode == HorizontalMode.REVERSE_LANDSCAPE_270
            prefs.remove(KEY_ALLOW_HORIZONTAL)
            prefs.remove(KEY_ALLOW_UPSIDE_DOWN)
            prefs.remove(KEY_ORIENTATION_PRESET)
        }
    }
}
