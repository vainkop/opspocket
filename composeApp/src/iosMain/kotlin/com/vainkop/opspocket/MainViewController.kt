package com.vainkop.opspocket

import androidx.compose.ui.window.ComposeUIViewController
import com.vainkop.opspocket.di.platformModule
import com.vainkop.opspocket.di.sharedModules
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        startKoin {
            modules(platformModule + sharedModules)
        }
    }
) {
    App()
}
