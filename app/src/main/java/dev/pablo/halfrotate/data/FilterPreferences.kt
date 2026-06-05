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
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.pablo.halfrotate.rotation.OrientationPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "halfrotate_prefs",
)

class FilterPreferences(context: Context) {
    private val dataStore = context.applicationContext.dataStore

    val filterEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_FILTER_ENABLED] ?: false
    }

    val orientationPreset: Flow<OrientationPreset> = dataStore.data.map { prefs ->
        OrientationPreset.fromStored(prefs[KEY_ORIENTATION_PRESET])
    }

    val forceSystemAutoRotate: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_FORCE_SYSTEM_AUTO_ROTATE] ?: false
    }

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

    suspend fun setOrientationPreset(preset: OrientationPreset) {
        dataStore.edit { prefs ->
            prefs[KEY_ORIENTATION_PRESET] = preset.name
        }
    }

    suspend fun setForceSystemAutoRotate(force: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_FORCE_SYSTEM_AUTO_ROTATE] = force
        }
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

    companion object {
        private val KEY_FILTER_ENABLED = booleanPreferencesKey("filter_enabled")
        private val KEY_ORIENTATION_PRESET = stringPreferencesKey("orientation_preset")
        private val KEY_FORCE_SYSTEM_AUTO_ROTATE =
            booleanPreferencesKey("force_system_auto_rotate")
        private val KEY_SAVED_ACCELEROMETER = intPreferencesKey("saved_accelerometer_rotation")
        private val KEY_SYSTEM_AUTO_ROTATE_AT_ENABLE =
            booleanPreferencesKey("system_auto_rotate_at_enable")
    }
}
