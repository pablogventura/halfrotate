/*
 * HalfRotate — limit auto-rotation to portrait and landscape.
 * Copyright (C) 2026 Pablo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package dev.pablo.halfrotate.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dev.pablo.halfrotate.HalfRotateApp
import dev.pablo.halfrotate.rotation.RotationController
import dev.pablo.halfrotate.util.PermissionsHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

object FilterServiceManager {
    private const val START_RETRY_DELAY_MS = 400L

    fun start(context: Context) {
        val intent = Intent(context, RotationGuardService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stop(context: Context) {
        context.stopService(Intent(context, RotationGuardService::class.java))
    }

    fun isRunning(context: Context): Boolean = ServiceState.isRunning

    fun enableFilter(context: Context) {
        val app = context.applicationContext as HalfRotateApp
        kotlinx.coroutines.runBlocking {
            val prefs = app.filterPreferences
            val controller = RotationController(context)
            if (prefs.savedAccelerometerRotation.first() == null && controller.canWriteSettings()) {
                val current = controller.getAccelerometerRotation()
                prefs.saveAccelerometerState(
                    wasEnabled = current == 1,
                    rotationValue = current,
                )
            }
            prefs.setFilterEnabled(true)
        }
        start(context)
    }

    fun disableFilter(context: Context) {
        val app = context.applicationContext as HalfRotateApp
        kotlinx.coroutines.runBlocking {
            app.filterPreferences.setFilterEnabled(false)
        }
        stop(context)
    }

    fun notifyConfigChanged(context: Context) {
        if (!isRunning(context)) return
        val intent = Intent(context, RotationGuardService::class.java)
            .setAction(RotationGuardService.ACTION_RELOAD_CONFIG)
        ContextCompat.startForegroundService(context, intent)
    }

    suspend fun isFilterEnabled(context: Context): Boolean {
        val app = context.applicationContext as HalfRotateApp
        return app.filterPreferences.filterEnabled.first()
    }

    /** Align persisted preference with the real foreground service state. */
    suspend fun syncRunningState(context: Context): SyncResult {
        val app = context.applicationContext as HalfRotateApp
        val wantsEnabled = app.filterPreferences.filterEnabled.first()
        val running = isRunning(context)

        if (wantsEnabled && !running) {
            if (!PermissionsHelper.canWriteSettings(context)) {
                app.filterPreferences.setFilterEnabled(false)
                return SyncResult.PreferenceCleared
            }
            start(context)
            delay(START_RETRY_DELAY_MS)
            if (!isRunning(context)) {
                app.filterPreferences.setFilterEnabled(false)
                return SyncResult.StartFailed
            }
            return SyncResult.Restarted
        }

        if (!wantsEnabled && running) {
            stop(context)
            delay(START_RETRY_DELAY_MS)
            return SyncResult.Stopped
        }

        return SyncResult.AlreadyInSync
    }

    enum class SyncResult {
        AlreadyInSync,
        Restarted,
        Stopped,
        StartFailed,
        PreferenceCleared,
    }
}
