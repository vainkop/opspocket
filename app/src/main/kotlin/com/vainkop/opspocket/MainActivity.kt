package com.vainkop.opspocket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.vainkop.opspocket.navigation.AppNavigation
import com.vainkop.opspocket.ui.theme.OpsPocketTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpsPocketTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}
