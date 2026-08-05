package com.cozynotes.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cozynotes.app.data.preferences.UserPreferences
import com.cozynotes.app.model.Avatar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val name: String = "",
    val selectedAvatar: Avatar = Avatar.GIRL,
    val isComplete: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onAvatarSelected(avatar: Avatar) {
        _uiState.value = _uiState.value.copy(selectedAvatar = avatar)
    }

    fun completeOnboarding() {
        val state = _uiState.value
        val trimmedName = state.name.trim()
        if (trimmedName.isEmpty()) return

        viewModelScope.launch {
            userPreferences.completeOnboarding(trimmedName, state.selectedAvatar)
            _uiState.value = state.copy(isComplete = true)
        }
    }
}
