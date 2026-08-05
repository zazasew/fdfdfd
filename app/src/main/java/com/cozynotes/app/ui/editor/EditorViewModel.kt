package com.cozynotes.app.ui.editor

import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cozynotes.app.data.preferences.UserPreferences
import com.cozynotes.app.data.repository.NotesRepository
import com.cozynotes.app.model.Note
import com.cozynotes.app.util.RichText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class EditorUiState(
    val noteId: String? = null,
    val title: String = "",
    val content: TextFieldValue = TextFieldValue(""),
    val pinned: Boolean = false,
    val favorite: Boolean = false,
    val color: Int? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isNewNote: Boolean = true
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val userPreferences: UserPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    // Linear undo/redo history of (title, content-text) snapshots.
    private val undoStack = ArrayDeque<Pair<String, String>>()
    private val redoStack = ArrayDeque<Pair<String, String>>()
    private var autoSaveEnabled = true
    private var lastHistoryPush: Pair<String, String>? = null

    init {
        val noteId: String? = savedStateHandle["noteId"]
        if (noteId != null && noteId != "new") {
            viewModelScope.launch {
                notesRepository.getNoteById(noteId)?.let { note ->
                    _uiState.value = EditorUiState(
                        noteId = note.id,
                        title = note.title,
                        content = TextFieldValue(note.content, TextRange(note.content.length)),
                        pinned = note.pinned,
                        favorite = note.favorite,
                        color = note.color,
                        isNewNote = false
                    )
                    lastHistoryPush = note.title to note.content
                }
            }
        } else {
            viewModelScope.launch {
                val defaultColor = userPreferences.userSettings.first().defaultNoteColor
                    ?: com.cozynotes.app.ui.theme.noteColorPalette.random().toArgb()
                _uiState.value = _uiState.value.copy(color = defaultColor)
            }
        }

        viewModelScope.launch {
            autoSaveEnabled = userPreferences.userSettings.first().autoSaveEnabled
        }
    }

    private fun pushHistoryIfChanged(title: String, content: String) {
        val current = title to content
        if (current == lastHistoryPush) return
        lastHistoryPush?.let { undoStack.addLast(it) }
        if (undoStack.size > 50) undoStack.removeFirst()
        redoStack.clear()
        lastHistoryPush = current
        updateUndoRedoFlags()
    }

    private fun updateUndoRedoFlags() {
        _uiState.value = _uiState.value.copy(
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty()
        )
    }

    fun onTitleChanged(newTitle: String) {
        pushHistoryIfChanged(newTitle, _uiState.value.content.text)
        _uiState.value = _uiState.value.copy(title = newTitle)
        autoSave()
    }

    fun onContentChanged(newValue: TextFieldValue) {
        val adjusted = RichText.continueListOnEnter(_uiState.value.content, newValue)
        pushHistoryIfChanged(_uiState.value.title, adjusted.text)
        _uiState.value = _uiState.value.copy(content = adjusted)
        autoSave()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        lastHistoryPush?.let { redoStack.addLast(it) }
        val (title, content) = undoStack.removeLast()
        lastHistoryPush = title to content
        _uiState.value = _uiState.value.copy(
            title = title,
            content = TextFieldValue(content, TextRange(content.length))
        )
        updateUndoRedoFlags()
        autoSave()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        lastHistoryPush?.let { undoStack.addLast(it) }
        val (title, content) = redoStack.removeLast()
        lastHistoryPush = title to content
        _uiState.value = _uiState.value.copy(
            title = title,
            content = TextFieldValue(content, TextRange(content.length))
        )
        updateUndoRedoFlags()
        autoSave()
    }

    // --- Formatting actions, all genuinely functional against the current selection ---

    fun applyBold() = applyInlineTag("b")
    fun applyItalic() = applyInlineTag("i")
    fun applyUnderline() = applyInlineTag("u")
    fun applyHighlight() = applyInlineTag("h")

    fun applyTextColor(hex: String) {
        val newValue = RichText.toggleInlineTag(_uiState.value.content, "c", hex)
        onContentChanged(newValue)
    }

    private fun applyInlineTag(tag: String) {
        val newValue = RichText.toggleInlineTag(_uiState.value.content, tag)
        onContentChanged(newValue)
    }

    fun applyBulletList() = applyLinePrefix("\u2022 ")
    fun applyNumberedList() = applyLinePrefix("1. ")

    /** Checkbox button: adds an unchecked box on lines that don't have one
     *  yet, or flips checked ↔ unchecked on lines that already do (checking
     *  a line strikes its text through — see RichText.parse). */
    fun applyCheckboxList() {
        val newValue = RichText.toggleCheckbox(_uiState.value.content)
        onContentChanged(newValue)
    }

    private fun applyLinePrefix(prefix: String) {
        val newValue = RichText.toggleLinePrefix(_uiState.value.content, prefix)
        onContentChanged(newValue)
    }

    fun togglePin() {
        _uiState.value = _uiState.value.copy(pinned = !_uiState.value.pinned)
        autoSave()
    }

    fun toggleFavorite() {
        _uiState.value = _uiState.value.copy(favorite = !_uiState.value.favorite)
        autoSave()
    }

    fun setColor(color: Int?) {
        _uiState.value = _uiState.value.copy(color = color)
        autoSave()
    }

    private fun autoSave() {
        if (!autoSaveEnabled) return
        persistNow()
    }

    /** Persists the current draft immediately, regardless of the Auto Save
     *  setting — used when autosave is off and the user navigates away. */
    fun persistNow() {
        val state = _uiState.value
        val contentText = state.content.text
        if (state.title.isBlank() && contentText.isBlank()) return

        viewModelScope.launch {
            val existing = state.noteId?.let { notesRepository.getNoteById(it) }
            val note = Note(
                id = state.noteId ?: existing?.id ?: java.util.UUID.randomUUID().toString(),
                title = state.title,
                content = contentText,
                createdDate = existing?.createdDate ?: Date(),
                modifiedDate = Date(),
                pinned = state.pinned,
                favorite = state.favorite,
                archived = false,
                color = state.color
            )
            notesRepository.saveNote(note)
            if (state.noteId == null) {
                _uiState.value = state.copy(noteId = note.id, isNewNote = false)
            }
        }
    }

    fun deleteNote() {
        val id = _uiState.value.noteId ?: return
        viewModelScope.launch {
            notesRepository.getNoteById(id)?.let { notesRepository.deleteNote(it) }
        }
    }
}
