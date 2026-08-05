package com.cozynotes.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cozynotes.app.BuildConfig
import com.cozynotes.app.data.preferences.FontSize
import com.cozynotes.app.model.Avatar
import com.cozynotes.app.ui.components.AvatarView
import com.cozynotes.app.ui.theme.noteColorPalette

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToTheme: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNameDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item { SectionHeader("Profile") }
            item {
                SettingsRow(
                    title = "Name",
                    subtitle = uiState.settings.userName.ifBlank { "Not set" },
                    leading = { AvatarView(avatar = uiState.settings.avatar, size = 32.dp) },
                    onClick = { showNameDialog = true }
                )
            }
            item {
                SettingsRow(
                    title = "Avatar",
                    subtitle = uiState.settings.avatar.displayName,
                    onClick = { showAvatarDialog = true }
                )
            }

            item { SectionHeader("Appearance") }
            item {
                SettingsRow(
                    title = "Theme",
                    subtitle = uiState.settings.theme.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = onNavigateToTheme
                )
            }
            item {
                SettingsRow(
                    title = "Font size",
                    subtitle = uiState.settings.fontSize.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { showFontSizeDialog = true }
                )
            }
            item {
                SettingsRow(
                    title = "Default note color",
                    subtitle = "Tap to choose",
                    trailing = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    uiState.settings.defaultNoteColor?.let { Color(it) }
                                        ?: MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                )
                        )
                    },
                    onClick = { showColorDialog = true }
                )
            }

            item { SectionHeader("General") }
            item {
                SettingsRow(
                    title = "Auto Save",
                    subtitle = "Automatically save notes as you type",
                    trailing = {
                        Switch(
                            checked = uiState.settings.autoSaveEnabled,
                            onCheckedChange = { viewModel.setAutoSaveEnabled(it) }
                        )
                    },
                    onClick = { viewModel.setAutoSaveEnabled(!uiState.settings.autoSaveEnabled) }
                )
            }
            item {
                SettingsRow(
                    title = "Backup",
                    subtitle = "Coming soon — export/import your notes",
                    onClick = { }
                )
            }

            item { SectionHeader("About") }
            item {
                SettingsRow(
                    title = "Notes stored",
                    subtitle = "${uiState.noteCount} notes, all on this device",
                    onClick = { }
                )
            }
            item {
                SettingsRow(
                    title = "Version",
                    subtitle = BuildConfig.VERSION_NAME,
                    onClick = { }
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showNameDialog) {
        var draftName by remember { mutableStateOf(uiState.settings.userName) }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Your name") },
            text = {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    singleLine = true,
                    placeholder = { Text("e.g. Sasa") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setUserName(draftName)
                    showNameDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text("Choose avatar") },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Avatar.entries.forEach { avatar ->
                        Box(
                            modifier = Modifier
                                .clickable {
                                    viewModel.setAvatar(avatar)
                                    showAvatarDialog = false
                                }
                                .border(
                                    width = if (uiState.settings.avatar == avatar) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .padding(4.dp)
                        ) {
                            AvatarView(avatar = avatar, size = 56.dp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarDialog = false }) { Text("Close") }
            }
        )
    }

    if (showFontSizeDialog) {
        AlertDialog(
            onDismissRequest = { showFontSizeDialog = false },
            title = { Text("Font size") },
            text = {
                Column {
                    FontSize.entries.forEach { size ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setFontSize(size)
                                    showFontSizeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.settings.fontSize == size,
                                onClick = { viewModel.setFontSize(size); showFontSizeDialog = false }
                            )
                            Icon(Icons.Default.TextFields, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(size.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showColorDialog) {
        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            title = { Text("Default note color") },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    noteColorPalette.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    viewModel.setDefaultNoteColor(color.toArgb())
                                    showColorDialog = false
                                }
                                .background(color, CircleShape)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setDefaultNoteColor(null)
                    showColorDialog = false
                }) { Text("Use default") }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (leading != null) {
                leading()
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
