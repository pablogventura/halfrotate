/*
 * HalfRotate — limit auto-rotation to portrait and landscape.
 * Copyright (C) 2026 Pablo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package dev.pablo.halfrotate.rotation

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings

/**
 * Keeps [Settings.System.ACCELEROMETER_ROTATION] locked off while HalfRotate runs,
 * so the MIUI/HyperOS rotation toggle cannot re-enable system auto-rotate.
 */
class SystemRotationLockGuard(
    context: Context,
    private val controller: RotationController,
) {
    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())

    private val observer = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            relockIfNeeded()
        }
    }

    fun start() {
        controller.lockSystemAutoRotate()
        appContext.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
            false,
            observer,
        )
    }

    fun stop() {
        appContext.contentResolver.unregisterContentObserver(observer)
    }

    private fun relockIfNeeded() {
        if (controller.getAccelerometerRotation() != 0) {
            controller.lockSystemAutoRotate()
        }
    }
}
