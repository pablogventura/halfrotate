/*
 * HalfRotate — limit auto-rotation to portrait and landscape.
 * Copyright (C) 2026 Pablo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package dev.pablo.halfrotate.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.pablo.halfrotate.HalfRotateApp
import dev.pablo.halfrotate.rotation.HorizontalMode
import dev.pablo.halfrotate.service.FilterServiceManager
import dev.pablo.halfrotate.util.PermissionsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val appActive: Boolean = false,
    val serviceStartFailed: Boolean = false,
    val writeSettingsGranted: Boolean = false,
    val notificationGranted: Boolean = true,
    val horizontalMode: HorizontalMode = HorizontalMode.LANDSCAPE_90,
) {
    val canEnableApp: Boolean =
        writeSettingsGranted && notificationGranted
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val prefs = (application as HalfRotateApp).filterPreferences

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.horizontalMode.collect { mode ->
                _uiState.update { it.copy(horizontalMode = mode) }
            }
        }
    }

    fun onResume() {
        viewModelScope.launch {
            val sync = FilterServiceManager.syncRunningState(context)
            updateStatus(
                serviceStartFailed = sync == FilterServiceManager.SyncResult.StartFailed,
            )
        }
    }

    fun updateStatus(serviceStartFailed: Boolean? = null) {
        viewModelScope.launch {
            val mode = prefs.horizontalMode.first()
            val appActive = FilterServiceManager.isRunning(context)
            _uiState.update { current ->
                current.copy(
                    appActive = appActive,
                    writeSettingsGranted = PermissionsHelper.canWriteSettings(context),
                    notificationGranted = PermissionsHelper.hasNotificationPermission(context),
                    horizontalMode = mode,
                    serviceStartFailed = serviceStartFailed ?: current.serviceStartFailed,
                )
            }
        }
    }

    fun toggleApp(enable: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(serviceStartFailed = false) }
            if (enable) {
                FilterServiceManager.enableFilter(context)
                val sync = FilterServiceManager.syncRunningState(context)
                _uiState.update { state ->
                    state.copy(
                        serviceStartFailed = sync == FilterServiceManager.SyncResult.StartFailed,
                    )
                }
            } else {
                FilterServiceManager.disableFilter(context)
            }
            updateStatus()
        }
    }

    fun setHorizontalMode(mode: HorizontalMode) {
        viewModelScope.launch {
            prefs.setHorizontalMode(mode)
            FilterServiceManager.notifyConfigChanged(context)
        }
    }
}
