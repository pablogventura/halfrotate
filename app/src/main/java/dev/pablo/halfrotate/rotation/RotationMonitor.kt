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
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Display

class RotationMonitor(
    context: Context,
    private val onRotationChanged: (Int) -> Unit,
) {
    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val displayManager =
        appContext.getSystemService(DisplayManager::class.java)

    private var debounceRunnable: Runnable? = null

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit

        override fun onDisplayRemoved(displayId: Int) = Unit

        override fun onDisplayChanged(displayId: Int) {
            if (displayId == Display.DEFAULT_DISPLAY) {
                scheduleNotify()
            }
        }
    }

    private val settingsObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            scheduleNotify()
        }
    }

    fun start() {
        displayManager.registerDisplayListener(displayListener, handler)
        appContext.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.USER_ROTATION),
            false,
            settingsObserver,
        )
        appContext.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
            false,
            settingsObserver,
        )
        scheduleNotify()
    }

    fun stop() {
        debounceRunnable?.let(handler::removeCallbacks)
        debounceRunnable = null
        displayManager.unregisterDisplayListener(displayListener)
        appContext.contentResolver.unregisterContentObserver(settingsObserver)
    }

    private fun scheduleNotify() {
        debounceRunnable?.let(handler::removeCallbacks)
        debounceRunnable = Runnable {
            val controller = RotationController(appContext)
            onRotationChanged(controller.getDisplayRotation())
        }.also { handler.postDelayed(it, DEBOUNCE_MS) }
    }

    companion object {
        private const val DEBOUNCE_MS = 150L
    }
}
