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

object RotationLogic {
    const val ROTATION_PORTRAIT = 0
    const val ROTATION_LANDSCAPE = 1
    const val ROTATION_REVERSE_PORTRAIT = 2
    const val ROTATION_REVERSE_LANDSCAPE = 3

    fun isAllowed(rotation: Int): Boolean =
        rotation == ROTATION_PORTRAIT || rotation == ROTATION_LANDSCAPE

    /**
     * Returns the corrected rotation, or null if no correction is needed.
     */
    fun correctIfNeeded(rotation: Int): Int? = when (rotation) {
        ROTATION_REVERSE_PORTRAIT -> ROTATION_PORTRAIT
        ROTATION_REVERSE_LANDSCAPE -> ROTATION_LANDSCAPE
        else -> null
    }
}
