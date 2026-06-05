/*
 * HalfRotate — limit auto-rotation to portrait and landscape.
 * Copyright (C) 2026 Pablo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package dev.pablo.halfrotate.tile

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dev.pablo.halfrotate.R
import dev.pablo.halfrotate.service.FilterServiceManager
import dev.pablo.halfrotate.ui.MainActivity
import dev.pablo.halfrotate.util.PermissionsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import dev.pablo.halfrotate.HalfRotateApp

class RotationTileService : TileService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        if (!PermissionsHelper.canWriteSettings(this)) {
            unlockAndRun {
                launchMainActivity()
            }
            return
        }

        scope.launch {
            val app = application as HalfRotateApp
            val enabled = app.filterPreferences.filterEnabled.first()
            if (enabled) {
                FilterServiceManager.disableFilter(this@RotationTileService)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (!PermissionsHelper.hasNotificationPermission(this@RotationTileService)) {
                        unlockAndRun {
                            launchMainActivity()
                        }
                        return@launch
                    }
                }
                FilterServiceManager.enableFilter(this@RotationTileService)
            }
            updateTile()
        }
    }

    private fun updateTile() {
        scope.launch {
            val qsTile = qsTile ?: return@launch
            val enabled = (application as HalfRotateApp).filterPreferences.filterEnabled.first()
            qsTile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            qsTile.subtitle = getString(
                if (enabled) R.string.tile_subtitle_on else R.string.tile_subtitle_off,
            )
            qsTile.updateTile()
        }
    }

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pending = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            startActivityAndCollapse(pending)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
