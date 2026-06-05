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
import android.provider.Settings
import android.view.Surface

class RotationController(private val context: Context) {

    fun canWriteSettings(): Boolean = Settings.System.canWrite(context)

    fun isAutoRotateEnabled(): Boolean =
        Settings.System.getInt(
            context.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
            0,
        ) == 1

    fun enableAutoRotate() {
        if (!canWriteSettings()) return
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
            1,
        )
    }

    fun getUserRotation(): Int =
        Settings.System.getInt(
            context.contentResolver,
            Settings.System.USER_ROTATION,
            Surface.ROTATION_0,
        )

    fun setUserRotation(rotation: Int) {
        if (!canWriteSettings()) return
        require(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
            "HalfRotate only sets portrait or landscape"
        }
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.USER_ROTATION,
            rotation,
        )
    }

    fun getDisplayRotation(): Int {
        val display = context.display ?: return getUserRotation()
        return display.rotation
    }

    fun applyCorrectionIfNeeded(): Boolean {
        if (!canWriteSettings() || !isAutoRotateEnabled()) return false

        val current = getDisplayRotation()
        val corrected = RotationLogic.correctIfNeeded(current) ?: return false
        setUserRotation(corrected)
        return true
    }
}
