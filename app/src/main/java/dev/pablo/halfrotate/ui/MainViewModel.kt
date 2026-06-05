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
import dev.pablo.halfrotate.rotation.OrientationPreset
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
    val filterEnabled: Boolean = false,
    val serviceRunning: Boolean = false,
    val writeSettingsGranted: Boolean = false,
    val notificationGranted: Boolean = true,
    val autoRotateEnabled: Boolean = false,
    val currentRotation: Int = RotationLogic.ROTATION_PORTRAIT,
    val orientationPreset: OrientationPreset = OrientationPreset.PortraitAndLandscape,
    val forceSystemAutoRotate: Boolean = false,
    val systemAutoRotateAtEnable: Boolean = false,
    val showAbout: Boolean = false,
) {
    val pausedBecauseAutoRotateOff: Boolean =
        filterEnabled && !forceSystemAutoRotate && !systemAutoRotateAtEnable

    val canEnableFilter: Boolean =
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
                prefs.filterEnabled,
                prefs.orientationPreset,
                prefs.forceSystemAutoRotate,
                prefs.systemAutoRotateAtEnable,
            ) { enabled, preset, force, atEnable ->
                ConfigSnapshot(enabled, preset, force, atEnable)
            }.collect { snapshot ->
                _uiState.update { state ->
                    state.copy(
                        filterEnabled = snapshot.filterEnabled,
                        orientationPreset = snapshot.preset,
                        forceSystemAutoRotate = snapshot.forceAutoRotate,
                        systemAutoRotateAtEnable = snapshot.systemAutoRotateAtEnable,
                    )
                }
            }
        }
    }

    fun onResume() {
        updateStatus()
        startUiMonitor()
    }

    fun onPause() {
        monitor?.stop()
        monitor = null
    }

    private fun startUiMonitor() {
        monitor?.stop()
        monitor = RotationMonitor(context) { rotation ->
            _uiState.update { state ->
                state.copy(
                    currentRotation = rotation,
                    autoRotateEnabled = if (state.filterEnabled) {
                        state.forceSystemAutoRotate || state.systemAutoRotateAtEnable
                    } else {
                        controller.isAutoRotateEnabled()
                    },
                    serviceRunning = FilterServiceManager.isRunning(context),
                )
            }
        }.also { it.start() }
    }

    fun updateStatus(filterEnabled: Boolean? = null) {
        viewModelScope.launch {
            val enabled = filterEnabled ?: prefs.filterEnabled.first()
            val preset = prefs.orientationPreset.first()
            val force = prefs.forceSystemAutoRotate.first()
            val atEnable = prefs.systemAutoRotateAtEnable.first()
            _uiState.update { current ->
                current.copy(
                    filterEnabled = enabled,
                    serviceRunning = FilterServiceManager.isRunning(context),
                    writeSettingsGranted = PermissionsHelper.canWriteSettings(context),
                    notificationGranted = PermissionsHelper.hasNotificationPermission(context),
                    autoRotateEnabled = if (enabled) force || atEnable else controller.isAutoRotateEnabled(),
                    currentRotation = controller.getDisplayRotation(),
                    orientationPreset = preset,
                    forceSystemAutoRotate = force,
                    systemAutoRotateAtEnable = atEnable,
                )
            }
        }
    }

    fun toggleFilter(enable: Boolean) {
        viewModelScope.launch {
            if (enable) {
                FilterServiceManager.enableFilter(context)
            } else {
                FilterServiceManager.disableFilter(context)
            }
        }
    }

    fun setOrientationPreset(preset: OrientationPreset) {
        viewModelScope.launch {
            prefs.setOrientationPreset(preset)
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

    fun allowedSummaryRes(preset: OrientationPreset): Int = when (preset) {
        OrientationPreset.PortraitAndLandscape -> R.string.allowed_summary_portrait_landscape
        OrientationPreset.PortraitOnly -> R.string.allowed_summary_portrait_only
        OrientationPreset.LandscapeOnly -> R.string.allowed_summary_landscape_only
        OrientationPreset.AllExceptUpsideDown -> R.string.allowed_summary_no_upside_down
    }

    private data class ConfigSnapshot(
        val filterEnabled: Boolean,
        val preset: OrientationPreset,
        val forceAutoRotate: Boolean,
        val systemAutoRotateAtEnable: Boolean,
    )
}
