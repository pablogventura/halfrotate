/*
 * HalfRotate — limit auto-rotation to portrait and landscape.
 * Copyright (C) 2026 Pablo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package dev.pablo.halfrotate

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import dev.pablo.halfrotate.rotation.AllowedRotations
import dev.pablo.halfrotate.service.FilterServiceManager
import dev.pablo.halfrotate.testing.TestFilterReceiver

object TestFilterHarness {
    private const val PACKAGE = "dev.pablo.halfrotate"
    private const val POLL_MS = 100L
    private const val TIMEOUT_MS = 5_000L

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    fun grantPermissions() {
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        uiAutomation.executeShellCommand(
            "appops set $PACKAGE WRITE_SETTINGS allow",
        ).close()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            uiAutomation.executeShellCommand(
                "pm grant $PACKAGE android.permission.POST_NOTIFICATIONS",
            ).close()
        }
    }

    fun enableFilter() {
        sendBroadcast(TestFilterReceiver.ACTION_ENABLE)
        waitForServiceRunning(running = true)
    }

    fun disableFilter() {
        sendBroadcast(TestFilterReceiver.ACTION_DISABLE)
        waitForServiceRunning(running = false)
    }

    fun setAllowedRotations(allowed: AllowedRotations) {
        context.sendBroadcast(
            Intent(TestFilterReceiver.ACTION_SET_ALLOWED).apply {
                setPackage(PACKAGE)
                putExtra(TestFilterReceiver.EXTRA_PORTRAIT, allowed.portrait)
                putExtra(TestFilterReceiver.EXTRA_LANDSCAPE, allowed.landscape)
                putExtra(TestFilterReceiver.EXTRA_REVERSE_PORTRAIT, allowed.reversePortrait)
                putExtra(TestFilterReceiver.EXTRA_REVERSE_LANDSCAPE, allowed.reverseLandscape)
            },
        )
    }

    fun setForceAutoRotate(force: Boolean) {
        context.sendBroadcast(
            Intent(TestFilterReceiver.ACTION_SET_FORCE_AUTO_ROTATE).apply {
                setPackage(PACKAGE)
                putExtra(TestFilterReceiver.EXTRA_FORCE, force)
            },
        )
    }

    fun waitForServiceRunning(running: Boolean) {
        val deadline = System.currentTimeMillis() + TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            if (FilterServiceManager.isRunning(context) == running) return
            Thread.sleep(POLL_MS)
        }
        error("Service running=$running not reached within ${TIMEOUT_MS}ms")
    }

    fun waitUntil(timeoutMs: Long = TIMEOUT_MS, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            Thread.sleep(POLL_MS)
        }
        error("Condition not met within ${timeoutMs}ms")
    }

    private fun sendBroadcast(action: String) {
        context.sendBroadcast(
            Intent(action).setPackage(PACKAGE),
        )
    }
}
