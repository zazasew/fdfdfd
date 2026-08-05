package com.cozynotes.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cozynotes.app.data.preferences.AppTheme
import com.cozynotes.app.data.preferences.FontSize
import com.cozynotes.app.data.preferences.UserPreferences
import com.cozynotes.app.data.preferences.UserSettings
import com.cozynotes.app.data.repository.NotesRepository
import com.cozynotes.app.model.Avatar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val noteCount: Int = 0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val notesRepository: NotesRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferences.userSettings,
        notesRepository.getNoteCount()
    ) { settings, count ->
        SettingsUiState(settings = settings, noteCount = count)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setUserName(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { userPreferences.setUserName(name.trim()) }
    }

    fun setAvatar(avatar: Avatar) {
        viewModelScope.launch { userPreferences.setAvatar(avatar) }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { userPreferences.setTheme(theme) }
    }

    fun setFontSize(fontSize: FontSize) {
        viewModelScope.launch { userPreferences.setFontSize(fontSize) }
    }

    fun setDefaultNoteColor(color: Int?) {
        viewModelScope.launch { userPreferences.setDefaultNoteColor(color) }
    }

    fun setAutoSaveEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setAutoSaveEnabled(enabled) }
    }
}
