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
import androidx.datastore.preferences.preferencesDataStore
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

    val autoRotateInitialized: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_AUTO_ROTATE_INITIALIZED] ?: false
    }

    suspend fun setFilterEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_FILTER_ENABLED] = enabled
        }
    }

    suspend fun setAutoRotateInitialized(initialized: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_AUTO_ROTATE_INITIALIZED] = initialized
        }
    }

    companion object {
        private val KEY_FILTER_ENABLED = booleanPreferencesKey("filter_enabled")
        private val KEY_AUTO_ROTATE_INITIALIZED =
            booleanPreferencesKey("auto_rotate_initialized")
    }
}
