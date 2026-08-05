package com.cozynotes.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cozynotes.app.ui.editor.EditorScreen
import com.cozynotes.app.ui.home.HomeScreen
import com.cozynotes.app.ui.onboarding.OnboardingScreen
import com.cozynotes.app.ui.search.SearchScreen
import com.cozynotes.app.ui.settings.SettingsScreen
import com.cozynotes.app.ui.settings.ThemeScreen

@Composable
fun NotesNavGraph(hasOnboarded: Boolean) {
    val navController = rememberNavController()
    val startDestination = if (hasOnboarded) Screen.Home.route else Screen.Onboarding.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 8 } },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { it / 8 } }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNoteClick = { noteId -> navController.navigate(Screen.Editor.createRoute(noteId)) },
                onNewNoteClick = { navController.navigate(Screen.Editor.createRoute("new")) },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(navArgument(Screen.Editor.ARG_NOTE_ID) { type = NavType.StringType })
        ) {
            EditorScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onNoteClick = { noteId ->
                    navController.navigate(Screen.Editor.createRoute(noteId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTheme = { navController.navigate(Screen.Theme.route) }
            )
        }

        composable(Screen.Theme.route) {
            ThemeScreen(onBack = { navController.popBackStack() })
        }
    }
}
