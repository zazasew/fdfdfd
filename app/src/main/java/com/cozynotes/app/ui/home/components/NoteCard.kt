package com.cozynotes.app.ui.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.cozynotes.app.model.Note
import com.cozynotes.app.ui.theme.cappuccinoTitleColor
import com.cozynotes.app.ui.theme.noteAccentBackground
import com.cozynotes.app.ui.theme.papyrusBodyColor
import com.cozynotes.app.util.RichText
import com.cozynotes.app.util.ShareUtils
import com.cozynotes.app.util.TimeUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
    highlightQuery: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(120),
        label = "noteCardScale"
    )
    val context = LocalContext.current
    var longPressMenuExpanded by remember { mutableStateOf(false) }
    var deleteConfirmVisible by remember { mutableStateOf(false) }

    // A gentle tint of the surface color, not the raw accent — keeps text
    // readable in both light and dark mode no matter which accent is picked.
    val cardColor = noteAccentBackground(note.color?.let { Color(it) })
    val titleColor = cappuccinoTitleColor()
    val bodyColor = papyrusBodyColor()
    val previewText = remember(note.content) { RichText.stripMarkup(note.content) }

    Box {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .scale(scale)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick,
                    onLongClick = { longPressMenuExpanded = true }
                ),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = highlightedText(note.title.ifBlank { "Untitled" }, highlightQuery),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (note.pinned) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = titleColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (note.favorite) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Favorite",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = highlightedText(previewText.ifBlank { "No additional text" }, highlightQuery),
                    style = MaterialTheme.typography.bodyMedium,
                    color = bodyColor,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(end = 28.dp)
                ) {
                    Text(
                        text = TimeUtils.relativeLabel(note.modifiedDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = bodyColor.copy(alpha = 0.7f)
                    )
                    if (note.color != null) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(note.color), CircleShape)
                        )
                    }
                }
            }
        }

        // Quick-delete "X" in the card's bottom-right corner.
        IconButton(
            onClick = { deleteConfirmVisible = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
                .size(28.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Delete note",
                tint = bodyColor.copy(alpha = 0.55f),
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = longPressMenuExpanded,
            onDismissRequest = { longPressMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Share as file") },
                onClick = {
                    longPressMenuExpanded = false
                    ShareUtils.shareNoteAsFile(context, note)
                }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    longPressMenuExpanded = false
                    deleteConfirmVisible = true
                }
            )
        }
    }

    if (deleteConfirmVisible) {
        AlertDialog(
            onDismissRequest = { deleteConfirmVisible = false },
            title = { Text("Delete note?") },
            text = { Text("\"${note.title.ifBlank { "Untitled" }}\" will be permanently deleted from this device.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteConfirmVisible = false
                    onDelete()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmVisible = false }) { Text("Cancel") }
            }
        )
    }
}

/** Bolds every case-insensitive occurrence of [query] within [text] — used to
 *  visually highlight matches in Search results, the way most note apps do. */
private fun highlightedText(text: String, query: String?) = buildAnnotatedString {
    if (query.isNullOrBlank()) {
        append(text)
        return@buildAnnotatedString
    }
    var startIndex = 0
    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    while (startIndex <= text.length) {
        val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
        if (matchIndex == -1) {
            append(text.substring(startIndex))
            break
        }
        append(text.substring(startIndex, matchIndex))
        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
            append(text.substring(matchIndex, matchIndex + query.length))
        }
        startIndex = matchIndex + query.length
    }
}
