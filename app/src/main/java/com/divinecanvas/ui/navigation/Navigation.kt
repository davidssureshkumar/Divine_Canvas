package com.divinecanvas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.divinecanvas.ui.editor.EditorScreen
import com.divinecanvas.ui.settings.SettingsScreen

object Routes {
    const val EDITOR = "editor"
    const val SETTINGS = "settings"
}

@Composable
fun DivineCanvasNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.EDITOR) {
        composable(Routes.EDITOR) {
            EditorScreen(onOpenSettings = { navController.navigate(Routes.SETTINGS) })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
