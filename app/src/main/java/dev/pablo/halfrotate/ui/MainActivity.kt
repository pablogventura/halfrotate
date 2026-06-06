/*
 * HalfRotate — limit auto-rotation to portrait and landscape.
 * Copyright (C) 2026 Pablo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package dev.pablo.halfrotate.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pablo.halfrotate.R
import dev.pablo.halfrotate.rotation.AllowedRotations
import dev.pablo.halfrotate.rotation.RotationLogic
import dev.pablo.halfrotate.ui.theme.HalfRotateTheme
import dev.pablo.halfrotate.util.PermissionsHelper

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        viewModel.updateStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HalfRotateTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                if (uiState.showAbout) {
                    AboutScreen(onBack = { viewModel.showAbout(false) })
                } else {
                    MainScreen(
                        uiState = uiState,
                        rotationLabelRes = viewModel.rotationLabelRes(uiState.currentRotation),
                        onGrantWriteSettings = {
                            startActivity(PermissionsHelper.writeSettingsIntent(this))
                        },
                        onGrantNotifications = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS,
                                )
                            }
                        },
                        onRotationToggled = viewModel::setRotationToggle,
                        onForceAutoRotateChanged = viewModel::setForceSystemAutoRotate,
                        onToggleApp = viewModel::toggleApp,
                        onBatteryOptimization = {
                            startActivity(PermissionsHelper.batteryOptimizationIntent(this))
                        },
                        onAutostart = {
                            startActivity(PermissionsHelper.autostartIntent(this))
                        },
                        onAbout = { viewModel.showAbout(true) },
                        onPrivacy = {
                            startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_url))),
                            )
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        viewModel.onPause()
        super.onPause()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    uiState: MainUiState,
    rotationLabelRes: Int,
    onGrantWriteSettings: () -> Unit,
    onGrantNotifications: () -> Unit,
    onRotationToggled: (toggle: Int, enabled: Boolean) -> Unit,
    onForceAutoRotateChanged: (Boolean) -> Unit,
    onToggleApp: (Boolean) -> Unit,
    onBatteryOptimization: () -> Unit,
    onAutostart: () -> Unit,
    onAbout: () -> Unit,
    onPrivacy: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onAbout) {
                        Icon(Icons.Default.Info, contentDescription = stringResource(R.string.about_title))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.app_subtitle),
                style = MaterialTheme.typography.bodyLarge,
            )

            if (uiState.serviceStartFailed) {
                Banner(text = stringResource(R.string.service_start_failed_banner))
            }

            if (uiState.pausedBecauseAutoRotateOff) {
                Banner(text = stringResource(R.string.paused_banner))
            }

            PermissionCard(
                title = stringResource(R.string.permission_write_settings_title),
                description = stringResource(R.string.permission_write_settings_desc),
                granted = uiState.writeSettingsGranted,
                grantedLabel = stringResource(R.string.permission_write_settings_granted),
                deniedLabel = stringResource(R.string.permission_write_settings_denied),
                actionLabel = stringResource(R.string.permission_write_settings_grant),
                onAction = onGrantWriteSettings,
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionCard(
                    title = stringResource(R.string.notification_permission_title),
                    description = stringResource(R.string.notification_permission_desc),
                    granted = uiState.notificationGranted,
                    grantedLabel = stringResource(R.string.permission_write_settings_granted),
                    deniedLabel = stringResource(R.string.permission_write_settings_denied),
                    actionLabel = stringResource(R.string.notification_permission_grant),
                    onAction = onGrantNotifications,
                )
            }

            ConfigurationSection(
                uiState = uiState,
                onRotationToggled = onRotationToggled,
                onForceAutoRotateChanged = onForceAutoRotateChanged,
            )

            StatusCard(uiState = uiState, rotationLabelRes = rotationLabelRes)

            Button(
                onClick = {
                    onToggleApp(!uiState.appActive)
                },
                enabled = uiState.canEnableApp || uiState.appActive,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(
                        if (uiState.appActive) {
                            R.string.action_disable_app
                        } else {
                            R.string.action_enable_app
                        },
                    ),
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.miui_section_title),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(stringResource(R.string.miui_section_desc))
                    OutlinedButton(onClick = onBatteryOptimization, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.miui_battery))
                    }
                    OutlinedButton(onClick = onAutostart, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.miui_autostart))
                    }
                }
            }

            FaqSection()
            OutlinedButton(onClick = onPrivacy, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.about_privacy))
            }
        }
    }
}

@Composable
private fun ConfigurationSection(
    uiState: MainUiState,
    onRotationToggled: (toggle: Int, enabled: Boolean) -> Unit,
    onForceAutoRotateChanged: (Boolean) -> Unit,
) {
    val allowed = uiState.allowedRotations
    val enabledCount = allowed.enabledCount()
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .padding(16.dp)
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.config_section_title),
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                stringResource(R.string.config_rotations_label),
                style = MaterialTheme.typography.labelLarge,
            )

            Text(
                stringResource(R.string.config_rotations_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RotationToggleRow(
                label = stringResource(R.string.rotation_0),
                checked = allowed.portrait,
                enabledCount = enabledCount,
                writeSettingsGranted = uiState.writeSettingsGranted,
                onCheckedChange = {
                    onRotationToggled(AllowedRotations.TOGGLE_PORTRAIT, it)
                },
            )
            RotationToggleRow(
                label = stringResource(R.string.rotation_90),
                checked = allowed.landscape,
                enabledCount = enabledCount,
                writeSettingsGranted = uiState.writeSettingsGranted,
                onCheckedChange = {
                    onRotationToggled(AllowedRotations.TOGGLE_LANDSCAPE, it)
                },
            )
            RotationToggleRow(
                label = stringResource(R.string.rotation_270),
                checked = allowed.reverseLandscape,
                enabledCount = enabledCount,
                writeSettingsGranted = uiState.writeSettingsGranted,
                onCheckedChange = {
                    onRotationToggled(AllowedRotations.TOGGLE_REVERSE_LANDSCAPE, it)
                },
            )
            RotationToggleRow(
                label = stringResource(R.string.rotation_180),
                checked = allowed.reversePortrait,
                enabledCount = enabledCount,
                writeSettingsGranted = uiState.writeSettingsGranted,
                onCheckedChange = {
                    onRotationToggled(AllowedRotations.TOGGLE_REVERSE_PORTRAIT, it)
                },
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.config_force_auto_rotate),
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        stringResource(R.string.config_force_auto_rotate_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = uiState.forceSystemAutoRotate,
                    onCheckedChange = onForceAutoRotateChanged,
                    enabled = uiState.writeSettingsGranted,
                )
            }
        }
    }
}

@Composable
private fun RotationToggleRow(
    label: String,
    checked: Boolean,
    enabledCount: Int,
    writeSettingsGranted: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = writeSettingsGranted && (!checked || enabledCount > 1),
        )
    }
}

@Composable
private fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.about_license))
            Text(stringResource(R.string.about_fdroid))
            OutlinedButton(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.source_url))),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.about_source))
            }
            OutlinedButton(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.privacy_url))),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.about_privacy))
            }
        }
    }
}

@Composable
private fun Banner(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text, Modifier.padding(12.dp))
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    granted: Boolean,
    grantedLabel: String,
    deniedLabel: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    if (granted) grantedLabel else deniedLabel,
                    color = if (granted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
            Text(description)
            if (!granted) {
                OutlinedButton(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
private fun StatusCard(uiState: MainUiState, rotationLabelRes: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            StatusRow(
                stringResource(R.string.status_app),
                stringResource(
                    if (uiState.appActive) R.string.status_app_on else R.string.status_app_off,
                ),
            )
            StatusRow(
                stringResource(R.string.status_auto_rotate),
                stringResource(
                    if (uiState.autoRotateEnabled) {
                        R.string.status_auto_rotate_on
                    } else {
                        R.string.status_auto_rotate_off
                    },
                ),
            )
            StatusRow(
                stringResource(R.string.status_rotation),
                stringResource(rotationLabelRes),
            )
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FaqSection() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.faq_title), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.faq_anti_flicker))
            Text(stringResource(R.string.faq_disabled_rotations))
            Text(stringResource(R.string.faq_app_orientation))
            Text(stringResource(R.string.faq_channels))
        }
    }
    Spacer(Modifier.height(8.dp))
}
