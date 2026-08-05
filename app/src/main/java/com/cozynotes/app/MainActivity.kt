package com.cozynotes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cozynotes.app.data.preferences.AppTheme
import com.cozynotes.app.data.preferences.FontSize
import com.cozynotes.app.data.preferences.UserPreferences
import com.cozynotes.app.ui.navigation.NotesNavGraph
import com.cozynotes.app.ui.theme.PersonalNotesTheme
import com.cozynotes.app.ui.theme.ThemeMode
import com.cozynotes.app.ui.theme.fontScaleFor
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val settings by appViewModel.settings.collectAsState()

            val themeMode = when (settings.theme) {
                AppTheme.LIGHT -> ThemeMode.LIGHT
                AppTheme.DARK -> ThemeMode.DARK
                AppTheme.SYSTEM -> ThemeMode.SYSTEM
            }

            PersonalNotesTheme(themeMode = themeMode, fontScale = fontScaleFor(settings.fontSize)) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Wait for the first DataStore read before deciding which
                    // start screen to show, so we never briefly flash onboarding
                    // for a returning user.
                    if (settings.isLoaded) {
                        NotesNavGraph(hasOnboarded = settings.hasOnboarded)
                    }
                }
            }
        }
    }
}

data class AppUiSettings(
    val hasOnboarded: Boolean = false,
    val theme: AppTheme = AppTheme.SYSTEM,
    val fontSize: FontSize = FontSize.MEDIUM,
    val isLoaded: Boolean = false
)

@HiltViewModel
class AppViewModel @Inject constructor(
    userPreferences: UserPreferences
) : ViewModel() {

    val settings: StateFlow<AppUiSettings> = userPreferences.userSettings
        .map { s -> AppUiSettings(hasOnboarded = s.hasOnboarded, theme = s.theme, fontSize = s.fontSize, isLoaded = true) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppUiSettings()
        )
}
