package com.cozynotes.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cozynotes.app.data.preferences.UserPreferences
import com.cozynotes.app.data.preferences.UserSettings
import com.cozynotes.app.data.repository.NotesRepository
import com.cozynotes.app.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val notes: List<Note> = emptyList(),
    val userSettings: UserSettings = UserSettings(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        notesRepository.getAllNotes(),
        userPreferences.userSettings
    ) { notes, settings ->
        HomeUiState(notes = notes, userSettings = settings, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun deleteNote(note: Note) {
        viewModelScope.launch { notesRepository.deleteNote(note) }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch { notesRepository.saveNote(note.copy(pinned = !note.pinned)) }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch { notesRepository.saveNote(note.copy(favorite = !note.favorite)) }
    }
}
