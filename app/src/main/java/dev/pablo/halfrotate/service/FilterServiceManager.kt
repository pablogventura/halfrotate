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

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dev.pablo.halfrotate.HalfRotateApp
import dev.pablo.halfrotate.data.FilterPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object FilterServiceManager {
    fun start(context: Context) {
        val intent = Intent(context, RotationGuardService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stop(context: Context) {
        context.stopService(Intent(context, RotationGuardService::class.java))
    }

    fun isRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        return manager.getRunningServices(Int.MAX_VALUE).any {
            it.service.className == RotationGuardService::class.java.name
        }
    }

    fun enableFilter(context: Context) {
        val app = context.applicationContext as HalfRotateApp
        runBlocking {
            val prefs = app.filterPreferences
            if (!prefs.autoRotateInitialized.first()) {
                val controller = dev.pablo.halfrotate.rotation.RotationController(context)
                if (controller.canWriteSettings()) {
                    controller.enableAutoRotate()
                    prefs.setAutoRotateInitialized(true)
                }
            }
            prefs.setFilterEnabled(true)
        }
        start(context)
    }

    fun disableFilter(context: Context) {
        val app = context.applicationContext as HalfRotateApp
        runBlocking {
            app.filterPreferences.setFilterEnabled(false)
        }
        stop(context)
    }

    suspend fun isFilterEnabled(context: Context): Boolean {
        val app = context.applicationContext as HalfRotateApp
        return app.filterPreferences.filterEnabled.first()
    }
}
