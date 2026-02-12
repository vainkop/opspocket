package com.vainkop.opspocket.di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import com.vainkop.opspocket.data.local.SecureStorage
import org.koin.dsl.module

val platformModule = module {
    single { SecureStorage() }
    single<Settings> { NSUserDefaultsSettings.Factory().create("azure_preferences") }
}
