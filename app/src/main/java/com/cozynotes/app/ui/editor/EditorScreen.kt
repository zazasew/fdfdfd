package com.cozynotes.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cozynotes.app.ui.theme.cappuccinoTitleColor
import com.cozynotes.app.ui.theme.papyrusBodyColor
import com.cozynotes.app.ui.theme.noteColorPalette
import com.cozynotes.app.ui.theme.BodyFontFamily
import com.cozynotes.app.ui.theme.TitleFontFamily
import com.cozynotes.app.util.RichText
import com.cozynotes.app.util.ShareUtils
import com.cozynotes.app.util.TextStats

@Composable
fun EditorScreen(
    onBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    var colorPickerVisible by remember { mutableStateOf(false) }
    var textColorPickerVisible by remember { mutableStateOf(false) }

    val plainContent = RichText.stripMarkup(uiState.content.text)
    val titleColor = cappuccinoTitleColor()
    val bodyColor = papyrusBodyColor()

    Scaffold(
        // Always the theme's neutral background — never the raw note accent
        // color — so title/body text stays legible no matter which accent
        // is picked or whether the app is in light or dark mode. The note's
        // accent still shows, just as a thin strip below the top bar.
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { viewModel.persistNow(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::togglePin) {
                        Icon(
                            if (uiState.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = if (uiState.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = viewModel::toggleFavorite) {
                        Icon(
                            if (uiState.favorite) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Favorite",
                            tint = if (uiState.favorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Note color") },
                                onClick = { menuExpanded = false; colorPickerVisible = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Share as file") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.persistNow()
                                    ShareUtils.shareNoteAsFile(
                                        context,
                                        com.cozynotes.app.model.Note(
                                            id = uiState.noteId ?: "",
                                            title = uiState.title,
                                            content = uiState.content.text
                                        )
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Copy") },
                                onClick = {
                                    menuExpanded = false
                                    clipboardManager.setText(AnnotatedString(uiState.title + "\n" + plainContent))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.deleteNote()
                                    onBack()
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            // navigationBarsPadding() keeps this toolbar clear of the phone's
            // gesture bar / 3-button nav instead of drawing underneath it.
            Column(modifier = Modifier.navigationBarsPadding()) {
                if (colorPickerVisible) {
                    Text(
                        "Note color",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                    ColorPickerRow(
                        selected = uiState.color,
                        onSelect = { viewModel.setColor(it); colorPickerVisible = false }
                    )
                }
                if (textColorPickerVisible) {
                    Text(
                        "Text color",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                    ColorPickerRow(
                        selected = null,
                        onSelect = {
                            val hex = String.format("%06X", it and 0xFFFFFF)
                            viewModel.applyTextColor(hex)
                            textColorPickerVisible = false
                        }
                    )
                }
                FormattingToolbar(
                    canUndo = uiState.canUndo,
                    canRedo = uiState.canRedo,
                    onUndo = viewModel::undo,
                    onRedo = viewModel::redo,
                    onBold = viewModel::applyBold,
                    onItalic = viewModel::applyItalic,
                    onUnderline = viewModel::applyUnderline,
                    onBullet = viewModel::applyBulletList,
                    onNumbered = viewModel::applyNumberedList,
                    onCheckbox = viewModel::applyCheckboxList,
                    onTextColor = { textColorPickerVisible = !textColorPickerVisible },
                    onHighlight = viewModel::applyHighlight
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Words: ${TextStats.wordCount(plainContent)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Characters: ${TextStats.characterCount(plainContent)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Thin accent strip reflecting the note's chosen color — a hint
            // of color without ever compromising text contrast.
            if (uiState.color != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(Color(uiState.color!!))
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                TextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChanged,
                    placeholder = {
                        Text(
                            "Title",
                            style = MaterialTheme.typography.headlineMedium,
                            color = titleColor.copy(alpha = 0.45f)
                        )
                    },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = TitleFontFamily,
                        color = titleColor
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = titleColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = uiState.content,
                    onValueChange = viewModel::onContentChanged,
                    visualTransformation = RichText.visualTransformation(),
                    placeholder = {
                        Text(
                            "Start writing...",
                            color = bodyColor.copy(alpha = 0.5f)
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = BodyFontFamily,
                        color = bodyColor
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = bodyColor
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun FormattingToolbar(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onBullet: () -> Unit,
    onNumbered: () -> Unit,
    onCheckbox: () -> Unit,
    onTextColor: () -> Unit,
    onHighlight: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onUndo, enabled = canUndo) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
            }
            IconButton(onClick = onRedo, enabled = canRedo) {
                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
            }
            IconButton(onClick = onBold) {
                Icon(Icons.Default.FormatBold, contentDescription = "Bold")
            }
            IconButton(onClick = onItalic) {
                Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
            }
            IconButton(onClick = onUnderline) {
                Icon(Icons.Default.FormatUnderlined, contentDescription = "Underline")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onBullet) {
                Icon(Icons.Default.FormatListBulleted, contentDescription = "Bullet list")
            }
            IconButton(onClick = onNumbered) {
                Icon(Icons.Default.FormatListNumbered, contentDescription = "Numbered list")
            }
            IconButton(onClick = onCheckbox) {
                Icon(Icons.Default.CheckBox, contentDescription = "Checkbox — tap again on a checked line to mark it done")
            }
            IconButton(onClick = onTextColor) {
                Icon(Icons.Default.FormatColorText, contentDescription = "Text color")
            }
            IconButton(onClick = onHighlight) {
                Icon(Icons.Default.Highlight, contentDescription = "Highlight", tint = Color(0xFFE91E8C))
            }
        }
    }
}

@Composable
private fun ColorPickerRow(
    selected: Int?,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        noteColorPalette.forEach { color ->
            val isSelected = selected == color.toArgb()
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onSelect(color.toArgb()) }
                    .background(color, CircleShape)
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        }
    }
}
