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

enum class HorizontalMode {
    LANDSCAPE_90,
    REVERSE_LANDSCAPE_270,
    ;

    fun userRotation(): Int = when (this) {
        LANDSCAPE_90 -> RotationLogic.ROTATION_LANDSCAPE
        REVERSE_LANDSCAPE_270 -> RotationLogic.ROTATION_REVERSE_LANDSCAPE
    }
}

data class AllowedRotations(
    val horizontalMode: HorizontalMode = HorizontalMode.LANDSCAPE_90,
) {
    val portrait: Boolean get() = true
    val reversePortrait: Boolean get() = false
    val landscape: Boolean get() = horizontalMode == HorizontalMode.LANDSCAPE_90
    val reverseLandscape: Boolean get() = horizontalMode == HorizontalMode.REVERSE_LANDSCAPE_270

    fun toSet(): Set<Int> = setOf(
        RotationLogic.ROTATION_PORTRAIT,
        horizontalMode.userRotation(),
    )

    companion object {
        val Default = AllowedRotations()

        fun fromLegacyFlags(
            portrait: Boolean,
            landscape: Boolean,
            reversePortrait: Boolean,
            reverseLandscape: Boolean,
        ): AllowedRotations {
            val horizontal = when {
                landscape && !reverseLandscape -> HorizontalMode.LANDSCAPE_90
                !landscape && reverseLandscape -> HorizontalMode.REVERSE_LANDSCAPE_270
                else -> HorizontalMode.LANDSCAPE_90
            }
            return AllowedRotations(horizontalMode = horizontal)
        }

        fun fromLegacyPreset(name: String?): AllowedRotations = when (name) {
            "LandscapeOnly" -> AllowedRotations(HorizontalMode.LANDSCAPE_90)
            else -> Default
        }

        fun fromBundledHorizontal(
            portrait: Boolean,
            horizontal: Boolean,
            upsideDown: Boolean,
        ): AllowedRotations {
            if (!horizontal) {
                return if (upsideDown) {
                    AllowedRotations(HorizontalMode.REVERSE_LANDSCAPE_270)
                } else {
                    Default
                }
            }
            return Default
        }
    }
}
