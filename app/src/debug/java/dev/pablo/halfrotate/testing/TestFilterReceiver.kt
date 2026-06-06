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
import dev.pablo.halfrotate.rotation.HorizontalMode
import dev.pablo.halfrotate.service.FilterServiceManager
import kotlinx.coroutines.runBlocking

class TestFilterReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_ENABLE -> FilterServiceManager.enableFilter(context)
            ACTION_DISABLE -> FilterServiceManager.disableFilter(context)
            ACTION_SET_HORIZONTAL_MODE -> {
                val modeName = intent.getStringExtra(EXTRA_HORIZONTAL_MODE) ?: return
                val mode = HorizontalMode.valueOf(modeName)
                val app = context.applicationContext as HalfRotateApp
                runBlocking {
                    app.filterPreferences.setHorizontalMode(mode)
                }
                FilterServiceManager.notifyConfigChanged(context)
            }
        }
    }

    companion object {
        const val ACTION_ENABLE = "dev.pablo.halfrotate.test.ENABLE_FILTER"
        const val ACTION_DISABLE = "dev.pablo.halfrotate.test.DISABLE_FILTER"
        const val ACTION_SET_HORIZONTAL_MODE = "dev.pablo.halfrotate.test.SET_HORIZONTAL_MODE"
        const val EXTRA_HORIZONTAL_MODE = "horizontal_mode"
    }
}
