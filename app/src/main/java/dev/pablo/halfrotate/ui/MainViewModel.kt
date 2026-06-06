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
import dev.pablo.halfrotate.R
import dev.pablo.halfrotate.rotation.AllowedRotations
import dev.pablo.halfrotate.rotation.RotationController
import dev.pablo.halfrotate.rotation.RotationLogic
import dev.pablo.halfrotate.rotation.RotationMonitor
import dev.pablo.halfrotate.service.FilterServiceManager
import dev.pablo.halfrotate.util.PermissionsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val appActive: Boolean = false,
    val serviceStartFailed: Boolean = false,
    val writeSettingsGranted: Boolean = false,
    val notificationGranted: Boolean = true,
    val autoRotateEnabled: Boolean = false,
    val currentRotation: Int = RotationLogic.ROTATION_PORTRAIT,
    val allowedRotations: AllowedRotations = AllowedRotations.Default,
    val forceSystemAutoRotate: Boolean = false,
    val systemAutoRotateAtEnable: Boolean = false,
    val showAbout: Boolean = false,
) {
    val pausedBecauseAutoRotateOff: Boolean =
        appActive && !forceSystemAutoRotate && !systemAutoRotateAtEnable

    val canEnableApp: Boolean =
        writeSettingsGranted && notificationGranted
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val controller = RotationController(context)
    private val prefs = (application as HalfRotateApp).filterPreferences

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var monitor: RotationMonitor? = null

    init {
        viewModelScope.launch {
            combine(
                prefs.allowedRotations,
                prefs.forceSystemAutoRotate,
                prefs.systemAutoRotateAtEnable,
            ) { allowed, force, atEnable ->
                ConfigSnapshot(allowed, force, atEnable)
            }.collect { snapshot ->
                _uiState.update { state ->
                    state.copy(
                        allowedRotations = snapshot.allowed,
                        forceSystemAutoRotate = snapshot.forceAutoRotate,
                        systemAutoRotateAtEnable = snapshot.systemAutoRotateAtEnable,
                    )
                }
            }
        }
    }

    fun onResume() {
        viewModelScope.launch {
            val sync = FilterServiceManager.syncRunningState(context)
            _uiState.update { state ->
                state.copy(
                    serviceStartFailed = sync == FilterServiceManager.SyncResult.StartFailed,
                )
            }
            updateStatus()
            startUiMonitor()
        }
    }

    fun onPause() {
        monitor?.stop()
        monitor = null
    }

    private fun startUiMonitor() {
        monitor?.stop()
        monitor = RotationMonitor(context) {
            val appActive = FilterServiceManager.isRunning(context)
            val rotation = if (appActive) {
                controller.getUserRotation()
            } else {
                controller.getDisplayRotation()
            }
            _uiState.update { state ->
                state.copy(
                    appActive = appActive,
                    currentRotation = rotation,
                    autoRotateEnabled = if (appActive) {
                        state.forceSystemAutoRotate || state.systemAutoRotateAtEnable
                    } else {
                        controller.isAutoRotateEnabled()
                    },
                )
            }
        }.also { it.start() }
    }

    fun updateStatus() {
        viewModelScope.launch {
            val allowed = prefs.allowedRotations.first()
            val force = prefs.forceSystemAutoRotate.first()
            val atEnable = prefs.systemAutoRotateAtEnable.first()
            val appActive = FilterServiceManager.isRunning(context)
            _uiState.update { current ->
                current.copy(
                    appActive = appActive,
                    writeSettingsGranted = PermissionsHelper.canWriteSettings(context),
                    notificationGranted = PermissionsHelper.hasNotificationPermission(context),
                    autoRotateEnabled = if (appActive) force || atEnable else controller.isAutoRotateEnabled(),
                    currentRotation = if (appActive) {
                        controller.getUserRotation()
                    } else {
                        controller.getDisplayRotation()
                    },
                    allowedRotations = allowed,
                    forceSystemAutoRotate = force,
                    systemAutoRotateAtEnable = atEnable,
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

    fun setRotationToggle(toggle: Int, enabled: Boolean) {
        viewModelScope.launch {
            prefs.setRotationToggle(toggle, enabled)
            FilterServiceManager.notifyConfigChanged(context)
        }
    }

    fun setForceSystemAutoRotate(force: Boolean) {
        viewModelScope.launch {
            prefs.setForceSystemAutoRotate(force)
            FilterServiceManager.notifyConfigChanged(context)
        }
    }

    fun showAbout(show: Boolean) {
        _uiState.update { it.copy(showAbout = show) }
    }

    fun rotationLabelRes(rotation: Int): Int = when (rotation) {
        RotationLogic.ROTATION_PORTRAIT -> R.string.rotation_0
        RotationLogic.ROTATION_LANDSCAPE -> R.string.rotation_90
        RotationLogic.ROTATION_REVERSE_PORTRAIT -> R.string.rotation_180
        RotationLogic.ROTATION_REVERSE_LANDSCAPE -> R.string.rotation_270
        else -> R.string.rotation_unknown
    }

    private data class ConfigSnapshot(
        val allowed: AllowedRotations,
        val forceAutoRotate: Boolean,
        val systemAutoRotateAtEnable: Boolean,
    )
}
