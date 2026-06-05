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

enum class OrientationPreset {
    PortraitAndLandscape,
    PortraitOnly,
    LandscapeOnly,
    AllExceptUpsideDown,
    ;

    companion object {
        fun fromStored(value: String?): OrientationPreset =
            entries.find { it.name == value } ?: PortraitAndLandscape
    }
}
