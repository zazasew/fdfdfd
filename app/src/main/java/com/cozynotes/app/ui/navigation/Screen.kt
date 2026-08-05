package com.cozynotes.app.ui.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Editor : Screen("editor/{noteId}") {
        fun createRoute(noteId: String) = "editor/$noteId"
        const val ARG_NOTE_ID = "noteId"
    }
    data object Search : Screen("search")
    data object Settings : Screen("settings")
    data object Theme : Screen("settings/theme")
}
