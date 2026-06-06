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

    fun isAutoRotateEnabled(): Boolean = getAccelerometerRotation() == 1

    fun getAccelerometerRotation(): Int =
        Settings.System.getInt(
            context.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
            0,
        )

    fun setAccelerometerRotation(enabled: Boolean) {
        if (!canWriteSettings()) return
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
            if (enabled) 1 else 0,
        )
    }

    fun lockSystemAutoRotate() {
        setAccelerometerRotation(false)
    }

    fun restoreSystemAutoRotate(savedValue: Int) {
        setAccelerometerRotation(savedValue == 1)
    }

    fun unlockSystemAutoRotate(restore: Int) {
        restoreSystemAutoRotate(restore)
    }

    fun getUserRotation(): Int =
        Settings.System.getInt(
            context.contentResolver,
            Settings.System.USER_ROTATION,
            Surface.ROTATION_0,
        )

    fun setUserRotation(rotation: Int) {
        if (!canWriteSettings()) return
        require(rotation in Surface.ROTATION_0..Surface.ROTATION_270) {
            "Invalid rotation $rotation"
        }
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.USER_ROTATION,
            rotation,
        )
    }

    fun getDisplayRotation(): Int {
        return try {
            val display = context.display ?: return getUserRotation()
            display.rotation
        } catch (_: UnsupportedOperationException) {
            getUserRotation()
        }
    }
}
