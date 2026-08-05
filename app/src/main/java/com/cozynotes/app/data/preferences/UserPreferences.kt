package com.cozynotes.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cozynotes.app.model.Avatar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class AppTheme { LIGHT, DARK, SYSTEM }
enum class FontSize { SMALL, MEDIUM, LARGE }

data class UserSettings(
    val userName: String = "",
    val hasOnboarded: Boolean = false,
    val avatar: Avatar = Avatar.GIRL,
    val theme: AppTheme = AppTheme.SYSTEM,
    val fontSize: FontSize = FontSize.MEDIUM,
    val defaultNoteColor: Int? = null,
    val autoSaveEnabled: Boolean = true
)

/**
 * Thin wrapper around Jetpack DataStore for the small amount of user/app state
 * that isn't a note (name, avatar, theme, etc). Persisted locally only.
 */
@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val HAS_ONBOARDED = booleanPreferencesKey("has_onboarded")
        val AVATAR = stringPreferencesKey("avatar")
        val THEME = stringPreferencesKey("theme")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val DEFAULT_NOTE_COLOR = intPreferencesKey("default_note_color")
        val AUTO_SAVE = booleanPreferencesKey("auto_save_enabled")
    }

    val userSettings: Flow<UserSettings> = dataStore.data.map { prefs ->
        UserSettings(
            userName = prefs[Keys.USER_NAME] ?: "",
            hasOnboarded = prefs[Keys.HAS_ONBOARDED] ?: false,
            avatar = Avatar.fromId(prefs[Keys.AVATAR]),
            theme = prefs[Keys.THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() }
                ?: AppTheme.SYSTEM,
            fontSize = prefs[Keys.FONT_SIZE]?.let { runCatching { FontSize.valueOf(it) }.getOrNull() }
                ?: FontSize.MEDIUM,
            defaultNoteColor = prefs[Keys.DEFAULT_NOTE_COLOR],
            autoSaveEnabled = prefs[Keys.AUTO_SAVE] ?: true
        )
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { it[Keys.USER_NAME] = name }
    }

    suspend fun completeOnboarding(name: String, avatar: Avatar) {
        dataStore.edit {
            it[Keys.USER_NAME] = name
            it[Keys.AVATAR] = avatar.id
            it[Keys.HAS_ONBOARDED] = true
        }
    }

    suspend fun setAvatar(avatar: Avatar) {
        dataStore.edit { it[Keys.AVATAR] = avatar.id }
    }

    suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun setFontSize(fontSize: FontSize) {
        dataStore.edit { it[Keys.FONT_SIZE] = fontSize.name }
    }

    suspend fun setDefaultNoteColor(color: Int?) {
        dataStore.edit {
            if (color == null) it.remove(Keys.DEFAULT_NOTE_COLOR) else it[Keys.DEFAULT_NOTE_COLOR] = color
        }
    }

    suspend fun setAutoSaveEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_SAVE] = enabled }
    }
}
