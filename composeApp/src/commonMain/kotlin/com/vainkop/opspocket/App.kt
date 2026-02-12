package com.vainkop.opspocket

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.vainkop.opspocket.navigation.AppNavigation
import com.vainkop.opspocket.ui.theme.OpsPocketTheme

@Composable
fun App() {
    OpsPocketTheme {
        val navController = rememberNavController()
        AppNavigation(navController = navController)
    }
}
