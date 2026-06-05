/*
 * HalfRotate — limit auto-rotation to portrait and landscape.
 * Copyright (C) 2026 Pablo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package dev.pablo.halfrotate.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dev.pablo.halfrotate.HalfRotateApp
import dev.pablo.halfrotate.R
import dev.pablo.halfrotate.rotation.RotationController
import dev.pablo.halfrotate.rotation.RotationMonitor
import dev.pablo.halfrotate.ui.MainActivity
import dev.pablo.halfrotate.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RotationGuardService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var controller: RotationController
    private var monitor: RotationMonitor? = null

    override fun onCreate() {
        super.onCreate()
        controller = RotationController(this)
        NotificationHelper.createChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DISABLE -> {
                scope.launch {
                    FilterServiceManager.disableFilter(this@RotationGuardService)
                }
                return START_NOT_STICKY
            }
        }

        scope.launch {
            val enabled = (application as HalfRotateApp).filterPreferences.filterEnabled.first()
            if (!enabled || !controller.canWriteSettings()) {
                stopSelf()
                return@launch
            }
            startForegroundInternal()
            startMonitoring()
        }

        return START_STICKY
    }

    private fun startForegroundInternal() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NotificationHelper.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NotificationHelper.NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val disable = PendingIntent.getService(
            this,
            1,
            Intent(this, RotationGuardService::class.java).setAction(ACTION_DISABLE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tile_rotation)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(
                R.drawable.ic_tile_rotation,
                getString(R.string.notification_action_disable),
                disable,
            )
            .build()
    }

    private fun startMonitoring() {
        monitor?.stop()
        monitor = RotationMonitor(this) {
            controller.applyCorrectionIfNeeded()
        }.also { it.start() }
        controller.applyCorrectionIfNeeded()
    }

    override fun onDestroy() {
        monitor?.stop()
        monitor = null
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_DISABLE = "dev.pablo.halfrotate.action.DISABLE_FILTER"
    }
}
