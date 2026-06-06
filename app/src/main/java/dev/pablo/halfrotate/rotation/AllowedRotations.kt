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

data class AllowedRotations(
    val portrait: Boolean = true,
    val landscape: Boolean = true,
    val reversePortrait: Boolean = false,
    val reverseLandscape: Boolean = false,
) {
    fun toSet(): Set<Int> = buildSet {
        if (portrait) add(RotationLogic.ROTATION_PORTRAIT)
        if (landscape) add(RotationLogic.ROTATION_LANDSCAPE)
        if (reversePortrait) add(RotationLogic.ROTATION_REVERSE_PORTRAIT)
        if (reverseLandscape) add(RotationLogic.ROTATION_REVERSE_LANDSCAPE)
    }

    fun isEmpty(): Boolean =
        !portrait && !landscape && !reversePortrait && !reverseLandscape

    fun enabledCount(): Int =
        listOf(portrait, landscape, reversePortrait, reverseLandscape).count { it }

    companion object {
        val Default = AllowedRotations()

        const val TOGGLE_PORTRAIT = 0
        const val TOGGLE_LANDSCAPE = 1
        const val TOGGLE_REVERSE_PORTRAIT = 2
        const val TOGGLE_REVERSE_LANDSCAPE = 3

        fun fromLegacyPreset(name: String?): AllowedRotations = when (name) {
            "PortraitOnly" -> AllowedRotations(
                portrait = true,
                landscape = false,
                reverseLandscape = false,
            )
            "LandscapeOnly" -> AllowedRotations(
                portrait = false,
                landscape = true,
                reverseLandscape = false,
            )
            "AllExceptUpsideDown" -> AllowedRotations(
                portrait = true,
                landscape = true,
                reverseLandscape = true,
            )
            else -> Default
        }

        fun fromBundledHorizontal(
            portrait: Boolean,
            horizontal: Boolean,
            upsideDown: Boolean,
        ): AllowedRotations = AllowedRotations(
            portrait = portrait,
            landscape = horizontal,
            reversePortrait = upsideDown,
            reverseLandscape = horizontal,
        )
    }
}
