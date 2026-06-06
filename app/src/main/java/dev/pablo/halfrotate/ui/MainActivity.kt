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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pablo.halfrotate.BuildConfig
import dev.pablo.halfrotate.R
import dev.pablo.halfrotate.rotation.HorizontalMode
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
                MainScreen(
                    uiState = uiState,
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
                    onHorizontalModeChanged = viewModel::setHorizontalMode,
                    onToggleApp = viewModel::toggleApp,
                    onBatteryOptimization = {
                        startActivity(PermissionsHelper.batteryOptimizationIntent(this))
                    },
                    onAutostart = {
                        startActivity(PermissionsHelper.autostartIntent(this))
                    },
                    onOpenUrl = { url ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    uiState: MainUiState,
    onGrantWriteSettings: () -> Unit,
    onGrantNotifications: () -> Unit,
    onHorizontalModeChanged: (HorizontalMode) -> Unit,
    onToggleApp: (Boolean) -> Unit,
    onBatteryOptimization: () -> Unit,
    onAutostart: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
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
            if (uiState.serviceStartFailed) {
                Banner(text = stringResource(R.string.service_start_failed_banner))
            }

            AppSwitchRow(
                checked = uiState.appActive,
                enabled = uiState.canEnableApp || uiState.appActive,
                onCheckedChange = onToggleApp,
            )

            HorizontalModeSelector(
                mode = uiState.horizontalMode,
                enabled = uiState.writeSettingsGranted,
                onModeChanged = onHorizontalModeChanged,
            )

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

            AboutSection(onOpenUrl = onOpenUrl)
        }
    }
}

@Composable
private fun AboutSection(onOpenUrl: (String) -> Unit) {
    val sourceUrl = stringResource(R.string.about_source_url)
    val privacyUrl = stringResource(R.string.about_privacy_url)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.about_section_title),
                fontWeight = FontWeight.SemiBold,
            )
            Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME))
            Text(stringResource(R.string.about_license))
            OutlinedButton(
                onClick = { onOpenUrl(sourceUrl) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.about_source_code))
            }
            OutlinedButton(
                onClick = { onOpenUrl(privacyUrl) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.about_privacy_policy))
            }
        }
    }
}

@Composable
private fun AppSwitchRow(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.app_name),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HorizontalModeSelector(
    mode: HorizontalMode,
    enabled: Boolean,
    onModeChanged: (HorizontalMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (mode) {
        HorizontalMode.LANDSCAPE_90 -> stringResource(R.string.rotation_90)
        HorizontalMode.REVERSE_LANDSCAPE_270 -> stringResource(R.string.rotation_270)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = label,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                label = { Text(stringResource(R.string.config_horizontal_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.rotation_90)) },
                    onClick = {
                        onModeChanged(HorizontalMode.LANDSCAPE_90)
                        expanded = false
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.rotation_270)) },
                    onClick = {
                        onModeChanged(HorizontalMode.REVERSE_LANDSCAPE_270)
                        expanded = false
                    },
                )
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
