/*
 * HalfRotate — limit auto-rotation to portrait and landscape.
 * Copyright (C) 2026 Pablo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package dev.pablo.halfrotate.testing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.pablo.halfrotate.HalfRotateApp
import dev.pablo.halfrotate.rotation.AllowedRotations
import dev.pablo.halfrotate.service.FilterServiceManager
import kotlinx.coroutines.runBlocking

class TestFilterReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_ENABLE -> FilterServiceManager.enableFilter(context)
            ACTION_DISABLE -> FilterServiceManager.disableFilter(context)
            ACTION_SET_ALLOWED -> {
                val allowed = AllowedRotations(
                    portrait = intent.getBooleanExtra(EXTRA_PORTRAIT, true),
                    landscape = intent.getBooleanExtra(EXTRA_LANDSCAPE, true),
                    reversePortrait = intent.getBooleanExtra(EXTRA_REVERSE_PORTRAIT, false),
                    reverseLandscape = intent.getBooleanExtra(EXTRA_REVERSE_LANDSCAPE, false),
                )
                val app = context.applicationContext as HalfRotateApp
                runBlocking {
                    app.filterPreferences.setAllowedRotations(allowed)
                }
                FilterServiceManager.notifyConfigChanged(context)
            }
            ACTION_SET_FORCE_AUTO_ROTATE -> {
                val force = intent.getBooleanExtra(EXTRA_FORCE, false)
                val app = context.applicationContext as HalfRotateApp
                runBlocking {
                    app.filterPreferences.setForceSystemAutoRotate(force)
                }
                FilterServiceManager.notifyConfigChanged(context)
            }
        }
    }

    companion object {
        const val ACTION_ENABLE = "dev.pablo.halfrotate.test.ENABLE_FILTER"
        const val ACTION_DISABLE = "dev.pablo.halfrotate.test.DISABLE_FILTER"
        const val ACTION_SET_ALLOWED = "dev.pablo.halfrotate.test.SET_ALLOWED"
        const val ACTION_SET_FORCE_AUTO_ROTATE = "dev.pablo.halfrotate.test.SET_FORCE_AUTO_ROTATE"
        const val EXTRA_PORTRAIT = "portrait"
        const val EXTRA_LANDSCAPE = "landscape"
        const val EXTRA_REVERSE_PORTRAIT = "reverse_portrait"
        const val EXTRA_REVERSE_LANDSCAPE = "reverse_landscape"
        const val EXTRA_FORCE = "force"
    }
}
